package com.milsabores.gateway.identity;

/**
 * Nombres de headers que el api-gateway inyecta hacia microservicios / BFF.
 * Convención EP1: identidad viene del token, no del body del cliente.
 */
public final class GatewayIdentityHeaders {

    public static final String AUTH_PROVIDER = "X-Auth-Provider";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_NAME = "X-User-Name";
    public static final String USER_ID = "X-User-Id";
    public static final String USER_ROLES = "X-User-Roles";

    public static final String ENTRA_OID = "X-Entra-Oid";
    public static final String ENTRA_SUB = "X-Entra-Sub";
    public static final String ENTRA_TENANT_ID = "X-Entra-Tenant-Id";
    public static final String ENTRA_SCOPES = "X-Entra-Scopes";

    public static final String PROVIDER_ENTRA = "entra";
    public static final String PROVIDER_LEGACY = "legacy";

    private GatewayIdentityHeaders() {}
}
