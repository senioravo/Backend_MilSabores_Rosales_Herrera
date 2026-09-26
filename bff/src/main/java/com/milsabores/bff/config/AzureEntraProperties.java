package com.milsabores.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.entra")
public class AzureEntraProperties {

    private boolean enabled = false;
    private String tenantId = "";
    private String clientId = "";
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
