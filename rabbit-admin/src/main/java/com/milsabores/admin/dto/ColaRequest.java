package com.milsabores.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ColaRequest(
		@NotBlank(message = "El nombre de la cola no puede estar vacío") String nombre,
		Boolean durable) {
}
