package com.milsabores.kafkaadmin.service;

import com.milsabores.kafkaadmin.dto.GrupoResponse;
import com.milsabores.kafkaadmin.dto.TopicResponse;
import com.milsabores.kafkaadmin.exception.DatoInvalidoException;
import com.milsabores.kafkaadmin.exception.KafkaAdminException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaAdminClientCluster implements KafkaCluster {

	private static final Logger log = LoggerFactory.getLogger(KafkaAdminClientCluster.class);
	private static final int ESPERA_SEGUNDOS = 10;

	private final AdminClient adminClient;

	public KafkaAdminClientCluster(AdminClient adminClient) {
		this.adminClient = adminClient;
	}

	@Override
	public void crear(String nombre, int particiones, short factorReplicacion, String cleanupPolicy, long retentionMs) {
		NewTopic topic = new NewTopic(nombre, particiones, factorReplicacion);
		topic.configs(Map.of(
				TopicConfig.CLEANUP_POLICY_CONFIG, cleanupPolicy,
				TopicConfig.RETENTION_MS_CONFIG, Long.toString(retentionMs)));
		ejecutar(() -> adminClient.createTopics(List.of(topic)).all().get(ESPERA_SEGUNDOS, TimeUnit.SECONDS));
	}

	@Override
	public void actualizar(String nombre, Integer particiones, String cleanupPolicy, Long retentionMs) {
		TopicDescription actual = describir(nombre);
		if (particiones != null) {
			int hoy = actual.partitions().size();
			if (particiones < hoy) {
				throw new DatoInvalidoException("Las particiones solo pueden aumentar. Hoy tiene " + hoy);
			}
			if (particiones > hoy) {
				ejecutar(() -> adminClient.createPartitions(Map.of(nombre, NewPartitions.increaseTo(particiones)))
						.all()
						.get(ESPERA_SEGUNDOS, TimeUnit.SECONDS));
			}
		}
		List<AlterConfigOp> cambios = new ArrayList<>();
		if (cleanupPolicy != null) {
			cambios.add(new AlterConfigOp(
					new ConfigEntry(TopicConfig.CLEANUP_POLICY_CONFIG, cleanupPolicy),
					AlterConfigOp.OpType.SET));
		}
		if (retentionMs != null) {
			cambios.add(new AlterConfigOp(
					new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, Long.toString(retentionMs)),
					AlterConfigOp.OpType.SET));
		}
		if (!cambios.isEmpty()) {
			ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
			ejecutar(() -> adminClient.incrementalAlterConfigs(Map.of(recurso, cambios))
					.all()
					.get(ESPERA_SEGUNDOS, TimeUnit.SECONDS));
		}
	}

	@Override
	public void eliminar(String nombre) {
		ejecutar(() -> adminClient.deleteTopics(List.of(nombre)).all().get(ESPERA_SEGUNDOS, TimeUnit.SECONDS));
	}

	@Override
	public List<TopicResponse> listarTopics() {
		Collection<String> nombres = ejecutar(() -> adminClient.listTopics().names().get(ESPERA_SEGUNDOS, TimeUnit.SECONDS))
				.stream()
				.filter(nombre -> !nombre.startsWith("__"))
				.sorted()
				.toList();
		Map<String, TopicDescription> descriptions = new LinkedHashMap<>();
		for (String nombre : nombres) {
			try {
				descriptions.put(nombre, describir(nombre));
			} catch (KafkaAdminException e) {
				if (e.status() != HttpStatus.NOT_FOUND) {
					throw e;
				}
				log.info("El topic {} todavía no está visible. La lista sigue con los demás", nombre);
			}
		}
		if (descriptions.isEmpty()) {
			return List.of();
		}
		Map<ConfigResource, Config> configs = ejecutar(() -> {
			List<ConfigResource> recursos = descriptions.keySet().stream()
					.map(nombre -> new ConfigResource(ConfigResource.Type.TOPIC, nombre))
					.toList();
			return adminClient.describeConfigs(recursos).all().get(ESPERA_SEGUNDOS, TimeUnit.SECONDS);
		});
		return descriptions.values().stream()
				.sorted(Comparator.comparing(TopicDescription::name))
				.map(description -> aTopic(description, configs))
				.toList();
	}

	@Override
	public List<GrupoResponse> listarGrupos() {
		Collection<ConsumerGroupListing> listados = ejecutar(() -> adminClient.listConsumerGroups()
				.all()
				.get(ESPERA_SEGUNDOS, TimeUnit.SECONDS));
		List<String> ids = listados.stream()
				.map(ConsumerGroupListing::groupId)
				.filter(grupo -> !grupo.startsWith("__"))
				.sorted()
				.toList();
		if (ids.isEmpty()) {
			return List.of();
		}
		Map<String, ConsumerGroupDescription> descritos = ejecutar(() -> adminClient.describeConsumerGroups(ids)
				.all()
				.get(ESPERA_SEGUNDOS, TimeUnit.SECONDS));
		return ids.stream()
				.map(descritos::get)
				.map(grupo -> new GrupoResponse(
						grupo.groupId(),
						grupo.state().name(),
						grupo.members().size()))
				.toList();
	}

	private TopicDescription describir(String nombre) {
		Map<String, TopicDescription> topics = ejecutar(() -> adminClient.describeTopics(List.of(nombre))
				.allTopicNames()
				.get(ESPERA_SEGUNDOS, TimeUnit.SECONDS));
		TopicDescription description = topics.get(nombre);
		if (description == null) {
			throw new KafkaAdminException(HttpStatus.NOT_FOUND, "El topic no existe");
		}
		return description;
	}

	private static TopicResponse aTopic(TopicDescription description, Map<ConfigResource, Config> configs) {
		List<TopicPartitionInfo> particiones = description.partitions();
		int replicas = particiones.isEmpty() ? 0 : particiones.get(0).replicas().size();
		Config config = configs.get(new ConfigResource(ConfigResource.Type.TOPIC, description.name()));
		return new TopicResponse(
				description.name(),
				particiones.size(),
				replicas,
				valor(config, TopicConfig.CLEANUP_POLICY_CONFIG),
				valor(config, TopicConfig.RETENTION_MS_CONFIG));
	}

	private static String valor(Config config, String clave) {
		if (config == null) {
			return "";
		}
		ConfigEntry entry = config.get(clave);
		return entry == null || entry.value() == null ? "" : entry.value();
	}

	private <T> T ejecutar(Llamada<T> llamada) {
		try {
			return llamada.ejecutar();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new KafkaAdminException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka no respondió");
		} catch (TimeoutException e) {
			throw new KafkaAdminException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka no respondió");
		} catch (ExecutionException e) {
			throw traducir(e.getCause() == null ? e : e.getCause());
		}
	}

	private static RuntimeException traducir(Throwable error) {
		Throwable actual = error;
		while (actual != null) {
			if (actual instanceof TopicExistsException) {
				return new KafkaAdminException(HttpStatus.CONFLICT, "El topic ya existe");
			}
			if (actual instanceof UnknownTopicOrPartitionException) {
				return new KafkaAdminException(HttpStatus.NOT_FOUND, "El topic no existe");
			}
			if (actual instanceof InvalidReplicationFactorException
					|| actual instanceof InvalidPartitionsException
					|| actual instanceof InvalidConfigurationException
					|| actual instanceof InvalidRequestException) {
				return new DatoInvalidoException(actual.getMessage());
			}
			if (actual instanceof org.apache.kafka.common.errors.TimeoutException) {
				return new KafkaAdminException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka no respondió");
			}
			actual = actual.getCause();
		}
		log.warn("Kafka no pudo completar la operación", error);
		return new KafkaAdminException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka no pudo completar la operación");
	}

	@FunctionalInterface
	private interface Llamada<T> {
		T ejecutar() throws InterruptedException, ExecutionException, TimeoutException;
	}
}
