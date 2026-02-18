package com.hungpham.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hungpham.common.enums.ErrorCodeEnum;
import com.hungpham.dtos.ErrorMessageResponseDto;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorMessageResponseDto body = new ErrorMessageResponseDto(
                ErrorCodeEnum.AUTHENTICATION_FAILED,
                "Authentication required"
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
