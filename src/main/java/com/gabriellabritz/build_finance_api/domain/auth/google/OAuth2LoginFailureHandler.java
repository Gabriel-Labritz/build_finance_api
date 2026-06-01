package com.gabriellabritz.build_finance_api.domain.auth.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    public OAuth2LoginFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        ProblemDetail problemDetail;

        if (exception instanceof UsernameNotFoundException) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            problemDetail = ProblemDetail
                    .forStatusAndDetail(HttpStatus.NOT_FOUND, "O usuário não foi encontrado. Por favor, realize seu cadastro.");

            problemDetail.setTitle("Usuário não encontrado.");
        } else if (exception instanceof DisabledException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            problemDetail = ProblemDetail
                    .forStatusAndDetail(HttpStatus.FORBIDDEN, "Sua conta ainda não foi verificada. Por favor, verifique seu e-mail.");

            problemDetail.setTitle("Conta não verificada.");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Falha na autenticação via Google. Tente novamente."
            );

            problemDetail.setTitle("Erro de autenticação.");
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
