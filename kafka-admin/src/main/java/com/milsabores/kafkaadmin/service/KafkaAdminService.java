package com.milsabores.kafkaadmin.service;

import com.milsabores.kafkaadmin.dto.GrupoResponse;
import com.milsabores.kafkaadmin.dto.TopicRequest;
import com.milsabores.kafkaadmin.dto.TopicResponse;
import com.milsabores.kafkaadmin.dto.TopicUpdateRequest;
import com.milsabores.kafkaadmin.exception.DatoInvalidoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Service
public class KafkaAdminService {

	private static final int MAX_BYTES = 249;
	private static final int MAX_PARTICIONES = 30;
	private static final Set<String> CLEANUP = Set.of("delete", "compact");

	private final KafkaCluster cluster;
	private final int brokers;

	public KafkaAdminService(KafkaCluster cluster, @Value("${milsabores.kafka.brokers:3}") int brokers) {
		this.cluster = cluster;
		this.brokers = brokers;
	}

	public void crear(TopicRequest request) {
		String nombre = exigirNombre(request.nombre());
		int particiones = exigirParticiones(request.particiones());
		short replicas = exigirReplicas(request.factorReplicacion());
		String cleanup = exigirCleanup(request.cleanupPolicy());
		long retention = exigirRetention(request.retentionMs());
		cluster.crear(nombre, particiones, replicas, cleanup, retention);
	}

	public void actualizar(String nombre, TopicUpdateRequest request) {
		String limpio = exigirNombre(nombre);
		if (request.particiones() == null && request.cleanupPolicy() == null && request.retentionMs() == null) {
			throw new DatoInvalidoException("Hay que indicar particiones, cleanup o retención");
		}
		Integer particiones = request.particiones() == null ? null : exigirParticiones(request.particiones());
		String cleanup = request.cleanupPolicy() == null ? null : exigirCleanup(request.cleanupPolicy());
		Long retention = request.retentionMs() == null ? null : exigirRetention(request.retentionMs());
		cluster.actualizar(limpio, particiones, cleanup, retention);
	}

	public void eliminar(String nombre) {
		cluster.eliminar(exigirNombre(nombre));
	}

	public List<TopicResponse> listarTopics() {
		return cluster.listarTopics();
	}

	public List<GrupoResponse> listarGrupos() {
		return cluster.listarGrupos();
	}

	private int exigirParticiones(Integer particiones) {
		if (particiones == null || particiones < 1 || particiones > MAX_PARTICIONES) {
			throw new DatoInvalidoException("Las particiones deben estar entre 1 y " + MAX_PARTICIONES);
		}
		return particiones;
	}

	private short exigirReplicas(Integer factor) {
		if (factor == null || factor < 1 || factor > brokers) {
			throw new DatoInvalidoException("El factor de replicación debe estar entre 1 y " + brokers);
		}
		return factor.shortValue();
	}

	private static String exigirCleanup(String cleanup) {
		if (cleanup == null || cleanup.isBlank()) {
			throw new DatoInvalidoException("La cleanup policy no puede quedar en blanco");
		}
		String limpio = cleanup.trim().toLowerCase();
		if (!CLEANUP.contains(limpio)) {
			throw new DatoInvalidoException("Cleanup inválida. Use delete o compact");
		}
		return limpio;
	}

	private static long exigirRetention(Long retentionMs) {
		if (retentionMs == null || retentionMs < 1) {
			throw new DatoInvalidoException("La retención tiene que ser de al menos 1 milisegundo");
		}
		return retentionMs;
	}

	private static String exigirNombre(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			throw new DatoInvalidoException("El nombre del topic no puede quedar en blanco");
		}
		String limpio = nombre.trim();
		if (limpio.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
			throw new DatoInvalidoException("El nombre del topic supera los 249 bytes");
		}
		if (".".equals(limpio) || "..".equals(limpio) || limpio.startsWith("__")) {
			throw new DatoInvalidoException("El nombre del topic no es válido");
		}
		if (!limpio.matches("[A-Za-z0-9._-]+")) {
			throw new DatoInvalidoException("El nombre del topic solo puede usar letras, números, punto, guion y guion bajo");
		}
		return limpio;
	}
}
