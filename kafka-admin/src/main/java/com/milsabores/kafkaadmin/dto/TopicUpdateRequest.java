package com.milsabores.kafkaadmin.dto;

import jakarta.validation.constraints.Min;

public record TopicUpdateRequest(
		@Min(value = 1, message = "El topic necesita al menos una partición") Integer particiones,
		String cleanupPolicy,
		@Min(value = 1, message = "La retención tiene que ser de al menos 1 milisegundo") Long retentionMs) {
}
