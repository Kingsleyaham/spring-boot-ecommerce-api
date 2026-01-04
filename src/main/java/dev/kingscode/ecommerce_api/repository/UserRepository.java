package dev.kingscode.ecommerce_api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kingscode.ecommerce_api.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndEmailVerifiedTrue(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByPwdResetToken(String pwdResetToken);

}