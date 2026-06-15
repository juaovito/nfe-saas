package br.com.nfesaas.security;

import java.util.UUID;

/**
 * Guarda o empresaId do tenant autenticado via ThreadLocal.
 * Garantia de isolamento multi-tenant por requisição HTTP.
 */
public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    public static void set(UUID empresaId) { CURRENT_TENANT.set(empresaId); }
    public static UUID get() { return CURRENT_TENANT.get(); }
    public static void clear() { CURRENT_TENANT.remove(); }
}
