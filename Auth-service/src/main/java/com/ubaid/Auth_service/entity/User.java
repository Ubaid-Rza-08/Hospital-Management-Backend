package com.ubaid.Auth_service.entity;

import com.ubaid.Auth_service.entity.type.AuthProviderType;
import com.ubaid.Auth_service.entity.type.RoleType;
import com.ubaid.Auth_service.security.RolePermissionMapping;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique handle, not necessarily email
    @Column(nullable = false, unique = true)
    private String username;

    // Distinct email (optional but recommended unique)
    @Column(unique = true)
    private String email;

    private String password;

    // Optional: store profile names on User if you want to sync them
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private AuthProviderType providerType;

    private String providerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<RoleType> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = roles.stream()
                .flatMap(role -> RolePermissionMapping.getAuthoritiesForRole(role).stream())
                .collect(Collectors.toSet());
        authorities.addAll(roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toSet()));
        return authorities;
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
