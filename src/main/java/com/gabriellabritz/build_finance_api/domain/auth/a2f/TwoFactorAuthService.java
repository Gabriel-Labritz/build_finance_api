package com.gabriellabritz.build_finance_api.domain.auth.a2f;

import com.gabriellabritz.build_finance_api.domain.auth.a2f.dtos.request.TwoFactorAuthRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.a2f.dtos.response.TwoFactorSetupResponse;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshToken;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshTokenService;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.InvalidPreAuthTokenException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.InvalidA2FCodeException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorAuthAlreadyEnabledException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorAuthNotEnabledException;
import com.gabriellabritz.build_finance_api.infra.exceptions.two_factor_auth.TwoFactorSecretNotFoundException;
import com.gabriellabritz.build_finance_api.infra.security.totp.TotpService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TwoFactorAuthService {
    private final TotpService totpService;
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public TwoFactorAuthService(
            TotpService totpService,
            TwoFactorAuthRepository twoFactorAuthRepository,
            UserRepository userRepository,
            JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.totpService = totpService;
        this.twoFactorAuthRepository = twoFactorAuthRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public TwoFactorSetupResponse generateQRCode(User userLogged) {
        if(userLogged.getTwoFactorEnabled()) {
            throw new TwoFactorAuthAlreadyEnabledException("A autenticação de dois fatores já está habilitada.");
        }

        twoFactorAuthRepository.findByUserId(userLogged.getId())
                .ifPresent(twoFactorAuth -> {
                    twoFactorAuthRepository.delete(twoFactorAuth);
                    twoFactorAuthRepository.flush();
        });

        String secret = totpService.generateSecret();

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.save(new TwoFactorAuth(secret, userLogged));
        String qrCodeUri = totpService.generateQRCodeUri(twoFactorAuth.getSecret(), userLogged.getUsername());

        return new TwoFactorSetupResponse(qrCodeUri);
    }

    @Transactional
    public void enableTwoFactorAuth(User userLogged, TwoFactorAuthRequestDto twoFactorAuthRequestDto) {
        if (userLogged.getTwoFactorEnabled()) {
            throw new TwoFactorAuthAlreadyEnabledException("A autenticação de dois fatores já está habilitada.");
        }

        validateTwoFactorCode(userLogged.getId(), twoFactorAuthRequestDto.code());

        userLogged.enableTwoFactor();
        userRepository.save(userLogged);
    }

    @Transactional
    public void disableTwoFactorAuth(User userLogged, TwoFactorAuthRequestDto twoFactorAuthRequestDto) {
        if (!userLogged.getTwoFactorEnabled()) {
            throw new TwoFactorAuthNotEnabledException("A autenticação de dois fatores não está habilitada.");
        }

        validateTwoFactorCode(userLogged.getId(), twoFactorAuthRequestDto.code());

        userLogged.disableTwoFactor();
        userRepository.save(userLogged);
        twoFactorAuthRepository.deleteByUserId(userLogged.getId());
    }

    public AuthLoginResponseDto verifyTwoFactorAuth(String authHeader, TwoFactorAuthRequestDto twoFactorAuthRequestDto) {
        String preAuthToken = authHeader.substring(7);

        if (!jwtService.isPreAuthToken(preAuthToken)) {
            throw new InvalidPreAuthTokenException("Token inválido para essa operação.");
        }

        String userEmailSubject = jwtService.verifyToken(preAuthToken);
        User user = userRepository.findByEmailIgnoreCase(userEmailSubject)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado."));

        validateTwoFactorCode(user.getId(), twoFactorAuthRequestDto.code());

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createAndSave(user);

        return new AuthLoginResponseDto(false, accessToken, refreshToken.getRefreshToken(), null);
    }

    private void validateTwoFactorCode(UUID userId, String code) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new TwoFactorSecretNotFoundException("Secret não foi encontrada."));

        if (!totpService.verifyCode(code, twoFactorAuth.getSecret())) {
            throw new InvalidA2FCodeException("O Código informado é inválido");
        }
    }
}
