package com.gabriellabritz.build_finance_api.domain.auth.jwt;

import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    private RefreshToken refreshTokenMock;

    @BeforeEach
    void setUp(){
    }

    @Nested
    class createAndSave {
        @Test
        @DisplayName("Deve gerar um novo refresh token")
        void shouldGenerateANewRefreshToken() {
            // Arrange
            String refreshToken = "new-refresh-token-generated";
            when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

            // Act
            refreshTokenService.createAndSave(user);

            // Assert
            verify(jwtService).generateRefreshToken(user);
        }

        @Test
        @DisplayName("Deve salvar um novo refresh token")
        void shouldSaveANewRefreshToken() {
            // Arrange
            String refreshToken = "new-refresh-token-generated";
            when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
            when(refreshTokenRepository.save(any()))
                    .thenAnswer(i -> i.getArgument(0));

            // Act
            RefreshToken result = refreshTokenService.createAndSave(user);

            // Assert
            verify(jwtService).generateRefreshToken(user);
            verify(refreshTokenRepository).save(argThat(token ->
                    token.getRefreshToken().equals(refreshToken) &&
                            token.getUser().equals(user)
            ));
            assertNotNull(result);
            assertEquals(refreshToken, result.getRefreshToken());
        }
    }

    @Nested
    class getValidRefreshToken {
        @Test
        @DisplayName("Deve lançar a exceção InvalidRefreshTokenException quando o refresh token não for encontrado")
        void shouldThrowInvalidRefreshTokenExceptionWhenRefreshTokenIsNotFound() {
            // Arrange
            String refreshTokenInvalid = "refresh-token-invalid";
            when(refreshTokenRepository.findByRefreshToken(refreshTokenInvalid))
                    .thenReturn(Optional.empty());

            // Act
            assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.getValidRefreshToken(refreshTokenInvalid));

            // Assert
            verify(refreshTokenMock, never()).isExpired();
        }

        @Test
        @DisplayName("Deve remover e lançar a exceção InvalidRefreshTokenException quando o refresh token está expirado")
        void shouldRemoveAndThrowInvalidRefreshTokenExceptionWhenRefreshTokenIsExpired() {
            // Arrange
            String refreshTokenExpired = "refresh-token-expired";
            when(refreshTokenRepository.findByRefreshToken(refreshTokenExpired))
                    .thenReturn(Optional.of(refreshTokenMock));
            when(refreshTokenMock.isExpired()).thenReturn(true);
            when(refreshTokenMock.getRefreshToken()).thenReturn(refreshTokenExpired);

            // Act
            assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.getValidRefreshToken(refreshTokenExpired));

            // Assert
            verify(refreshTokenRepository).deleteByRefreshToken(refreshTokenExpired);
        }

        @Test
        @DisplayName("Deve retornar o refresh token quando válido")
        void shouldReturnRefreshTokenWhenValid() {
            // Arrange
            String refreshTokenValid = "refresh-token-valid";
            when(refreshTokenRepository.findByRefreshToken(refreshTokenValid))
                    .thenReturn(Optional.of(refreshTokenMock));
            when(refreshTokenMock.isExpired()).thenReturn(false);

            // Act
            RefreshToken result = refreshTokenService.getValidRefreshToken(refreshTokenValid);

            // Assert
            verify(refreshTokenRepository).findByRefreshToken(refreshTokenValid);
            verify(refreshTokenMock).isExpired();
            verify(refreshTokenRepository, never()).deleteByRefreshToken(refreshTokenValid);
            assertNotNull(result);
            assertEquals(refreshTokenMock, result);
        }
    }

    @Nested
    class removeToken {
        @Test
        @DisplayName("Deve remover refresh token")
        void shouldRemoveRefreshToken() {
            // Arrange
            when(refreshTokenMock.getRefreshToken()).thenReturn("refresh-token");

            // Act
            refreshTokenService.removeToken(refreshTokenMock);

            // Assert
            verify(refreshTokenRepository).deleteByRefreshToken(refreshTokenMock.getRefreshToken());
        }
    }
}