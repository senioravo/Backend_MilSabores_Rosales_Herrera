package com.milsabores.kafkaadmin.controller;

import com.milsabores.kafkaadmin.dto.GrupoResponse;
import com.milsabores.kafkaadmin.dto.TopicRequest;
import com.milsabores.kafkaadmin.dto.TopicResponse;
import com.milsabores.kafkaadmin.dto.TopicUpdateRequest;
import com.milsabores.kafkaadmin.service.KafkaAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kafka-admin")
public class KafkaAdminController {

	private final KafkaAdminService kafkaAdminService;

	public KafkaAdminController(KafkaAdminService kafkaAdminService) {
		this.kafkaAdminService = kafkaAdminService;
	}

	@GetMapping("/topics")
	public List<TopicResponse> listarTopics() {
		return kafkaAdminService.listarTopics();
	}

	@PostMapping("/topics")
	public ResponseEntity<Void> crearTopic(@Valid @RequestBody TopicRequest request) {
		kafkaAdminService.crear(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/topics/{nombre:.+}")
	public ResponseEntity<Void> actualizarTopic(
			@PathVariable String nombre,
			@Valid @RequestBody TopicUpdateRequest request) {
		kafkaAdminService.actualizar(nombre, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/topics/{nombre:.+}")
	public ResponseEntity<Void> eliminarTopic(@PathVariable String nombre) {
		kafkaAdminService.eliminar(nombre);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/grupos")
	public List<GrupoResponse> listarGrupos() {
		return kafkaAdminService.listarGrupos();
	}
}
