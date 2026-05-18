package com.gabriellabritz.build_finance_api.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmailIgnoreCase(String email);
}
