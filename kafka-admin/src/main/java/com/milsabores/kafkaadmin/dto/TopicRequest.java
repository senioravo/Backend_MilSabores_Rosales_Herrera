package com.milsabores.kafkaadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TopicRequest(
		@NotBlank(message = "El nombre del topic no puede estar vacío") String nombre,
		@NotNull(message = "Las particiones no pueden estar vacías")
		@Min(value = 1, message = "El topic necesita al menos una partición") Integer particiones,
		@NotNull(message = "El factor de replicación no puede estar vacío")
		@Min(value = 1, message = "El factor de replicación debe ser al menos 1") Integer factorReplicacion,
		@NotBlank(message = "La cleanup policy no puede estar vacía") String cleanupPolicy,
		@NotNull(message = "La retención no puede estar vacía")
		@Min(value = 1, message = "La retención tiene que ser de al menos 1 milisegundo") Long retentionMs) {
}
