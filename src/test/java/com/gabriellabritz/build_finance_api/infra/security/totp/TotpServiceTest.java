package com.gabriellabritz.build_finance_api.infra.security.totp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TotpServiceTest {
    private TotpService totpService;

    @BeforeEach
    void setUp() {
        this.totpService = new TotpService();
    }

    @Nested
    class generateSecret {
        @Test
        @DisplayName("Deve retornar uma secret não nulo")
        void shoulReturnNonNullSecret() {
            // Arrange
            String secret = totpService.generateSecret();

            // Assert
            assertNotNull(secret);
            assertFalse(secret.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar diferentes secret a cada chamada")
        void shouldReturnDifferentSecretsOnEachCall() {
            // Arrange
            String firstSecret = totpService.generateSecret();
            String secondSecret = totpService.generateSecret();

            // Assert
            assertNotEquals(firstSecret, secondSecret);
        }
    }

    @Nested
    class generateQRCodeUri {
        private final String SECRET = "secret";
        private final String USER_EMAIL = "useremail@email.com";

        @Test
        @DisplayName("deve retornar URI com o scheme otpauth correto")
        void shouldReturnUriWithCorrectScheme() {
            // Act
            String result = totpService.generateQRCodeUri(SECRET, USER_EMAIL);

            // Assert
            assertTrue(result.startsWith("otpauth://totp/"));
        }

        @Test
        @DisplayName("deve conter o email do usuário na URI")
        void shouldContainUserEmailInUri() {
            // Act
            String result = totpService.generateQRCodeUri(SECRET, USER_EMAIL);

            // Assert
            assertTrue(result.contains("useremail%40email.com"));
        }

        @Test
        @DisplayName("deve conter a secret na URI")
        void shouldContainSecretInUri() {
            // Act
            String result = totpService.generateQRCodeUri(SECRET, USER_EMAIL);

            // Assert
            assertTrue(result.contains("secret=" + SECRET));
        }

        @Test
        @DisplayName("deve conter a issuer na URI")
        void shouldContainIssuerInUri() {
            // Act
            String result = totpService.generateQRCodeUri(SECRET, USER_EMAIL);

            // Assert
            assertTrue(result.contains("issuer=Build%20Finance%20"));
        }
    }

    @Nested
    class verifyCode {
        @Test
        @DisplayName("deve retornar false para um código inválido")
        void shouldReturnFalseForInvalidCode() {
            // Arrange
            String secret = totpService.generateSecret();

            // Act
            Boolean result = totpService.verifyCode("000000", secret);

            // Assert
            assertFalse(result);
        }
    }
}