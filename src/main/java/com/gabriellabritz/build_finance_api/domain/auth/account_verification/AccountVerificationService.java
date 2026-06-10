package com.gabriellabritz.build_finance_api.domain.auth.account_verification;

import com.gabriellabritz.build_finance_api.domain.auth.account_verification.dtos.VerifiedUserResponseDto;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.InvalidVerificationTokenException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AccountVerificationService {
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public AccountVerificationService(EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
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
}
