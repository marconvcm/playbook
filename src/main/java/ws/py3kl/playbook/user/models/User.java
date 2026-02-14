package ws.py3kl.playbook.user.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@Table(name = "users", indexes = {
    @Index(name = "idx_users_handle", columnList = "handle"),
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_access_token", columnList = "access_token")
})
@Entity
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = "users_id_seq", strategy = GenerationType.SEQUENCE)
    protected Long id;

    @Column(unique = true, name = "handle", nullable = false)
    @NotBlank
    protected String handle;

    @Column(name = "first_name", nullable = false)
    @NotBlank
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank
    protected String lastName;

    @Column(unique = true, name = "email", nullable = false)
    @Email
    protected String email;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    protected String password;

    @Column(name = "password_salt", nullable = false)
    @JsonIgnore
    protected String passwordSalt;

    @Column(name = "roles", nullable = false)
    protected String[] roles;

    @Column(name = "access_token")
    @JsonIgnore
    protected String accessToken;

    @Column(name = "refresh_token")
    @JsonIgnore
    protected String refreshToken;

    @Column(name = "api_token")
    @JsonIgnore
    protected String apiToken;

    @Column(name = "system_user")
    @JsonIgnore
    protected Boolean systemUser = false;

    @Column(name = "token_created_at")
    @JsonIgnore
    protected LocalDateTime tokenCreatedAt;

    @Column(name = "token_expires_at")
    @JsonIgnore
    protected LocalDateTime tokenExpiresAt;

    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    protected LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    protected LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @JsonIgnore
    protected LocalDateTime deletedAt;

    @Column(name = "disabled_at")
    @JsonIgnore
    protected LocalDateTime disabledAt;

    @Column(name = "last_login_at")
    @JsonIgnore
    protected LocalDateTime lastLoginAt;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of(roles)
                .map(role -> (GrantedAuthority) () -> role)
                .toList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired() && deletedAt == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked() && disabledAt == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired() && deletedAt == null;
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled() && deletedAt == null && disabledAt == null;
    }

    public long getTokenExpiresIn() {
        if (tokenCreatedAt == null) {
            return 0;
        }
        return tokenExpiresAt.toEpochSecond(java.time.ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC);
    }

    public boolean isAdmin() {
        return Arrays.asList(roles).contains("ROLE_ADMIN");
    }

    public boolean isNonSystemUser() {
        return systemUser == null || !systemUser;
    }
}
