package com.hungpham.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servlet filter that enforces X-Internal-Proxy-Key on protected endpoints.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class InternalProxyKeyFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(InternalProxyKeyFilter.class);
    private static final String HEADER = "X-Internal-Proxy-Key";

    @Value("${app.internal-proxy-key:}")
    private String expectedKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Always allow CORS preflight requests.
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        if (!requiresProxyKey(path)) {
            chain.doFilter(request, response);
            return;
        }

        // If no key configured, skip check for local/dev convenience.
        if (expectedKey == null || expectedKey.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String incoming = req.getHeader(HEADER);
        if (incoming == null || !incoming.equals(expectedKey)) {
            log.warn("[ProxyKeyFilter] Rejected request to {} - invalid or missing proxy key", path);
            writeError(res, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Invalid or missing proxy key");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean requiresProxyKey(String path) {
        if (isPublicPath(path)) {
            return false;
        }
        return path.equals("/api/auth")
                || path.startsWith("/api/auth/")
                || path.startsWith("/api/admin/");
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/public/")
                || path.equals("/api/health")
                || path.equals("/health");
    }

    private void writeError(HttpServletResponse res, int status, String errorCode, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", errorCode);
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
