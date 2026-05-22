package com.gabriellabritz.build_finance_api.domain.auth;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthLoginRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthRegisterResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.VerifiedUserResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.RefreshToken;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.RefreshTokenRepository;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationToken;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationTokenRepository;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.email.EmailService;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.EmailAlreadyUsedException;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.InvalidVerificationTokenException;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService,
            AuthenticationManager authenticationManager,
            JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public AuthRegisterResponseDto register(AuthRegisterRequestDto authRegisterRequestDto) {
        if(userRepository.existsByEmailIgnoreCase(authRegisterRequestDto.email())) {
            throw new EmailAlreadyUsedException("O email informado já está em uso, por favor, informe outro email.");
        }

        String passwordEncoded = passwordEncoder.encode(authRegisterRequestDto.password());

        User user = userRepository.save(new User(authRegisterRequestDto, passwordEncoded));
        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.save(new EmailVerificationToken(user));

        emailService.sendEmailVerification(user, emailVerificationToken);
        return new AuthRegisterResponseDto("Cadastro realizado com sucesso! Verifique seu email para ativar a conta.");
    }

    @Transactional
    public VerifiedUserResponseDto verifyAccount(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByToken(token).orElseThrow(() -> new InvalidVerificationTokenException("Token de verificação inválido."));

        verificationToken.validate();
        verificationToken.getUser().verify();
        emailVerificationTokenRepository.delete(verificationToken);

        return new VerifiedUserResponseDto("Sua conta foi verificada com sucesso!, Você já pode fazer o login para começar a organizar suas finanças");
    }

    @Transactional
    public AuthLoginResponseDto login(AuthLoginRequestDto authLoginRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authLoginRequestDto.email(), authLoginRequestDto.password());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        User user = (User) authenticate.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenRepository.save(new RefreshToken(refreshToken, user, jwtService.getRefreshTokenExpiration()));
        return new AuthLoginResponseDto(accessToken, refreshToken);
    }
}
