package com.spring.workspacemanager.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // We'll handle CSRF later for APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/register").permitAll() // Public
                        .anyRequest().authenticated() // All other endpoints need login
                )
                .formLogin(Customizer.withDefaults())
                .logout(logout->logout
                        .logoutUrl("/auth/logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                )// We're using custom login endpoint

                .httpBasic(basic -> basic.disable()) // No Basic Auth
                .sessionManagement(session -> session
                        .maximumSessions(2) // Allow only 1 active session per user
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
