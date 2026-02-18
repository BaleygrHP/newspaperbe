package com.hungpham.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hungpham.common.enums.ErrorCodeEnum;
import com.hungpham.dtos.ErrorMessageResponseDto;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorMessageResponseDto body = new ErrorMessageResponseDto(
                ErrorCodeEnum.AUTHORIZATION_FAILED,
                "Access denied"
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
