package com.gabriellabritz.build_finance_api.domain.auth;

import com.gabriellabritz.build_finance_api.domain.auth.account_verification.EmailVerificationToken;
import com.gabriellabritz.build_finance_api.domain.auth.account_verification.EmailVerificationTokenRepository;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthLoginRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshToken;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshTokenService;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.dtos.RefreshTokenRequestDto;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.email.EmailService;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.EmailAlreadyUsedException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private RefreshToken refreshToken;

    @Mock
    private EmailVerificationToken emailVerificationToken;

    @Mock
    private User user;

    private AuthRegisterRequestDto authRegisterRequestDto;

    private AuthLoginRequestDto authLoginRequestDto;

    private RefreshTokenRequestDto refreshTokenRequestDto;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<EmailVerificationToken> emailVerificationTokenArgumentCaptor;

    @BeforeEach
    void setUp() {
        this.authRegisterRequestDto = new AuthRegisterRequestDto("Teste", "teste@email.com", "Senha@123");
        this.authLoginRequestDto = new AuthLoginRequestDto("teste@email.com", "Acb1234!");
        this.refreshTokenRequestDto = new RefreshTokenRequestDto("refresh-token");
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

            // Act
            authService.register(authRegisterRequestDto);

            // Assert
            verify(userRepository).existsByEmailIgnoreCase(authRegisterRequestDto.email());
            verify(passwordEncoder).encode(authRegisterRequestDto.password());
            verify(userRepository).save(userArgumentCaptor.capture());

            User userCaptured = userArgumentCaptor.getValue();
            assertEquals(authRegisterRequestDto.name(), userCaptured.getName());
            assertEquals(authRegisterRequestDto.email(), userCaptured.getEmail());
            assertEquals("password_hash", userCaptured.getPassword());
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
            verify(emailVerificationTokenRepository).save(emailVerificationTokenArgumentCaptor.capture());

            EmailVerificationToken emailTokenCaptured = emailVerificationTokenArgumentCaptor.getValue();
            assertNotNull(emailTokenCaptured.getUser());
            assertEquals(authRegisterRequestDto.email(), emailTokenCaptured.getUser().getEmail());
            assertNotNull(emailTokenCaptured.getToken());
            assertFalse(emailTokenCaptured.isExpired());
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
            verify(emailService).sendEmailVerification(userArgumentCaptor.capture(), emailVerificationTokenArgumentCaptor.capture());

            User userCaptured = userArgumentCaptor.getValue();
            EmailVerificationToken emailTokenCaptured = emailVerificationTokenArgumentCaptor.getValue();

            assertEquals(authRegisterRequestDto.email(), userCaptured.getEmail());
            assertNotNull(emailTokenCaptured.getToken());
            assertEquals(userCaptured, emailTokenCaptured.getUser());
        }
    }

    @Nested
    class login {
        @Test
        @DisplayName("Deve lança a exceção BadCredentialsException quando as credênciais do usuário são inválidas.")
        void shouldThrowBadCredentialsExceptionWhenUserCredentialsIsInvalid() {
            // Arrange
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas."));

            // Act + Assert
            assertThrows(BadCredentialsException.class, () -> authService.login(authLoginRequestDto));
            verify(jwtService, never()).generateAccessToken(any());
            verify(refreshTokenService, never()).createAndSave(any());
        }

        @Test
        @DisplayName("Deve lança a exceção DisabledException quando a conta do usuário está inativa.")
        void shouldThrowDisabledExceptionWhenUserAccountIsInactive() {
            // Arrange
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new DisabledException("Conta inativa."));

            // Act + Assert
            assertThrows(DisabledException.class, () -> authService.login(authLoginRequestDto));
            verify(jwtService, never()).generateAccessToken(any());
            verify(refreshTokenService, never()).createAndSave(any());
        }

        @Test
        @DisplayName("Deve gerar um token de pre-auth quando o usuário está com a a2f habilitada")
        void shouldGeneratePreAuthTokenWhenTwoFactorAuthIsEnabled() {
            // Arrange
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(user.getTwoFactorEnabled()).thenReturn(true);
            when(jwtService.generatePreAuthToken(user)).thenReturn("pre-auth-token");

            // Act
            AuthLoginResponseDto result = authService.login(authLoginRequestDto);

            // Assert
            verify(jwtService).generatePreAuthToken(user);
            verify(jwtService, never()).generateAccessToken(any());
            verify(refreshTokenService, never()).createAndSave(any());

            assertTrue(result.requiresTwoFactor());
            assertNull(result.accessToken());
            assertNull(result.refreshToken());
            assertEquals("pre-auth-token", result.preAuthToken());
        }

        @Test
        @DisplayName("Deve retornar o accessToken e o refreshToken quando o usuário não está com a a2f habilitada.")
        void shouldReturnAccessAndRefreshTokenWhenUserDoesNotTwoFactorEnabled() {
            // Arrange
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(user.getTwoFactorEnabled()).thenReturn(false);
            when(jwtService.generateAccessToken(user)).thenReturn("access-token");
            when(refreshTokenService.createAndSave(user)).thenReturn(refreshToken);
            when(refreshToken.getRefreshToken()).thenReturn("refresh-token");

            // Act
            AuthLoginResponseDto result = authService.login(authLoginRequestDto);

            // Assert
            verify(jwtService, never()).generatePreAuthToken(any());
            verify(jwtService).generateAccessToken(user);
            verify(refreshTokenService).createAndSave(user);

            assertNotNull(result);
            assertFalse(result.requiresTwoFactor());
            assertEquals("access-token", result.accessToken());
            assertEquals("refresh-token", result.refreshToken());
            assertNull(result.preAuthToken());
        }
    }
}