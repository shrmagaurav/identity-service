package com.learning.ecommerce.identityservice.tenant;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

//@Component
//@WebFilter("/*")
//public class TenantIDFilter implements Filter {
//
//    private static final String TENANT_HEADER = "TenantID";
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        String tenantId = httpRequest.getHeader(TENANT_HEADER);
//
//        if (StringUtils.isEmpty(tenantId)) {
//            tenantId = TenantContext.getTenantId();  // Default if missing
//        }
//
//        TenantContext.setTenantId(tenantId);
//
//        try {
//            chain.doFilter(request, response);
//        } finally {
//            TenantContext.clear();  // Clean up to prevent leaks
//        }
//    }
//}
