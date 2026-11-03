package com.milsabores.admin.service;

import com.milsabores.admin.exception.DatoInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

@Service
public class RabbitAdminService {

	private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);
	private static final Set<String> TIPOS = Set.of("direct", "topic", "fanout", "headers");
	private static final int MAX_BYTES = 255;

	private final RabbitAdmin rabbitAdmin;

	public RabbitAdminService(RabbitAdmin rabbitAdmin) {
		this.rabbitAdmin = rabbitAdmin;
	}

	public void crearCola(String nombre, Boolean durable) {
		String limpio = exigirNombre(nombre, "la cola");
		Queue cola = esDurable(durable)
				? QueueBuilder.durable(limpio).build()
				: QueueBuilder.nonDurable(limpio).build();
		rabbitAdmin.declareQueue(cola);
		log.info("Cola creada: {}", limpio);
	}

	public void eliminarCola(String nombre) {
		String limpio = exigirNombre(nombre, "la cola");
		rabbitAdmin.deleteQueue(limpio);
		log.info("Cola eliminada: {}", limpio);
	}

	public void crearExchange(String nombre, String tipo, Boolean durable) {
		String limpio = exigirNombre(nombre, "el exchange");
		String tipoLimpio = exigirTipo(tipo);
		rabbitAdmin.declareExchange(exchange(limpio, tipoLimpio, esDurable(durable)));
		log.info("Exchange creado: {} ({})", limpio, tipoLimpio);
	}

	public void eliminarExchange(String nombre) {
		String limpio = exigirNombre(nombre, "el exchange");
		rabbitAdmin.deleteExchange(limpio);
		log.info("Exchange eliminado: {}", limpio);
	}

	public void crearBinding(String exchange, String cola, String routingKey) {
		Binding binding = binding(exchange, cola, routingKey);
		rabbitAdmin.declareBinding(binding);
		log.info("Binding creado: {} -> {} con clave {}", binding.getExchange(), binding.getDestination(), binding.getRoutingKey());
	}

	public void eliminarBinding(String exchange, String cola, String routingKey) {
		Binding binding = binding(exchange, cola, routingKey);
		rabbitAdmin.removeBinding(binding);
		log.info("Binding eliminado: {} -> {} con clave {}", binding.getExchange(), binding.getDestination(), binding.getRoutingKey());
	}

	private Binding binding(String exchange, String cola, String routingKey) {
		String exchangeLimpio = exigirNombre(exchange, "el exchange");
		String colaLimpia = exigirNombre(cola, "la cola");
		String clave = exigirClave(routingKey);
		return new Binding(colaLimpia, Binding.DestinationType.QUEUE, exchangeLimpio, clave, Map.of());
	}

	private static Exchange exchange(String nombre, String tipo, boolean durable) {
		return switch (tipo) {
			case "direct" -> new DirectExchange(nombre, durable, false);
			case "topic" -> new TopicExchange(nombre, durable, false);
			case "fanout" -> new FanoutExchange(nombre, durable, false);
			case "headers" -> new HeadersExchange(nombre, durable, false);
			default -> throw new DatoInvalidoException("Tipo de exchange inválido. Use direct, topic, fanout o headers");
		};
	}

	private static boolean esDurable(Boolean durable) {
		return durable == null || durable;
	}

	private static String exigirNombre(String nombre, String campo) {
		String limpio = exigirTexto(nombre, "El nombre de " + campo);
		if (limpio.startsWith("amq.")) {
			throw new DatoInvalidoException("El nombre de " + campo + " no puede empezar con amq.");
		}
		return limpio;
	}

	private static String exigirClave(String routingKey) {
		return exigirTexto(routingKey, "La routing key");
	}

	private static String exigirTexto(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new DatoInvalidoException(campo + " no puede quedar en blanco");
		}
		String limpio = valor.trim();
		if (limpio.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
			throw new DatoInvalidoException(campo + " supera los 255 bytes");
		}
		return limpio;
	}

	private static String exigirTipo(String tipo) {
		if (tipo == null || tipo.isBlank()) {
			throw new DatoInvalidoException("El tipo de exchange no puede estar vacío");
		}
		String limpio = tipo.trim().toLowerCase();
		if (!TIPOS.contains(limpio)) {
			throw new DatoInvalidoException("Tipo de exchange inválido. Use direct, topic, fanout o headers");
		}
		return limpio;
	}
}
