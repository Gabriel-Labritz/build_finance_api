package com.gabriellabritz.build_finance_api.domain.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthLoginRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.dtos.RefreshTokenRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshToken;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshTokenService;
import com.gabriellabritz.build_finance_api.domain.auth.account_verification.EmailVerificationToken;
import com.gabriellabritz.build_finance_api.domain.auth.account_verification.EmailVerificationTokenRepository;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.email.EmailService;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.EmailAlreadyUsedException;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.InvalidVerificationTokenException;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.UserAlreadyVerifiedException;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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

    private EmailVerificationToken emailVerificationToken;
    private User user;
    private AuthRegisterRequestDto authRegisterRequestDto;
    private AuthLoginRequestDto authLoginRequestDto;
    private RefreshTokenRequestDto refreshTokenRequestDto;

    @BeforeEach
    void setUp() {
        this.authRegisterRequestDto = new AuthRegisterRequestDto("Teste", "teste@email.com", "Senha@123");
        this.emailVerificationToken = mock(EmailVerificationToken.class);
        this.user = mock(User.class);
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
                                    !token.isExpired()
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
        @DisplayName("Deve retornar o accessToken e o refreshToken para login válido.")
        void shouldReturnTokensWhenLoginValid() {
            // Arrange
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(jwtService.generateAccessToken(user)).thenReturn("access-token");
            when(refreshTokenService.createAndSave(user)).thenReturn(refreshToken);
            when(refreshToken.getRefreshToken()).thenReturn("refresh-token");

            // Act
            AuthLoginResponseDto result = authService.login(authLoginRequestDto);

            // Assert
            assertNotNull(result);
            assertEquals("access-token", result.accessToken());
            assertEquals("refresh-token", result.refreshToken());
        }

        @Test
        @DisplayName("Deve salvar o no banco para login válido.")
        void shouldSaveRefreshTokenWhenLoginValid() {
            // Arrange
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(refreshTokenService.createAndSave(user)).thenReturn(refreshToken);
            when(refreshToken.getRefreshToken()).thenReturn("refresh-token");

            // Act
            AuthLoginResponseDto result = authService.login(authLoginRequestDto);

            // Assert
            verify(refreshTokenService).createAndSave(user);
        }
    }
}