package com.dermabliss.clinic.security;

import com.dermabliss.clinic.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String phoneNumber;
    private final String password;
    private final String role;
    private final boolean active;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.phoneNumber = user.getPhoneNumber();
        this.password = user.getPasswordHash();
        this.role = user.getRole().name();
        this.active = "ACTIVE".equalsIgnoreCase(user.getStatus());
    }

    public Long getId() { return id; }
    public String getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return phoneNumber; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
