package com.gabriellabritz.build_finance_api.domain.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.jwt.JwtGenerationException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JwtService {
    @Value("${api.security.jwt.secret}")
    private String jwtSecret;

    @Value("${api.security.jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Getter
    @Value("${api.security.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration);
    }

    private String generateToken(User user, Long expirationSeconds) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            return JWT.create()
                    .withIssuer("Build Finance API")
                    .withSubject(user.getEmail())
                    .withClaim("userId", user.getId().toString())
                    .withExpiresAt(expiration(expirationSeconds))
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new JwtGenerationException("Erro ao gerar token JWT.");
        }
    }

    public String verifyToken(String token) {
        DecodedJWT decodedJWT;
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("Build Finance API")
                    .build();

            decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException exception){
            throw new JWTVerificationException("Token JWT inválido.");
        }
    }

    private Instant expiration(Long seconds) {
        return LocalDateTime.now().plusSeconds(seconds).toInstant(ZoneOffset.UTC);
    }
}
