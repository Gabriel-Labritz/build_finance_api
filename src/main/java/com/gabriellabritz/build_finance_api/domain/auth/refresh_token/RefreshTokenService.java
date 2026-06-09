package com.gabriellabritz.build_finance_api.domain.auth.refresh_token;

import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.dtos.RefreshTokenRequestDto;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.InvalidRefreshTokenException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
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
            removeToken(refreshToken.getRefreshToken());
            throw new InvalidRefreshTokenException("Refresh token expirado.");
        }

        return refreshToken;
    }

    @Transactional
    public void removeToken(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    @Transactional
    public AuthLoginResponseDto generateNewRefreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        RefreshToken refreshToken = getValidRefreshToken(
                refreshTokenRequestDto.refreshToken()
        );

        String userEmailSubject = jwtService.verifyToken(refreshToken.getRefreshToken());
        User user = userRepository.findByEmailIgnoreCase(userEmailSubject)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado."));

        removeToken(refreshToken.getRefreshToken());

        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = createAndSave(user);

        return new AuthLoginResponseDto(false, newAccessToken, newRefreshToken.getRefreshToken(), null);
    }
}
