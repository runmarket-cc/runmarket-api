package com.runmarket.pacer.infrastructure.persistence.repository;

import com.runmarket.pacer.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {
    Optional<PasswordResetTokenJpaEntity> findByToken(String token);
    void deleteByUserId(UUID userId);
}
