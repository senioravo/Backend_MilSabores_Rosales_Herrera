package com.milsabores.admin.controller;

import com.milsabores.admin.exception.GlobalExceptionHandler;
import com.milsabores.admin.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RabbitAdminController.class)
@Import(GlobalExceptionHandler.class)
class RabbitAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RabbitAdminService rabbitAdminService;

	@Test
	void colaSinNombreResponde400YNoCrea() throws Exception {
		mockMvc.perform(post("/api/admin/colas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\":\"  \",\"durable\":true}"))
				.andExpect(status().isBadRequest());
		verifyNoInteractions(rabbitAdminService);
	}

	@Test
	void exchangeConTipoInvalidoResponde400YNoCrea() throws Exception {
		mockMvc.perform(post("/api/admin/exchanges")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\":\"prueba.exchange\",\"tipo\":\"correo\"}"))
				.andExpect(status().isBadRequest());
		verifyNoInteractions(rabbitAdminService);
	}

	@Test
	void bindingSinRoutingKeyResponde400YNoCrea() throws Exception {
		mockMvc.perform(post("/api/admin/bindings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"exchange\":\"prueba.exchange\",\"cola\":\"prueba.cola\",\"routingKey\":\"\"}"))
				.andExpect(status().isBadRequest());
		verifyNoInteractions(rabbitAdminService);
	}
}
