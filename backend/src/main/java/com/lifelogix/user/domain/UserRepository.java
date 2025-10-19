package com.lifelogix.user.domain;

import com.lifelogix.user.domain.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderTypeAndProviderId(ProviderType providerType, String providerId);
}
