package com.gabriellabritz.build_finance_api.domain.auth.account_verification;

import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.InvalidVerificationTokenException;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.UserAlreadyVerifiedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountVerificationServiceTest {
    @InjectMocks
    private AccountVerificationService accountVerificationService;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private EmailVerificationToken emailVerificationToken;

    @Mock
    private User user;

    @Nested
    class verifyAccount {
        @Test
        @DisplayName("Deve lançar a exceção InvalidVerificationTokenException para um token inválido")
        void shouldThrowInvalidVerificationTokenExceptionWhenTokenIsInvalid() {
            // Arrange
            when(emailVerificationTokenRepository.findByToken("token-invalid")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(InvalidVerificationTokenException.class, () -> accountVerificationService.verifyAccount("token-invalid"));
            verify(emailVerificationTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidVerificationTokenException para um token expirado")
        void shouldThrowInvalidVerificationTokenExceptionWhenTokenIsExpired() {
            // Arrange
            when(emailVerificationTokenRepository.findByToken("token-expired")).thenReturn(Optional.of(emailVerificationToken));
            doThrow(new InvalidVerificationTokenException("Token de verificação expirado."))
                    .when(emailVerificationToken).validate();

            // Act + Assert
            assertThrows(InvalidVerificationTokenException.class, () -> accountVerificationService.verifyAccount("token-expired"));
            verify(emailVerificationTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção UserAlreadyVerifiedException para um usuário já verificado")
        void shouldThrowUserAlreadyVerifiedExceptionWhenUserAlreadyVerified() {
            // Arrange
            when(emailVerificationTokenRepository.findByToken("token-valid")).thenReturn(Optional.of(emailVerificationToken));
            when(emailVerificationToken.getUser()).thenReturn(user);
            doThrow(new UserAlreadyVerifiedException("O usuário já foi verificado."))
                    .when(user).verify();

            // Act + Assert
            assertThrows(UserAlreadyVerifiedException.class, () -> accountVerificationService.verifyAccount("token-valid"));
            verify(emailVerificationTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve verificar o usuário e deletar o token quando o token é válido.")
        void shouldVerifyUserAndDeleteTokenWhenValid() {
            // Arrange
            when(emailVerificationTokenRepository.findByToken("token-valid")).thenReturn(Optional.of(emailVerificationToken));
            when(emailVerificationToken.getUser()).thenReturn(user);

            // Act
            accountVerificationService.verifyAccount("token-valid");

            // Assert
            verify(emailVerificationToken).validate();
            verify(user).verify();
            verify(emailVerificationTokenRepository).delete(emailVerificationToken);
        }
    }
}