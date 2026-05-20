package com.gabriellabritz.build_finance_api.domain.auth.verification;

import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.auth.InvalidVerificationTokenException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public EmailVerificationToken(User user) {
        this.token = UUID.randomUUID().toString();
        this.expiresAt = LocalDateTime.now().plusHours(24);
        this.user = user;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void validate() {
        if (isExpired()) {
            throw new InvalidVerificationTokenException("Token de verificação expirado.");
        }
    }
}
