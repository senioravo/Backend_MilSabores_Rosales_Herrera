package com.milsabores.admin.controller;

import com.milsabores.admin.dto.BindingRequest;
import com.milsabores.admin.dto.ColaRequest;
import com.milsabores.admin.dto.ExchangeRequest;
import com.milsabores.admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class RabbitAdminController {

	private final RabbitAdminService rabbitAdminService;

	public RabbitAdminController(RabbitAdminService rabbitAdminService) {
		this.rabbitAdminService = rabbitAdminService;
	}

	@PostMapping("/colas")
	public ResponseEntity<Void> crearCola(@Valid @RequestBody ColaRequest request) {
		rabbitAdminService.crearCola(request.nombre(), request.durable());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("/colas/{nombre:.+}")
	public ResponseEntity<Void> eliminarCola(@PathVariable String nombre) {
		rabbitAdminService.eliminarCola(nombre);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/exchanges")
	public ResponseEntity<Void> crearExchange(@Valid @RequestBody ExchangeRequest request) {
		rabbitAdminService.crearExchange(request.nombre(), request.tipo(), request.durable());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("/exchanges/{nombre:.+}")
	public ResponseEntity<Void> eliminarExchange(@PathVariable String nombre) {
		rabbitAdminService.eliminarExchange(nombre);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/bindings")
	public ResponseEntity<Void> crearBinding(@Valid @RequestBody BindingRequest request) {
		rabbitAdminService.crearBinding(request.exchange(), request.cola(), request.routingKey());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping("/bindings")
	public ResponseEntity<Void> eliminarBinding(@Valid @RequestBody BindingRequest request) {
		rabbitAdminService.eliminarBinding(request.exchange(), request.cola(), request.routingKey());
		return ResponseEntity.noContent().build();
	}
}
