package com.ubaid.Auth_service.repository;

import com.ubaid.Auth_service.entity.User;
import com.ubaid.Auth_service.entity.type.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProviderType providerType);
    boolean existsByUsername(String username);
}
