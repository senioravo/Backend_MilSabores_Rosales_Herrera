package com.milsabores.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(
		@NotBlank(message = "El nombre del exchange no puede estar vacío") String nombre,
		@NotBlank(message = "El tipo de exchange no puede estar vacío")
		@Pattern(
				regexp = "(?i)direct|topic|fanout|headers",
				message = "Tipo de exchange inválido. Use direct, topic, fanout o headers")
		String tipo,
		Boolean durable) {
}
