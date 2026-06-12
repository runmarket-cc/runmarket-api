package com.runmarket.pacer.domain.port.out.user;

import com.runmarket.pacer.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(User user);
    void updateVerified(UUID userId);
    void updatePassword(UUID userId, String encodedPassword);
    void deleteById(UUID userId);
}
