package com.tpg.connect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for Connect Dating App
 * 
 * Provides JWT-based authentication while maintaining frontend compatibility.
 * Allows auth endpoints, health checks, and documentation to remain accessible
 * while securing all other API endpoints.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("🔒 Configuring Spring Security with JWT authentication");
        
        http
            // Enable CORS with existing configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Disable CSRF for REST API (not needed for stateless JWT)
            .csrf(csrf -> csrf.disable())
            
            // Configure session management (stateless for JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Allow OPTIONS requests (CORS preflight) - CRITICAL for frontend
                .requestMatchers("OPTIONS", "/**").permitAll()
                
                // Allow health check and documentation (needed for monitoring)
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/").permitAll()
                
                // Allow ALL auth endpoints without authentication (needed for login/register)
                .requestMatchers("/api/auth/**").permitAll()
                
                // Allow WebSocket connections (if used for real-time messaging)
                .requestMatchers("/simple-ws/**").permitAll()
                
                // ALL other API requests require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Allow any other requests (for static content, etc.)
                .anyRequest().permitAll()
            )
            
            // Add JWT authentication filter before Spring Security's default authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("✅ Spring Security configured successfully");
        logger.info("🔓 Public endpoints: /api/auth/**, /actuator/**, /swagger-ui/**, OPTIONS requests");
        logger.info("🔒 Protected endpoints: /api/** (except auth)");
        
        return http.build();
    }
}