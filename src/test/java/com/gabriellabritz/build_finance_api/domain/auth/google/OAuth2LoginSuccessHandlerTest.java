package com.gabriellabritz.build_finance_api.domain.auth.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshToken;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshTokenService;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {
    @InjectMocks
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Mock
    private UserRepository userRepository;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private OidcUser oidcUser;

    private Authentication authentication;

    private User user;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private RefreshToken refreshToken;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.oidcUser = mock(OidcUser.class);
        this.authentication = mock(Authentication.class);
        this.user = mock(User.class);
        this.refreshToken = mock(RefreshToken.class);
    }

    @Nested
    class onAuthenticationSuccess {
        @Test
        @DisplayName("Deve retornar 200 com os tokens quando o usuário está ativo")
        void shouldReturn200StatusWithTokensWhenUserIsActive() throws ServletException, IOException {
            // Arrange
            String userEmail = "user.email@email.com";
            String accessToken = "access-token";
            String tokenRefresh = "refresh-token";

            when(oidcUser.getEmail()).thenReturn(userEmail);
            when(authentication.getPrincipal()).thenReturn(oidcUser);
            when(userRepository.findByEmailIgnoreCase(userEmail))
                    .thenReturn(Optional.of(user));
            when(user.isEnabled()).thenReturn(true);
            when(jwtService.generateAccessToken(user)).thenReturn(accessToken);
            when(refreshTokenService.createAndSave(user)).thenReturn(refreshToken);
            when(refreshToken.getRefreshToken()).thenReturn(tokenRefresh);

            // Act
            oAuth2LoginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

            // Assert
            assertNotNull(response.getContentType());
            assertTrue(response.getContentType().contains("application/json"));
            assertEquals("UTF-8", response.getCharacterEncoding());
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            assertNotNull(response.getContentAsString());

            AuthLoginResponseDto responseBody = objectMapper
                    .readValue(response.getContentAsString(), AuthLoginResponseDto.class);

            assertEquals(accessToken, responseBody.accessToken());
            assertEquals(tokenRefresh, responseBody.refreshToken());
        }

        @Test
        @DisplayName("Deve lançar a exceção UsernameNotFoundException quando o usuário não existe.")
        void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist()  {
            // Arrange
            String userEmail = "usernotexist.email@email.com";

            when(oidcUser.getEmail()).thenReturn(userEmail);
            when(authentication.getPrincipal()).thenReturn(oidcUser);
            when(userRepository.findByEmailIgnoreCase(userEmail))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(UsernameNotFoundException.class,
                    () -> oAuth2LoginSuccessHandler.onAuthenticationSuccess(request, response, authentication));
            verify(user, never()).isEnabled();
            verify(jwtService, never()).generateAccessToken(any());
            verify(refreshTokenService, never()).createAndSave(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção DisabledException quando o usuário não está verificado.")
        void shouldThrowDisabledExceptionWhenUserIsNotVerified()  {
            // Arrange
            String userEmail = "usernotverified.email@email.com";

            when(oidcUser.getEmail()).thenReturn(userEmail);
            when(authentication.getPrincipal()).thenReturn(oidcUser);
            when(userRepository.findByEmailIgnoreCase(userEmail))
                    .thenReturn(Optional.of(user));
            when(user.isEnabled()).thenReturn(false);

            // Act + Assert
            assertThrows(DisabledException.class,
                    () -> oAuth2LoginSuccessHandler.onAuthenticationSuccess(request, response, authentication));
            verify(userRepository).findByEmailIgnoreCase(userEmail);
            verify(user).isEnabled();
            verify(jwtService, never()).generateAccessToken(any());
            verify(refreshTokenService, never()).createAndSave(any());
        }
    }

}