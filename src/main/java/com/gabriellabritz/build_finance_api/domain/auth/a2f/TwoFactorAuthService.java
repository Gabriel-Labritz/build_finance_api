package com.gabriellabritz.build_finance_api.domain.auth.a2f;

import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.TwoFactorSetupResponse;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorAuthAlreadyEnabled;
import com.gabriellabritz.build_finance_api.infra.security.totp.TotpService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorAuthService {
    private final TotpService totpService;
    private final TwoFactorAuthRepository twoFactorAuthRepository;

    public TwoFactorAuthService(TotpService totpService, TwoFactorAuthRepository twoFactorAuthRepository) {
        this.totpService = totpService;
        this.twoFactorAuthRepository = twoFactorAuthRepository;
    }

    @Transactional
    public TwoFactorSetupResponse generateQRCode(User userLogged) {
        if (userLogged.getTwoFactorEnabled()) {
            throw new TwoFactorAuthAlreadyEnabled("A autenticação de dois fatores já está habilitada.");
        }

        twoFactorAuthRepository.findByUserId(userLogged.getId())
                .ifPresent(twoFactorAuth -> {
                    twoFactorAuthRepository.delete(twoFactorAuth);
                    twoFactorAuthRepository.flush();
                } );

        String secret = totpService.generateSecret();

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.save(new TwoFactorAuth(secret, userLogged));
        String qrCodeUrl = totpService.generateQRCode(twoFactorAuth);

        return new TwoFactorSetupResponse(qrCodeUrl);
    }
}
