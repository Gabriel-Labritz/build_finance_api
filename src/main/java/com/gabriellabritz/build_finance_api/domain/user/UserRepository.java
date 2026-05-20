package com.gabriellabritz.build_finance_api.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmailIgnoreCase(String email);
    Optional<UserDetails> findByEmailIgnoreCaseAndActiveTrueAndVerifiedTrue(String email);
}
