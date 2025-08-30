package com.ubaid.Auth_service.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static com.ubaid.Auth_service.entity.type.PermissionType.*;
import static com.ubaid.Auth_service.entity.type.RoleType.*;
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/public/**", "/auth/**", "/error").permitAll()

                        // Admin endpoints - only ADMIN can access
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Patient endpoints with specific role-based access
                        .requestMatchers(HttpMethod.POST, "/api/patients").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/patients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasRole("ADMIN")

                        // Doctor endpoints
                        .requestMatchers("/api/doctors/**").hasAnyRole("DOCTOR", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 error: {}", exception.getMessage());
                            handlerExceptionResolver.resolveException(request, response, null, exception);
                        })
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.error("Access denied for user: {} on path: {}",
                                    SecurityContextHolder.getContext().getAuthentication().getName(),
                                    request.getRequestURI());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"" +
                                    accessDeniedException.getMessage() + "\"}");
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.error("Authentication error: {}", authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Authentication required\",\"message\":\"" +
                                    authException.getMessage() + "\"}");
                        })
                );

        return httpSecurity.build();
    }
}
