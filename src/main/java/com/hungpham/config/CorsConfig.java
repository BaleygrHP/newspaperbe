package com.hungpham.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    @Value("${APP_CORS_ALLOWED_ORIGINS:}")
    private String allowedOriginsRaw;

    private List<String> resolveAllowedOrigins() {
        if (allowedOriginsRaw != null && !allowedOriginsRaw.trim().isEmpty()) {
            List<String> parsed = Arrays.stream(allowedOriginsRaw.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toList());
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }
        return Arrays.asList(
                "https://newsroombaleygr.com",
                "https://www.newsroombaleygr.com"
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(resolveAllowedOrigins());
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(Arrays.asList("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        return source;
    }
}
