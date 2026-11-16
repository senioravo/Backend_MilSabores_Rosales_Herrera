package com.milsabores.kafkaadmin.exception;

import org.springframework.http.HttpStatus;

public class KafkaAdminException extends RuntimeException {

	private final HttpStatus status;

	public KafkaAdminException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus status() {
		return status;
	}
}
