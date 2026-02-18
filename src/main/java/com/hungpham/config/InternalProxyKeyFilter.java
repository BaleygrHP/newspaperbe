package com.hungpham.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servlet filter that enforces the X-Internal-Proxy-Key header on admin and
 * protected auth endpoints. Public endpoints pass through freely.
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

        String path = req.getRequestURI();
        // Strip context path if present
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        // Public endpoints – no proxy key needed
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // If no key configured, skip check (dev convenience)
        if (expectedKey == null || expectedKey.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        // Check proxy key
        String incoming = req.getHeader(HEADER);
        if (incoming == null || !incoming.equals(expectedKey)) {
            log.warn("[ProxyKeyFilter] Rejected request to {} – invalid or missing proxy key", path);
            writeError(res, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Invalid or missing proxy key");
            return;
        }

        chain.doFilter(request, response);
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
