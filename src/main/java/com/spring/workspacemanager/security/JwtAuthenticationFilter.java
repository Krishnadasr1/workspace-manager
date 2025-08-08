package com.spring.workspacemanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter checks each incoming HTTP request for a valid JWT in the Authorization header.
 * If the token is valid, it sets the Authentication in the SecurityContext so downstream controllers can
 * access the authenticated principal.
 */
@Component // Register this filter as a Spring bean
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService JwtService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtService JwtService, CustomUserDetailsService userDetailsService) {
        this.JwtService = JwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * doFilterInternal is called once per request (because we extend OncePerRequestFilter).
     * We look for "Authorization: Bearer <token>" header, validate token, and set the user in SecurityContext.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Read the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2) If header is missing or doesn't start with "Bearer ", skip setting authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3) Extract token substring after "Bearer "
        final String token = authHeader.substring(7);

        // 4) Extract username (email) from token using JwtUtil
        String username = null;
        try {
            username = JwtService.extractUsername(token);
        } catch (Exception ex) {
            // Token parsing might fail (invalid token). We simply continue filter chain and let Security handle auth failure.
            filterChain.doFilter(request, response);
            return;
        }

        // 5) If username exists and SecurityContext has no authentication yet, validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details from DB (this will provide username, password, roles)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate token (signature, expiry, and subject match)
            if (JwtService.validateToken(token, userDetails.getUsername())) {

                // Create an Authentication token using userDetails and authorities
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Attach request details (IP, session id, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in SecurityContext — user is now authenticated for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6) Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
