package dev.kingscode.ecommerce_api.model;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.kingscode.ecommerce_api.model.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users", uniqueConstraints = {
                @UniqueConstraint(columnNames = "email", name = "uk_users_email")
}, indexes = { @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_deleted_at", columnList = "deleted_at") })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User implements UserDetails {
        @Id
        @UuidGenerator
        @Column(nullable = false, updatable = false)
        private UUID id;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(name = "first_name", length = 100)
        private String firstName;

        @Column(name = "last_name", length = 100)
        private String lastName;

        @Column(nullable = false)
        private String password;

        @Column(name = "is_active", nullable = false)
        @Builder.Default
        private Boolean active = true;

        @Column(name = "email_verified")
        @Builder.Default
        private Boolean emailVerified = false;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        @Builder.Default
        private UserRole role = UserRole.USER;

        @Column(name = "profile_image")
        private String profileImage;

        @Column(name = "verification_token")
        private String verificationToken;

        @Column(name = "password_reset_token")
        private String pwdResetToken;

        @Column(name = "token_expiration")
        private Instant tokenExpiration;

        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        @Column(name = "deleted_at")
        private Instant deletedAt;

        @PrePersist
        protected void onCreate() {
                Instant now = Instant.now();
                this.createdAt = now;
                this.updatedAt = now;
        }

        @PreUpdate
        protected void onUpdate() {
                this.updatedAt = Instant.now();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(role.name()));
        }

        @Override
        public String getUsername() {
                return email;
        }

}
