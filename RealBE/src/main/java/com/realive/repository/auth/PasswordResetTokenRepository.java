package com.realive.repository.auth;

import com.realive.domain.auth.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository 
        extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByEmailAndCode(String email, String code);
}
