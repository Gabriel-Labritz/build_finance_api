package com.gabriellabritz.build_finance_api.domain.auth.jwt;

import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.InvalidRefreshTokenException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createAndSave(User user) {
        String refreshToken = jwtService.generateRefreshToken(user);
        return refreshTokenRepository.save(new RefreshToken(refreshToken, user, jwtService.getRefreshTokenExpiration()));
    }

    @Transactional
    public RefreshToken getValidRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido."));

        if(refreshToken.isExpired()) {
            removeToken(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token expirado.");
        }

        return refreshToken;
    }

    @Transactional
    public void removeToken(RefreshToken refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken.getRefreshToken());
    }
}
