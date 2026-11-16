package com.milsabores.kafkaadmin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DatoInvalidoException.class)
	public ResponseEntity<Map<String, Object>> handleDatoInvalido(DatoInvalidoException ex) {
		return ResponseEntity.badRequest().body(cuerpo(HttpStatus.BAD_REQUEST, ex.getMessage()));
	}

	@ExceptionHandler(KafkaAdminException.class)
	public ResponseEntity<Map<String, Object>> handleKafka(KafkaAdminException ex) {
		return ResponseEntity.status(ex.status()).body(cuerpo(ex.status(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
		Map<String, String> errores = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String campo = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
			errores.put(campo, error.getDefaultMessage());
		});
		Map<String, Object> cuerpo = cuerpo(HttpStatus.BAD_REQUEST, "Hay datos vacíos o inválidos. No se cambió nada.");
		cuerpo.put("errores", errores);
		return ResponseEntity.badRequest().body(cuerpo);
	}

	private static Map<String, Object> cuerpo(HttpStatus status, String mensaje) {
		Map<String, Object> cuerpo = new HashMap<>();
		cuerpo.put("status", status.value());
		cuerpo.put("message", mensaje);
		cuerpo.put("timestamp", LocalDateTime.now());
		return cuerpo;
	}
}
