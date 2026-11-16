package com.milsabores.kafkaadmin.service;

import com.milsabores.kafkaadmin.dto.GrupoResponse;
import com.milsabores.kafkaadmin.dto.TopicResponse;

import java.util.List;

/**
 * Operaciones sobre el clúster. No valida el pedido HTTP:
 * eso lo hace KafkaAdminService antes de llamar aquí.
 */
public interface KafkaCluster {

	void crear(String nombre, int particiones, short factorReplicacion, String cleanupPolicy, long retentionMs);

	void actualizar(String nombre, Integer particiones, String cleanupPolicy, Long retentionMs);

	void eliminar(String nombre);

	List<TopicResponse> listarTopics();

	List<GrupoResponse> listarGrupos();
}
