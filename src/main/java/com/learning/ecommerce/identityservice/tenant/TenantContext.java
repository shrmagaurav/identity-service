package com.learning.ecommerce.identityservice.tenant;

import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    private static final ThreadLocal<String> tenantContext = new ThreadLocal<>();
    private static final String DEFAULT_TENANT = "devel";

    public static void setTenantId(String tenantId) {
        tenantContext.set(tenantId);
    }

    public static String getTenantId() {
        return tenantContext.get() != null ? tenantContext.get() : DEFAULT_TENANT;
    }

    public static void clear() {
        tenantContext.remove();
    }
}