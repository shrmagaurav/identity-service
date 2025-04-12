package com.learning.ecommerce.identityservice;

import com.learning.ecommerce.identityservice.tenant.TenantContext;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 🔹 Extract TenantID from request headers
        String tenantId = request.getHeader("TenantID");
        if (StringUtils.isEmpty(tenantId)) {
            tenantId = TenantContext.getTenantId(); // Use default if missing
        }
        TenantContext.setTenantId(tenantId); // Store in ThreadLocal for later use

        // 🔹 Extract JWT Token (First from Cookie, then from Authorization Header)
        String token = extractJwtFromCookie(request);  // ✅ Try to get token from cookies
        if (token == null) {
            token = extractJwtFromHeader(request);  // ✅ Fallback to Authorization header
        }

        String username = null;

        // 🔹 Validate JWT Token
        if (token != null) {
            try {
                username = this.jwtHelper.getUsernameFromToken(token);
            } catch (Exception e) {
                logger.error("Invalid JWT token", e);
            }
        } else {
            logger.info("No valid JWT token found");
        }

        // 🔹 Authenticate User
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtHelper.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.info("JWT validation failed");
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // Clean up to prevent memory leaks
        }
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue(); // ✅ Return JWT from cookie
                }
            }
        }
        return null; // ❌ No JWT cookie found
    }

    private String extractJwtFromHeader(HttpServletRequest request) {
        String requestHeader = request.getHeader("Authorization");
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            return requestHeader.substring(7); // ✅ Return JWT from Authorization header
        }
        return null; // ❌ No valid Authorization header found
    }


}
