package com.gabriellabritz.build_finance_api.domain.auth;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.requests.AuthRegisterRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthRegisterResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.VerifiedUserResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationToken;
import com.gabriellabritz.build_finance_api.domain.auth.verification.EmailVerificationTokenRepository;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.email.EmailService;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.EmailAlreadyUsedException;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.InvalidVerificationTokenException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
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

    @Transactional
    public VerifiedUserResponseDto verifyAccount(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByToken(token).orElseThrow(() -> new InvalidVerificationTokenException("Token de verificação inválido."));

        verificationToken.validate();
        verificationToken.getUser().verify();
        emailVerificationTokenRepository.delete(verificationToken);

        return new VerifiedUserResponseDto("Sua conta foi verificada com sucesso!, Você já pode fazer o login para começar a organizar suas finanças");
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailIgnoreCaseAndActiveTrueAndVerifiedTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado."));
    }
}
