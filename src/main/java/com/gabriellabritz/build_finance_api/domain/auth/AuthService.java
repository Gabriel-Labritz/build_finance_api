package com.gabriellabritz.build_finance_api.domain.auth;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthRegisterResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationToken;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationTokenRepository;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.email.EmailService;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.EmailAlreadyUsedException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailVerificationTokenRepository emailVerificationTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
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
}
