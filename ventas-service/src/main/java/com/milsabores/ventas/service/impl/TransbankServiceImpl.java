package com.milsabores.ventas.service.impl;

import com.milsabores.ventas.dto.TransbankCommitResponseDTO;
import com.milsabores.ventas.dto.TransbankCreateRequestDTO;
import com.milsabores.ventas.dto.TransbankCreateResponseDTO;
import com.milsabores.ventas.service.TransbankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransbankServiceImpl implements TransbankService {
    
    private final WebClient transbankWebClient;
    
    @Value("${transbank.return.url}")
    private String returnUrl;
    
    @Override
    public TransbankCreateResponseDTO crearTransaccion(Long ventaId, Double amount, String customReturnUrl) {
        try {
            String buyOrder = "OC_" + ventaId + "_" + System.currentTimeMillis();
            String sessionId = "SID_" + ventaId;
            String finalReturnUrl = customReturnUrl != null ? customReturnUrl : returnUrl;
            
            TransbankCreateRequestDTO request = TransbankCreateRequestDTO.builder()
                .buyOrder(buyOrder)
                .sessionId(sessionId)
                .amount(amount)
                .returnUrl(finalReturnUrl)
                .build();
            
            log.info("Creando transacción Transbank: buyOrder={}, amount={}", buyOrder, amount);
            
            TransbankCreateResponseDTO response = transbankWebClient
                .post()
                .uri("/rswebpaytransaction/api/webpay/v1.2/transactions")
                .body(Mono.just(request), TransbankCreateRequestDTO.class)
                .retrieve()
                .bodyToMono(TransbankCreateResponseDTO.class)
                .block();
            
            log.info("Transacción creada exitosamente: token={}, url={}", response.getToken(), response.getUrl());
            return response;
            
        } catch (Exception e) {
            log.error("Error al crear transacción en Transbank", e);
            throw new RuntimeException("Error al comunicarse con Transbank: " + e.getMessage(), e);
        }
    }
    
    @Override
    public TransbankCommitResponseDTO confirmarTransaccion(String token) {
        try {
            log.info("Confirmando transacción con token: {}", token);
            
            TransbankCommitResponseDTO response = transbankWebClient
                .put()
                .uri("/rswebpaytransaction/api/webpay/v1.2/transactions/{token}", token)
                .retrieve()
                .bodyToMono(TransbankCommitResponseDTO.class)
                .block();
            
            log.info("Transacción confirmada: responseCode={}, authorizationCode={}", 
                response.getResponseCode(), response.getAuthorizationCode());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al confirmar transacción en Transbank", e);
            throw new RuntimeException("Error al confirmar transacción: " + e.getMessage(), e);
        }
    }
}
