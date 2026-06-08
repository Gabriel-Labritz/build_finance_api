package com.gabriellabritz.build_finance_api.domain.auth.a2f;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.TwoFactorAuthRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.TwoFactorSetupResponse;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.InvalidA2FCodeException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorAuthAlreadyEnabledException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorAuthNotEnabledException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorSecretNotFoundException;
import com.gabriellabritz.build_finance_api.infra.security.totp.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthServiceTest {
    @InjectMocks
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private User user;

    @Mock
    private TwoFactorAuthRepository twoFactorAuthRepository;

    @Mock
    private TotpService totpService;

    @Mock
    private TwoFactorAuth twoFactorAuth;

    private TwoFactorAuthRequestDto twoFactorAuthRequestDto;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        this.twoFactorAuthRequestDto = new TwoFactorAuthRequestDto("123456");
    }

    @Nested
    class generateQRCode {
        @Test
        @DisplayName("Deve lançar a exceção TwoFactorAuthAlreadyEnabledException se a a2f está habilitada")
        void shouldThrowTwoFactorAuthAlreadyEnabledExceptionWhenA2FIsEnabled() {
            // Arrange
            when(user.getTwoFactorEnabled()).thenReturn(true);

            // Act + Assert
            assertThrows(TwoFactorAuthAlreadyEnabledException.class, () -> twoFactorAuthService.generateQRCode(user));
            verify(twoFactorAuthRepository, never()).findByUserId(any());
            verify(totpService, never()).generateSecret();
            verify(twoFactorAuthRepository, never()).save(twoFactorAuth);
            verify(totpService, never()).generateQRCodeUri(any(), any());
        }

        @Test
        @DisplayName("Deve deletar a secret anterior se já existe uma.")
        void shouldDeletePreviousSecret() {
            // Arrange
            when(user.getTwoFactorEnabled()).thenReturn(false);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.of(twoFactorAuth));
            when(twoFactorAuthRepository.save(any())).thenReturn(twoFactorAuth);

            // Act
            twoFactorAuthService.generateQRCode(user);

            // Assert
            verify(twoFactorAuthRepository).findByUserId(user.getId());
            verify(twoFactorAuthRepository).delete(twoFactorAuth);
            verify(twoFactorAuthRepository).flush();
        }

        @Test
        @DisplayName("Não deve deletar quando não há secret anterior")
        void shouldNotDeleteWhenNoExistPreviousSecret() {
            // Arrange
            when(user.getTwoFactorEnabled()).thenReturn(false);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
            when(twoFactorAuthRepository.save(any())).thenReturn(twoFactorAuth);

            // Act
            twoFactorAuthService.generateQRCode(user);

            // Assert
            verify(twoFactorAuthRepository).findByUserId(user.getId());
            verify(twoFactorAuthRepository, never()).delete(twoFactorAuth);
        }

        @Test
        @DisplayName("Deve retornar TwoFactorSetupResponse com a URI correta.")
        void shouldReturnSetupResponseWithQrUri() {
            // Arrange
            String secret = "secret";
            String qrCodeUri = "qrcodeuri";

            when(user.getTwoFactorEnabled()).thenReturn(false);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
            when(totpService.generateSecret()).thenReturn(secret);
            when(twoFactorAuthRepository.save(any())).thenReturn(twoFactorAuth);
            when(twoFactorAuth.getSecret()).thenReturn(secret);
            when(totpService.generateQRCodeUri(secret, user.getUsername())).thenReturn(qrCodeUri);

            // Act
            TwoFactorSetupResponse result = twoFactorAuthService.generateQRCode(user);

            // Assert
            assertEquals(qrCodeUri, result.qrCodeUrl());
            verify(user).getTwoFactorEnabled();
            verify(twoFactorAuthRepository).findByUserId(user.getId());
            verify(totpService).generateSecret();
            verify(totpService).generateQRCodeUri(twoFactorAuth.getSecret(), user.getUsername());
        }
    }

    @Nested
    class enableTwoFactorAuth {
        @Test
        @DisplayName("Deve lançar a exceção TwoFactorAuthAlreadyEnabledException quando a a2f já está habilitada")
        void shouldThrowTwoFactorAuthAlreadyEnabledExceptionWhenA2FIsEnabled() {
            // Arrange
            when(user.getTwoFactorEnabled()).thenReturn(true);

            // Act + Assert
            assertThrows(TwoFactorAuthAlreadyEnabledException.class,
                    () -> twoFactorAuthService.enableTwoFactorAuth(user, twoFactorAuthRequestDto));
            verify(user, never()).enableTwoFactor();
            verify(userRepository, never()).save(user);
        }

        @Test
        @DisplayName("Deve lançar a exceção TwoFactorSecretNotFoundException quando a secret não existe")
        void shouldThrowTwoFactorSecretNotFoundExceptionWhenUserSecretNotFound() {
            // Arrange
            when(user.getTwoFactorEnabled()).thenReturn(false);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(TwoFactorSecretNotFoundException.class,
                    () -> twoFactorAuthService.enableTwoFactorAuth(user, twoFactorAuthRequestDto));
            verify(user, never()).enableTwoFactor();
            verify(userRepository, never()).save(user);
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidA2FCodeException quando o código informado é inválido")
        void shouldThrowInvalidA2FCodeExceptionWhenCodeIsInvalid() {
            // Arrange
            String secret = "secret";

            when(user.getTwoFactorEnabled()).thenReturn(false);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.of(twoFactorAuth));
            when(twoFactorAuth.getSecret()).thenReturn(secret);
            when(totpService.verifyCode(twoFactorAuthRequestDto.code(), twoFactorAuth.getSecret())).thenReturn(false);

            // Act + Assert
            assertThrows(InvalidA2FCodeException.class,
                    () -> twoFactorAuthService.enableTwoFactorAuth(user, twoFactorAuthRequestDto));
            verify(user, never()).enableTwoFactor();
            verify(userRepository, never()).save(user);
        }

        @Test
        @DisplayName("Deve habilitar e salvar o usuário quando o código é válido")
        void shouldEnableAndSaveUserWhenCodeIsValid() {
            // Arrange
            String secret = "secret";

            when(user.getTwoFactorEnabled()).thenReturn(false);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.of(twoFactorAuth));
            when(twoFactorAuth.getSecret()).thenReturn(secret);
            when(totpService.verifyCode(twoFactorAuthRequestDto.code(), secret)).thenReturn(true);

            // Act
            twoFactorAuthService.enableTwoFactorAuth(user, twoFactorAuthRequestDto);

            verify(user).getTwoFactorEnabled();
            verify(user).enableTwoFactor();
            verify(userRepository).save(user);
        }
    }

    @Nested
    class disableTwoFactorAuth {
        @Test
        @DisplayName("Deve lançar a exceção TwoFactorAuthNotEnabledException quando a a2f não está habilitada")
        void shouldThrowTwoFactorAuthNotEnabledExceptionWhenA2FIsNotEnabled() {
            // Arrange
            when(user.getTwoFactorEnabled()).thenReturn(false);

            // Act + Assert
            assertThrows(TwoFactorAuthNotEnabledException.class,
                    () -> twoFactorAuthService.disableTwoFactorAuth(user, twoFactorAuthRequestDto));
            verify(user, never()).disableTwoFactor();
            verify(userRepository, never()).save(user);
            verify(twoFactorAuthRepository, never()).deleteByUserId(user.getId());
        }

        @Test
        @DisplayName("Deve lançar a exceção TwoFactorSecretNotFoundException quando a secret não existe")
        void shouldThrowTwoFactorSecretNotFoundExceptionWhenUserSecretNotFound() {
            // Arrange
            when(user.getTwoFactorEnabled()).thenReturn(true);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(TwoFactorSecretNotFoundException.class,
                    () -> twoFactorAuthService.disableTwoFactorAuth(user, twoFactorAuthRequestDto));
            verify(user, never()).disableTwoFactor();
            verify(userRepository, never()).save(user);
            verify(twoFactorAuthRepository, never()).deleteByUserId(user.getId());
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidA2FCodeException quando o código informado é inválido")
        void shouldThrowInvalidA2FCodeExceptionWhenCodeIsInvalid() {
            // Arrange
            String secret = "secret";

            when(user.getTwoFactorEnabled()).thenReturn(true);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.of(twoFactorAuth));
            when(twoFactorAuth.getSecret()).thenReturn(secret);
            when(totpService.verifyCode(twoFactorAuthRequestDto.code(), twoFactorAuth.getSecret())).thenReturn(false);

            // Act + Assert
            assertThrows(InvalidA2FCodeException.class,
                    () -> twoFactorAuthService.disableTwoFactorAuth(user, twoFactorAuthRequestDto));
            verify(user, never()).disableTwoFactor();
            verify(userRepository, never()).save(user);
            verify(twoFactorAuthRepository, never()).deleteByUserId(user.getId());
        }

        @Test
        @DisplayName("Deve desabilitar a A2F e deletar a secret do usuário quando o código é válido")
        void shouldDisableAndDeleteUserSecretWhenCodeIsValid() {
            // Arrange
            String secret = "secret";

            when(user.getTwoFactorEnabled()).thenReturn(true);
            when(twoFactorAuthRepository.findByUserId(user.getId())).thenReturn(Optional.of(twoFactorAuth));
            when(twoFactorAuth.getSecret()).thenReturn(secret);
            when(totpService.verifyCode(twoFactorAuthRequestDto.code(), secret)).thenReturn(true);

            // Act
            twoFactorAuthService.disableTwoFactorAuth(user, twoFactorAuthRequestDto);

            InOrder inOrder = inOrder(user, userRepository, twoFactorAuthRepository);
            inOrder.verify(user).disableTwoFactor();
            inOrder.verify(userRepository).save(user);
            inOrder.verify(twoFactorAuthRepository).deleteByUserId(user.getId());
        }
    }
}