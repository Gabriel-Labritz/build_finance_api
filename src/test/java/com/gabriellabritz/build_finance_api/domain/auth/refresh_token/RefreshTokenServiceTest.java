package com.gabriellabritz.build_finance_api.domain.auth.refresh_token;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.dtos.RefreshTokenRequestDto;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private User user;

    @Mock
    private RefreshToken refreshToken;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;

    private RefreshTokenRequestDto refreshTokenRequestDto;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        this.refreshTokenRequestDto = new RefreshTokenRequestDto("refresh-token");
    }

    @Nested
    class createAndSave {
        @Test
        @DisplayName("Deve gerar um refresh token")
        void shouldGenerateARefreshToken() {
            // Arrange
            String refreshToken = "refresh-token";
            when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

            // Act
            refreshTokenService.createAndSave(user);

            // Assert
            verify(jwtService).generateRefreshToken(user);
        }

        @Test
        @DisplayName("Deve salvar o refresh token gerado")
        void shouldSaveRefreshTokenGenerated() {
            // Arrange
            String refreshToken = "refresh-token";
            long expirationSeconds = 3600L;
            when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
            when(jwtService.getRefreshTokenExpiration()).thenReturn(expirationSeconds);
            when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Act
            refreshTokenService.createAndSave(user);

            // Assert
            verify(jwtService).generateRefreshToken(user);
            verify(refreshTokenRepository).save(refreshTokenArgumentCaptor.capture());

            RefreshToken captured = refreshTokenArgumentCaptor.getValue();
            assertEquals(refreshToken, captured.getRefreshToken());
            assertEquals(user, captured.getUser());
            assertNotNull(captured.getExpiresAt());
        }
    }

    @Nested
    class getValidRefreshToken {
        @Test
        @DisplayName("Deve lançar a exceção InvalidRefreshTokenException quando o refresh token não é encontrado")
        void shouldThrowInvalidRefreshTokenExceptionWhenRefreshTokenNotFound() {
            // Arrange
            String invalidRefreshToken = "invalid-refresh-token";
            when(refreshTokenRepository.findByRefreshToken(invalidRefreshToken)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(InvalidRefreshTokenException.class,
                    () -> refreshTokenService.getValidRefreshToken(invalidRefreshToken));
            verify(refreshToken, never()).isExpired();
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidRefreshTokenException quando o refresh token está expirado")
        void shouldThrowInvalidRefreshTokenExceptionWhenRefreshTokenIsExpired() {
            // Arrange
            String expiredRefreshToken = "expired-refresh-token";
            when(refreshTokenRepository.findByRefreshToken(expiredRefreshToken)).thenReturn(Optional.of(refreshToken));
            when(refreshToken.isExpired()).thenReturn(true);
            when(refreshToken.getRefreshToken()).thenReturn(expiredRefreshToken);

            // Act + Assert
            assertThrows(InvalidRefreshTokenException.class,
                    () -> refreshTokenService.getValidRefreshToken(expiredRefreshToken));
            verify(refreshTokenRepository).findByRefreshToken(expiredRefreshToken);
            verify(refreshTokenRepository).deleteByRefreshToken(refreshToken.getRefreshToken());
        }

        @Test
        @DisplayName("Deve retornar o refresh token ")
        void shouldReturnRefreshToken() {
            // Arrange
            String refreshTokenValid = "refresh-token-valid";
            when(refreshTokenRepository.findByRefreshToken(refreshTokenValid))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshToken.isExpired()).thenReturn(false);

            // Act + Assert
            RefreshToken result = refreshTokenService.getValidRefreshToken(refreshTokenValid);

            verify(refreshTokenRepository).findByRefreshToken(refreshTokenValid);
            verify(refreshToken).isExpired();
            verify(refreshTokenRepository, never()).deleteByRefreshToken(any());
            assertNotNull(result);
            assertEquals(refreshToken, result);
        }
    }

    @Nested
    class removeToken {
        @Test
        @DisplayName("Deve remover o refresh token")
        void shouldRemoveRefreshToken() {
            // Arrange
            String tokenRefresh = "refresh-token";

            // Act
            refreshTokenService.removeToken(tokenRefresh);

            // Assert
            verify(refreshTokenRepository).deleteByRefreshToken(tokenCaptor.capture());

            String tokenCaptured = tokenCaptor.getValue();
            assertEquals(tokenRefresh, tokenCaptured);
        }
    }

    @Nested
    class generateNewRefreshToken {
        @Test
        @DisplayName("Deve lançar a exceção InvalidRefreshTokenException quando o refresh token informado não existe")
        void shouldThrowInvalidRefreshTokenExceptionWhenRefreshTokenNotFound() {
            // Arrange
            when(refreshTokenRepository.findByRefreshToken(refreshTokenRequestDto.refreshToken()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(InvalidRefreshTokenException.class,
                    () -> refreshTokenService.generateNewRefreshToken(refreshTokenRequestDto));
            verify(jwtService, never()).verifyToken(refreshTokenRequestDto.refreshToken());
            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(refreshTokenRepository, never()).deleteByRefreshToken(any());
            verify(jwtService, never()).generateAccessToken(any());
            verify(jwtService, never()).generateRefreshToken(any());
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidRefreshTokenException quando o refresh token informado está expirado")
        void shouldThrowInvalidRefreshTokenExceptionWhenRefreshTokenIsExpired() {
            // Arrange
            when(refreshTokenRepository.findByRefreshToken(refreshTokenRequestDto.refreshToken()))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshToken.isExpired()).thenReturn(true);

            // Act + Assert
            assertThrows(InvalidRefreshTokenException.class,
                    () -> refreshTokenService.generateNewRefreshToken(refreshTokenRequestDto));
            verify(refreshToken).isExpired();
            verify(refreshTokenRepository).deleteByRefreshToken(any());
            verify(jwtService, never()).verifyToken(refreshTokenRequestDto.refreshToken());
            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(jwtService, never()).generateAccessToken(any());
            verify(jwtService, never()).generateRefreshToken(any());
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção UsernameNotFoundException quando o usuário não é encontrado")
        void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
            // Arrange
            String userEmailNotExists = "usernotexists@email.com";

            when(refreshTokenRepository.findByRefreshToken(refreshTokenRequestDto.refreshToken()))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshToken.isExpired()).thenReturn(false);
            when(refreshToken.getRefreshToken()).thenReturn(refreshTokenRequestDto.refreshToken());
            when(jwtService.verifyToken(refreshTokenRequestDto.refreshToken())).thenReturn(userEmailNotExists);
            when(userRepository.findByEmailIgnoreCase(userEmailNotExists)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(UsernameNotFoundException.class,
                    () -> refreshTokenService.generateNewRefreshToken(refreshTokenRequestDto));
            verify(refreshToken).isExpired();
            verify(jwtService).verifyToken(refreshTokenRequestDto.refreshToken());
            verify(userRepository).findByEmailIgnoreCase(userEmailNotExists);
            verify(refreshTokenRepository, never()).deleteByRefreshToken(any());
            verify(jwtService, never()).generateAccessToken(any());
            verify(jwtService, never()).generateRefreshToken(any());
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve remover o refresh token anterior")
        void shouldRemovePreviousRefreshToken() {
            // Arrange
            RefreshToken oldRefreshToken = mock(RefreshToken.class);
            RefreshToken newRefreshToken = mock(RefreshToken.class);
            String userEmail = "user@email.com";

            when(refreshTokenRepository.findByRefreshToken(refreshTokenRequestDto.refreshToken()))
                    .thenReturn(Optional.of(oldRefreshToken));
            when(oldRefreshToken.isExpired()).thenReturn(false);
            when(oldRefreshToken.getRefreshToken()).thenReturn(refreshTokenRequestDto.refreshToken());
            when(jwtService.verifyToken(refreshTokenRequestDto.refreshToken())).thenReturn(userEmail);
            when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
            when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
            when(refreshTokenRepository.save(any())).thenReturn(newRefreshToken);

            // Act
            refreshTokenService.generateNewRefreshToken(refreshTokenRequestDto);

            // Assert
            verify(refreshTokenRepository).deleteByRefreshToken(tokenCaptor.capture());

            String tokenCaptured = tokenCaptor.getValue();
            assertEquals(refreshTokenRequestDto.refreshToken(), tokenCaptured);
        }

        @Test
        @DisplayName("Deve gerar novos access token e refresh token")
        void shouldGenerateNewAccessAndRefreshTokens() {
            // Arrange
            RefreshToken oldRefreshToken = mock(RefreshToken.class);
            RefreshToken newRefreshToken = mock(RefreshToken.class);
            String userEmail = "user@email.com";

            when(refreshTokenRepository.findByRefreshToken(refreshTokenRequestDto.refreshToken()))
                    .thenReturn(Optional.of(oldRefreshToken));
            when(oldRefreshToken.isExpired()).thenReturn(false);
            when(oldRefreshToken.getRefreshToken()).thenReturn(refreshTokenRequestDto.refreshToken());
            when(jwtService.verifyToken(refreshTokenRequestDto.refreshToken())).thenReturn(userEmail);
            when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
            when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
            when(refreshTokenRepository.save(any())).thenReturn(newRefreshToken);
            when(newRefreshToken.getRefreshToken()).thenReturn("new-refresh-token");

            // Act
            AuthLoginResponseDto result = refreshTokenService.generateNewRefreshToken(refreshTokenRequestDto);

            // Assert
            assertNotNull(result);
            assertEquals("new-access-token", result.accessToken());
            assertEquals("new-refresh-token", result.refreshToken());
        }
    }
}