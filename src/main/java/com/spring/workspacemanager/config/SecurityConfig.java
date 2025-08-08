package com.spring.workspacemanager.config;

import com.spring.workspacemanager.security.CustomUserDetailsService;
import com.spring.workspacemanager.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig configures Spring Security for our application.
 */
@Configuration // Marks as a configuration class for Spring (beans defined here are created)
@EnableMethodSecurity(prePostEnabled = true) // Enable method-level security annotations (e.g., @PreAuthorize)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Provide a PasswordEncoder bean. BCrypt is industry standard for hashing passwords.
     * We will use this encoder to hash user passwords when registering and to verify them on login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider tells Spring Security how to retrieve user details and verify credentials.
     * DaoAuthenticationProvider uses our UserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // how to load users
        provider.setPasswordEncoder(passwordEncoder());     // how to verify passwords
        return provider;
    }

    /**
     * Expose AuthenticationManager so we can use it in controllers (e.g., to manually authenticate a login request).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configure HTTP security, which endpoints are public, session policy, filters, etc.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {

        http
                // Disable CSRF because we are a stateless REST API (CSRF protection is mainly for browser sessions)
                .csrf(csrf -> csrf.disable())

                // We don't use HTTP sessions — JWT is stateless — so disable session creation
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Set AuthenticationProvider (our DaoAuthenticationProvider)
                .authenticationProvider(authenticationProvider())

                // Configure URL authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to authentication endpoints and swagger (if you add it)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                );

        // Add our JWT filter before the default UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Build and return the filter chain
        return http.build();
    }
}
