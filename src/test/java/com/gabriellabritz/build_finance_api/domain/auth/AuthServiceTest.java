package com.gabriellabritz.build_finance_api.domain.auth;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationTokenRepository;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.email.EmailService;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.EmailAlreadyUsedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthRegisterRequestDto authRegisterRequestDto;

    @BeforeEach
    void setUp() {
        this.authRegisterRequestDto = new AuthRegisterRequestDto("Teste", "teste@email.com", "Senha@123");
    }

    @Nested
    class register {
        @Test
        @DisplayName("Deve lança a exceção EmailAlreadyUsedException quando o email informado já está em uso.")
        void shouldThrowEmailAlreadyUsedExceptionWhenEmailIsUsed() {
            // Assert
            when(userRepository.existsByEmailIgnoreCase(authRegisterRequestDto.email()))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(EmailAlreadyUsedException.class, () -> authService.register(authRegisterRequestDto));

            verify(userRepository, never()).save(any());
            verify(emailVerificationTokenRepository, never()).save(any());
            verify(emailService, never()).sendEmailVerification(any(), any());
        }

        @Test
        @DisplayName("Deve salvar o usuário com a senha encodada quando os dados são válidos")
        void shouldSaveUserWithEncodedPassword() {
            // Arrange
            when(userRepository.existsByEmailIgnoreCase(authRegisterRequestDto.email()))
                    .thenReturn(false);
            when(passwordEncoder.encode(authRegisterRequestDto.password()))
                    .thenReturn("password_hash");
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(emailVerificationTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Act
            authService.register(authRegisterRequestDto);

            // Assert
            verify(passwordEncoder).encode(authRegisterRequestDto.password());
            verify(userRepository).save(argThat(user -> user.getPassword().equals("password_hash")));
        }

        @Test
        @DisplayName("Deve gerar e salvar o token de verificação de email após registro")
        void shouldSaveTokenVerification() {
            // Arrange
            when(userRepository.existsByEmailIgnoreCase(authRegisterRequestDto.email()))
                    .thenReturn(false);
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(emailVerificationTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Act
            authService.register(authRegisterRequestDto);

            // Assert
            verify(emailVerificationTokenRepository)
                    .save(argThat(token ->
                            token.getUser() != null &&
                                    token.getToken() != null &&
                                    !token.getToken().isBlank() &&
                                    token.isExpired()
                    ));
        }

        @Test
        @DisplayName("Deve disparar o email de verificação de conta após o registro")
        void shouldSendVerificationEmailAfterRegister() {
            // Arrange
            when(userRepository.existsByEmailIgnoreCase(authRegisterRequestDto.email()))
                    .thenReturn(false);
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(emailVerificationTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Act
            authService.register(authRegisterRequestDto);

            // Assert
            verify(emailService).sendEmailVerification(argThat(user ->
                    user.getEmail().equals(authRegisterRequestDto.email())), any());
        }
    }
}