package com.exam.exam.config;

import com.exam.exam.security.JwtAuthenticationFilter;
import com.exam.exam.config.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Turn off CSRF since we're using JWT
        http.csrf().disable();
        
        // Use stateless sessions for JWT
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        
        // Set up URL permissions
        http.authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()  // Login/register endpoints
                .requestMatchers("/h2-console/**").permitAll()  // Database console
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Admin only
                .requestMatchers("/api/orders/**").authenticated()  // Need to be logged in
                .requestMatchers("/api/products/**").authenticated()  // Need to be logged in
                .anyRequest().authenticated();
        
        // Add our custom filters
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(jwtAuthenticationFilter, RateLimitFilter.class);
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}