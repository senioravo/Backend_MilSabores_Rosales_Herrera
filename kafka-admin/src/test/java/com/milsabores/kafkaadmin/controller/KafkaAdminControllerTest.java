package com.milsabores.kafkaadmin.controller;

import com.milsabores.kafkaadmin.dto.TopicResponse;
import com.milsabores.kafkaadmin.exception.GlobalExceptionHandler;
import com.milsabores.kafkaadmin.service.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = KafkaAdminController.class)
@Import(GlobalExceptionHandler.class)
class KafkaAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private KafkaAdminService kafkaAdminService;

	@Test
	void listarTopicsDevuelveParticionesYRetencion() throws Exception {
		when(kafkaAdminService.listarTopics()).thenReturn(List.of(
				new TopicResponse("milsabores.stock", 3, 3, "delete", "3600000")));

		mockMvc.perform(get("/api/kafka-admin/topics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("milsabores.stock"))
				.andExpect(jsonPath("$[0].particiones").value(3))
				.andExpect(jsonPath("$[0].retentionMs").value("3600000"));
	}

	@Test
	void unTopicSinNombreResponde400YNoCrea() throws Exception {
		mockMvc.perform(post("/api/kafka-admin/topics")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"  ","particiones":3,"factorReplicacion":3,"cleanupPolicy":"delete","retentionMs":3600000}
								"""))
				.andExpect(status().isBadRequest());
		verifyNoInteractions(kafkaAdminService);
	}
}
