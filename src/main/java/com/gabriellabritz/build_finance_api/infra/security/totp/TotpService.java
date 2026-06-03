package com.gabriellabritz.build_finance_api.infra.security.totp;

import com.atlassian.onetime.service.RandomSecretProvider;
import com.gabriellabritz.build_finance_api.domain.auth.a2f.TwoFactorAuth;
import org.springframework.stereotype.Service;

@Service
public class TotpService {
    public String generateSecret() {
        return new RandomSecretProvider().generateSecret().getBase32Encoded();
    }

    public String generateQRCode(TwoFactorAuth twoFactorAuth) {
        String issuer = "Build Finance API";

        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, twoFactorAuth.getUser().getUsername(), twoFactorAuth.getSecret(), issuer
        );
    }
}
