package com.milsabores.kafkaadmin.service;

import com.milsabores.kafkaadmin.dto.TopicRequest;
import com.milsabores.kafkaadmin.dto.TopicUpdateRequest;
import com.milsabores.kafkaadmin.exception.DatoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KafkaAdminServiceTest {

	@Mock
	private KafkaCluster cluster;

	@Test
	void unNombreVacioNoLlegaAlCluster() {
		KafkaAdminService service = new KafkaAdminService(cluster, 3);

		assertThrows(DatoInvalidoException.class, () -> service.crear(pedido("  ", 3, 3, "delete", 3_600_000L)));

		verifyNoInteractions(cluster);
	}

	@Test
	void unTopicInternoNoSeCrea() {
		KafkaAdminService service = new KafkaAdminService(cluster, 3);

		assertThrows(DatoInvalidoException.class,
				() -> service.crear(pedido("__consumer_offsets", 3, 3, "delete", 3_600_000L)));

		verifyNoInteractions(cluster);
	}

	@Test
	void unaCleanupInvalidaNoLlegaAlCluster() {
		KafkaAdminService service = new KafkaAdminService(cluster, 3);

		assertThrows(DatoInvalidoException.class,
				() -> service.crear(pedido("milsabores.prueba", 3, 3, "correo", 3_600_000L)));

		verifyNoInteractions(cluster);
	}

	@Test
	void unFactorMayorQueLosBrokersNoLlegaAlCluster() {
		KafkaAdminService service = new KafkaAdminService(cluster, 3);

		assertThrows(DatoInvalidoException.class,
				() -> service.crear(pedido("milsabores.prueba", 3, 4, "delete", 3_600_000L)));

		verifyNoInteractions(cluster);
	}

	@Test
	void unTopicValidoSeCreaEnElCluster() {
		KafkaAdminService service = new KafkaAdminService(cluster, 3);

		service.crear(pedido(" milsabores.prueba ", 3, 3, "DELETE", 3_600_000L));

		verify(cluster).crear("milsabores.prueba", 3, (short) 3, "delete", 3_600_000L);
	}

	@Test
	void unCambioVacioNoLlegaAlCluster() {
		KafkaAdminService service = new KafkaAdminService(cluster, 3);

		assertThrows(DatoInvalidoException.class,
				() -> service.actualizar("milsabores.stock", new TopicUpdateRequest(null, null, null)));

		verifyNoInteractions(cluster);
	}

	@Test
	void cambiarLaRetencionLlegaAlCluster() {
		KafkaAdminService service = new KafkaAdminService(cluster, 3);

		service.actualizar("milsabores.stock", new TopicUpdateRequest(null, "compact", 7_200_000L));

		verify(cluster).actualizar("milsabores.stock", null, "compact", 7_200_000L);
	}

	private static TopicRequest pedido(String nombre, int particiones, int replicas, String cleanup, long retention) {
		return new TopicRequest(nombre, particiones, replicas, cleanup, retention);
	}
}
