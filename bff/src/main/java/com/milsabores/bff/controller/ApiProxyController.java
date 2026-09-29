package com.milsabores.bff.controller;

import com.milsabores.bff.proxy.DownstreamApiProxy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrada /api/** en el BFF: el gateway solo habla con el BFF; el BFF orquesta hacia microservicios.
 */
@RestController
public class ApiProxyController {

    private final DownstreamApiProxy downstreamApiProxy;

    public ApiProxyController(DownstreamApiProxy downstreamApiProxy) {
        this.downstreamApiProxy = downstreamApiProxy;
    }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return downstreamApiProxy.forward(request, body);
    }
}
