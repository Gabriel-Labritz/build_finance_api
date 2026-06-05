package com.gabriellabritz.build_finance_api.domain.auth.a2f;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, UUID> {
    Optional<TwoFactorAuth> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
