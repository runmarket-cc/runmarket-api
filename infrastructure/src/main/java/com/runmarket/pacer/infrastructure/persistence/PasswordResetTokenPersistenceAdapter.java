package com.runmarket.pacer.infrastructure.persistence;

import com.runmarket.pacer.domain.model.PasswordResetToken;
import com.runmarket.pacer.domain.port.out.user.PasswordResetTokenRepository;
import com.runmarket.pacer.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.runmarket.pacer.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    @Override
    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenJpaEntity entity = jpaRepository.save(
                PasswordResetTokenJpaEntity.builder()
                        .userId(token.getUserId())
                        .token(token.getToken())
                        .expiresAt(token.getExpiresAt())
                        .build()
        );
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }

    private PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        return PasswordResetToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .token(entity.getToken())
                .expiresAt(entity.getExpiresAt())
                .build();
    }
}
