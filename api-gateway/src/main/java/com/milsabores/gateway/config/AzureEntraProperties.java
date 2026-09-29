package com.milsabores.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración Entra ID para validar access tokens del frontend (MSAL).
 * Paso 1 EP1: propiedades; el filtro las usará en pasos siguientes.
 */
@ConfigurationProperties(prefix = "azure.entra")
public class AzureEntraProperties {

    /**
     * Activa validación JWT de Microsoft (desactivado = solo JWT legacy usuario-service).
     */
    private boolean enabled = false;

    private String tenantId = "";

    /** Application (client) ID de la app SPA / API en Entra. */
    private String clientId = "";

    /**
     * Audience esperada en el access token (aud).
     * Suele ser {@code api://{client-id}} o el client id de la API expuesta.
     */
    private String audience = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    /** Issuer OIDC v2 del tenant (JWKS en login.microsoftonline.com). */
    public String getIssuerUri() {
        if (tenantId == null || tenantId.isBlank()) {
            return "";
        }
        return "https://login.microsoftonline.com/" + tenantId.trim() + "/v2.0";
    }

    public boolean isConfigured() {
        return enabled
                && tenantId != null && !tenantId.isBlank()
                && audience != null && !audience.isBlank();
    }
}
