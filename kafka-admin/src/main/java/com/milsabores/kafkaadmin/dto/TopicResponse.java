package com.milsabores.kafkaadmin.dto;

public record TopicResponse(
		String nombre,
		int particiones,
		int factorReplicacion,
		String cleanupPolicy,
		String retentionMs) {
}
