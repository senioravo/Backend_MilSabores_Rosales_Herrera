package com.milsabores.admin.service;

import com.milsabores.admin.exception.DatoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

	@Mock
	private RabbitAdmin rabbitAdmin;

	private RabbitAdminService service;

	@BeforeEach
	void setUp() {
		service = new RabbitAdminService(rabbitAdmin);
	}

	@Test
	void nombreVacioNoCreaCola() {
		assertThrows(DatoInvalidoException.class, () -> service.crearCola("   ", true));
		verifyNoInteractions(rabbitAdmin);
	}

	@Test
	void nombreAmqNoCreaExchange() {
		assertThrows(DatoInvalidoException.class, () -> service.crearExchange("amq.direct", "direct", true));
		verifyNoInteractions(rabbitAdmin);
	}

	@Test
	void tipoInvalidoNoCreaExchange() {
		assertThrows(DatoInvalidoException.class, () -> service.crearExchange("prueba.exchange", "correo", true));
		verifyNoInteractions(rabbitAdmin);
	}

	@Test
	void routingKeyVaciaNoCreaBinding() {
		assertThrows(DatoInvalidoException.class, () -> service.crearBinding("prueba.exchange", "prueba.cola", " "));
		verifyNoInteractions(rabbitAdmin);
	}

	@Test
	void colaValidaSeDeclara() {
		service.crearCola("prueba.cola", true);
		verify(rabbitAdmin).declareQueue(org.mockito.ArgumentMatchers.any(Queue.class));
	}

	@Test
	void exchangeValidoSeDeclara() {
		service.crearExchange("prueba.exchange", "topic", true);
		verify(rabbitAdmin).declareExchange(org.mockito.ArgumentMatchers.any(Exchange.class));
	}

	@Test
	void bindingValidoSeDeclara() {
		service.crearBinding("prueba.exchange", "prueba.cola", "pedido.prueba");
		verify(rabbitAdmin).declareBinding(org.mockito.ArgumentMatchers.any(Binding.class));
	}
}
