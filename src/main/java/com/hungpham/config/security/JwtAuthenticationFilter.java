package com.hungpham.config.security;

import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.UserRepository;
import com.hungpham.service.auth.TokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final UuidBinaryMapper uuidBinaryMapper;

    public JwtAuthenticationFilter(TokenService tokenService,
                                   UserRepository userRepository,
                                   UuidBinaryMapper uuidBinaryMapper) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.uuidBinaryMapper = uuidBinaryMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String userId = tokenService.extractUserId(token);
            UserEntity user = userRepository.findById(uuidBinaryMapper.toBytes(userId)).orElse(null);
            if (user != null && user.isActive() && user.getRole() != null) {
                AuthenticatedUser principal = new AuthenticatedUser(
                        userId,
                        user.getEmail(),
                        user.getRole()
                );
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
