package com.gabriellabritz.build_finance_api.domain.auth.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginFailureHandlerTest {
    @InjectMocks
    private OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Spy
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
    }

    @Nested
    class onAuthenticationFailure {
        @Test
        @DisplayName("Deve retornar 404 quando o usuário não for encontrado")
        void shouldReturn404WhenUserNotFound() throws ServletException, IOException {
            // Arrange
            UsernameNotFoundException exception = new UsernameNotFoundException("O usuário não foi encontrado. Por favor, realize seu cadastro.");

            // Act
            oAuth2LoginFailureHandler.onAuthenticationFailure(request, response, exception);

            // Assert
            assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());

            ProblemDetail responseBody = objectMapper
                    .readValue(response.getContentAsString(), ProblemDetail.class);

            assertEquals(HttpStatus.NOT_FOUND.value(), responseBody.getStatus());
            assertEquals("O usuário não foi encontrado. Por favor, realize seu cadastro.", responseBody.getDetail());
            assertEquals("Usuário não encontrado.", responseBody.getTitle());
        }

        @Test
        @DisplayName("Deve retornar 403 quando o usuário não está verificado")
        void shouldReturn403WhenUserIsNotVerified() throws ServletException, IOException {
            // Arrange
            DisabledException exception = new DisabledException("Sua conta ainda não foi verificada. Por favor, verifique seu e-mail.");

            // Act
            oAuth2LoginFailureHandler.onAuthenticationFailure(request, response, exception);

            // Assert
            assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());

            ProblemDetail responseBody = objectMapper
                    .readValue(response.getContentAsString(), ProblemDetail.class);

            assertEquals(HttpStatus.FORBIDDEN.value(), responseBody.getStatus());
            assertEquals("Sua conta ainda não foi verificada. Por favor, verifique seu e-mail.", responseBody.getDetail());
            assertEquals("Conta não verificada.", responseBody.getTitle());
        }

        @Test
        @DisplayName("Deve retornar 401 para qualquer outra falha de autenticação")
        void shouldReturn401WhenForGenericAuthenticationFailure() throws ServletException, IOException {
            // Arrange
            BadCredentialsException exception = new BadCredentialsException("Falha na autenticação via Google. Tente novamente.");

            // Act
            oAuth2LoginFailureHandler.onAuthenticationFailure(request, response, exception);

            // Assert
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

            ProblemDetail responseBody = objectMapper
                    .readValue(response.getContentAsString(), ProblemDetail.class);

            assertEquals(HttpStatus.UNAUTHORIZED.value(), responseBody.getStatus());
            assertEquals("Falha na autenticação via Google. Tente novamente.", responseBody.getDetail());
            assertEquals("Erro de autenticação.", responseBody.getTitle());
        }
    }
}