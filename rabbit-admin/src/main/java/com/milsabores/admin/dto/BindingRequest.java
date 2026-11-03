package com.milsabores.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record BindingRequest(
		@NotBlank(message = "El nombre del exchange no puede estar vacío") String exchange,
		@NotBlank(message = "El nombre de la cola no puede estar vacío") String cola,
		@NotBlank(message = "La routing key no puede estar vacía") String routingKey) {
}
