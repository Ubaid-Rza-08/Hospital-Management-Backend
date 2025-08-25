package com.ubaid.Auth_service.config;

import com.ubaid.Auth_service.repository.UserRepository;
import com.ubaid.Auth_service.security.AuthUtil;
import com.ubaid.Auth_service.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
public class SecurityBeansConfig {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(userRepository, authUtil, handlerExceptionResolver);
    }
}

