package com.todobackend.config;

import com.todobackend.security.exception.SecurityExceptionHandler;
import com.todobackend.security.jwt.JwtAuthenticationFilter;
import com.todobackend.security.principal.UserPrincipalService;
import com.todobackend.security.rateLimit.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserPrincipalService userPrincipalService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityExceptionHandler securityExceptionHandler;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userPrincipalService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;

    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationProvider(
                        authenticationProvider()
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        securityExceptionHandler
                                )
                                .accessDeniedHandler(
                                        securityExceptionHandler
                                )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/v1/api-docs/**",
                                "/v1/swagger-ui/**",
                                "/v1/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/posts"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/verify-reset-otp",
                                "/api/v1/auth/reset-password",
                                "/api/v1/profile/email/confirm"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        rateLimitFilter,
                        JwtAuthenticationFilter.class
                );

        return http.build();
    }

}
