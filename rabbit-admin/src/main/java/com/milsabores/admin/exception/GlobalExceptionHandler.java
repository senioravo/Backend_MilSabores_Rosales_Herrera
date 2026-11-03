package com.milsabores.admin.exception;

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
		return ResponseEntity.badRequest().body(cuerpo(ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
		Map<String, String> errores = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String campo = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
			errores.put(campo, error.getDefaultMessage());
		});
		Map<String, Object> cuerpo = cuerpo("Hay datos vacíos o inválidos. No se creó nada.");
		cuerpo.put("errores", errores);
		return ResponseEntity.badRequest().body(cuerpo);
	}

	private static Map<String, Object> cuerpo(String mensaje) {
		Map<String, Object> cuerpo = new HashMap<>();
		cuerpo.put("status", HttpStatus.BAD_REQUEST.value());
		cuerpo.put("message", mensaje);
		cuerpo.put("timestamp", LocalDateTime.now());
		return cuerpo;
	}
}
