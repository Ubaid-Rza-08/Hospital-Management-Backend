package com.ubaid.Auth_service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static com.ubaid.Auth_service.entity.type.PermissionType.*;
import static com.ubaid.Auth_service.entity.type.RoleType.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrfConfig -> csrfConfig.disable())
                .sessionManagement(sessionConfig ->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/auth/**").permitAll()
                        // Patient endpoints
                        .requestMatchers(HttpMethod.POST, "/api/patients/**").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/patients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/admin/**").hasAnyAuthority(APPOINTMENT_DELETE.name(), USER_MANAGE.name())
                        .requestMatchers("/admin/**").hasRole(ADMIN.name())
                        .requestMatchers("/doctors/**").hasAnyRole(DOCTOR.name(), ADMIN.name())
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oAuth2 -> oAuth2
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 error: {}", exception.getMessage());
                            handlerExceptionResolver.resolveException(request, response, null, exception);
                        })
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exceptionHandlingConfigurer ->
                        exceptionHandlingConfigurer.accessDeniedHandler((request, response, accessDeniedException) -> {
                            handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
                        }));

        return httpSecurity.build();
    }

}
