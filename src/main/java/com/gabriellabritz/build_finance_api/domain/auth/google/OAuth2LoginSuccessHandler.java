package com.gabriellabritz.build_finance_api.domain.auth.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabriellabritz.build_finance_api.domain.auth.dtos.responses.AuthLoginResponseDto;
import com.gabriellabritz.build_finance_api.domain.auth.jwt.JwtService;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshToken;
import com.gabriellabritz.build_finance_api.domain.auth.refresh_token.RefreshTokenService;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.domain.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtService jwtService, RefreshTokenService refreshTokenService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();

        UserDetails user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado. Por favor, realize seu cadastro."));

        if (!user.isEnabled()) {
            throw new DisabledException("Sua conta não está verificada");
        }

        String accessToken = jwtService.generateAccessToken((User) user);
        RefreshToken refreshToken = refreshTokenService.createAndSave((User) user);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(
                objectMapper.writeValueAsString(new AuthLoginResponseDto(false, accessToken, refreshToken.getRefreshToken(), null))
        );
    }
}
