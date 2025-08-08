package com.spring.workspacemanager.security;

import com.spring.workspacemanager.model.User;
import com.spring.workspacemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * CustomUserDetailsService implements Spring Security's UserDetailsService.
 * Spring Security calls loadUserByUsername(...) to retrieve user details (username, password, roles).
 */
@Service // Marks this class as a Spring bean and allows it to be injected where needed.
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired // Inject the UserRepository bean automatically
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username (we use email as username).
     * This method is invoked by Spring Security during authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find the user in DB by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Build a Spring Security UserDetails object with username, password, and authorities
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())           // principal / username
                .password(user.getPassword())        // hashed password from DB
                .authorities(getAuthorities(user))   // roles/authorities
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Convert our User.role to a collection of GrantedAuthority.
     * GrantedAuthority is the Spring Security representation of a role/permission.
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // SimpleGrantedAuthority expects a string like "ROLE_ADMIN" or "ROLE_MEMBER"
        String roleName = "ROLE_" + user.getRole().name();
        return List.of(new SimpleGrantedAuthority(roleName));
    }
}
