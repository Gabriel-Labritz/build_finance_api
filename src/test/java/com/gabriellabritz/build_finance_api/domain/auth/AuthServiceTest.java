package com.gabriellabritz.build_finance_api.domain.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthLoginRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.dtos.RefreshTokenRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshToken;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshTokenService;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationToken;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationTokenRepository;
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
    class verifyAccount {
        @Test
        @DisplayName("Deve lançar a exceção InvalidVerificationTokenException para um token inválido")
        void shouldThrowInvalidVerificationTokenExceptionWhenTokenIsInvalid() {
            // Arrange
            when(emailVerificationTokenRepository.findByToken("token-invalid")).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(InvalidVerificationTokenException.class, () -> authService.verifyAccount("token-invalid"));
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
            assertThrows(InvalidVerificationTokenException.class, () -> authService.verifyAccount("token-expired"));
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
            assertThrows(UserAlreadyVerifiedException.class, () -> authService.verifyAccount("token-valid"));
            verify(emailVerificationTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve verificar o usuário e deletar o token quando o token é válido.")
        void shouldVerifyUserAndDeleteTokenWhenValid() {
            // Arrange
            when(emailVerificationTokenRepository.findByToken("token-valid")).thenReturn(Optional.of(emailVerificationToken));
            when(emailVerificationToken.getUser()).thenReturn(user);

            // Act
            authService.verifyAccount("token-valid");

            // Assert
            verify(emailVerificationToken).validate();
            verify(user).verify();
            verify(emailVerificationTokenRepository).delete(emailVerificationToken);
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

    @Nested
    class generateNewRefreshToken {
        @Test
        @DisplayName("Deve lançar a exceção InvalidRefreshTokenException quando o token não existe")
        void shouldThrowInvalidRefreshTokenExceptionWhenRefreshTokenNotFound() {
            // Arrange
            String invalidToken = "invalid-token";
            when(refreshTokenService.getValidRefreshToken(invalidToken))
                    .thenThrow(new InvalidRefreshTokenException("Refresh token inválido."));

            // Act + Assert
            assertThrows(InvalidRefreshTokenException.class, () -> authService
                    .generateNewRefreshToken(new RefreshTokenRequestDto("invalid-token")));
            verify(jwtService, never()).verifyToken(any());
            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(refreshTokenService, never()).removeToken(any());
            verify(jwtService, never()).generateAccessToken(any());
            verify(refreshTokenService, never()).createAndSave(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção InvalidRefreshTokenException quando o token está expirado")
        void shouldThrowInvalidRefreshTokenExceptionWhenRefreshTokenIsExpired() {
            // Arrange
            String tokenExpired = "token-expired";
            when(refreshTokenService.getValidRefreshToken(tokenExpired))
                    .thenThrow(new InvalidRefreshTokenException("Refresh token expirado."));

            // Act + Assert
            assertThrows(InvalidRefreshTokenException.class, () -> authService
                    .generateNewRefreshToken(new RefreshTokenRequestDto("token-expired")));
            verify(jwtService, never()).verifyToken(any());
            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(refreshTokenService, never()).removeToken(any());
            verify(jwtService, never()).generateAccessToken(any());
            verify(refreshTokenService, never()).createAndSave(any());
        }

        @Test
        @DisplayName("Deve lançar a exceção JWTVerificationException quando o token JWT está corrompido")
        void shouldThrowJWTVerificationExceptionWhenRefreshTokenIsCorrupted() {
            // Arrange
            String token = "token";
            String invalidToken = "invalid-token";
            when(refreshTokenService.getValidRefreshToken(token)).thenReturn(refreshToken);
            when(refreshToken.getRefreshToken()).thenReturn(invalidToken);
            when(jwtService.verifyToken(invalidToken)).thenThrow(new JWTVerificationException("Token JWT inválido."));

            // Act + Assert
            assertThrows(JWTVerificationException.class, () -> authService
                    .generateNewRefreshToken(new RefreshTokenRequestDto(token)));
            verify(jwtService).verifyToken(invalidToken);
            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(refreshTokenService, never()).removeToken(any());
            verify(jwtService, never()).generateAccessToken(any());
            verify(refreshTokenService, never()).createAndSave(any());
        }

        @Test
        @DisplayName("Deve retornar novos tokens quando o refresh token é válido")
        void shouldReturnTokensWhenRefreshTokenIsValid() {
            // Arrange
            RefreshToken oldRefreshToken = mock(RefreshToken.class);
            RefreshToken newRefreshToken = mock(RefreshToken.class);

            String validToken = "valid-token";
            String userEmailSubject = "useremail@email.com";

            when(refreshTokenService.getValidRefreshToken(validToken)).thenReturn(oldRefreshToken);
            when(oldRefreshToken.getRefreshToken()).thenReturn(validToken);
            when(jwtService.verifyToken(validToken)).thenReturn(userEmailSubject);
            when(userRepository.findByEmailIgnoreCase(userEmailSubject)).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
            when(refreshTokenService.createAndSave(user)).thenReturn(newRefreshToken);
            when(newRefreshToken.getRefreshToken()).thenReturn("new-refresh-token");

            // Act
            AuthLoginResponseDto result = authService.generateNewRefreshToken(new RefreshTokenRequestDto(validToken));

            // Assert
            assertNotNull(result);
            assertEquals("new-access-token", result.accessToken());
            assertEquals("new-refresh-token", result.refreshToken());
        }

        @Test
        @DisplayName("Deve remover o token antigo quando o refresh token é válido")
        void shouldRemoveOldRefreshTokenWhenRefreshTokenIsValid() {
            // Arrange
            RefreshToken oldRefreshToken = mock(RefreshToken.class);
            RefreshToken newRefreshToken = mock(RefreshToken.class);

            String validToken = "valid-token";
            String userEmailSubject = "useremail@email.com";

            when(refreshTokenService.getValidRefreshToken(validToken)).thenReturn(oldRefreshToken);
            when(oldRefreshToken.getRefreshToken()).thenReturn(validToken);
            when(jwtService.verifyToken(validToken)).thenReturn(userEmailSubject);
            when(userRepository.findByEmailIgnoreCase(userEmailSubject)).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
            when(refreshTokenService.createAndSave(user)).thenReturn(newRefreshToken);
            when(newRefreshToken.getRefreshToken()).thenReturn("new-refresh-token");

            // Act
            authService.generateNewRefreshToken(new RefreshTokenRequestDto(validToken));

            // Assert
            verify(refreshTokenService).removeToken(oldRefreshToken);
        }
    }
}