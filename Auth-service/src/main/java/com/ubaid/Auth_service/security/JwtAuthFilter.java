package com.ubaid.Auth_service.security;

import com.ubaid.Auth_service.entity.User;
import com.ubaid.Auth_service.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            log.info("Incoming request: {}", request.getRequestURI());

            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(authUtil.getSecretKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userIdStr = claims.getSubject();
                if (userIdStr == null) {
                    log.warn("No subject found in JWT token");
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId = Long.parseLong(userIdStr);
                List<String> roles = claims.get("roles", List.class);
                String username = claims.get("username", String.class);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findById(userId).orElseThrow(() ->
                            new UsernameNotFoundException("User not found with id: " + userId));

                    // Convert roles to GrantedAuthorities with ROLE_ prefix
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (roles != null && !roles.isEmpty()) {
                        authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());
                    }

                    // Also add permission-based authorities
                    Set<SimpleGrantedAuthority> permissionAuthorities = user.getRoles().stream()
                            .flatMap(role -> RolePermissionMapping.getAuthoritiesForRole(role).stream())
                            .collect(Collectors.toSet());
                    authorities.addAll(permissionAuthorities);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authToken.setDetails(Map.of(
                            "userId", userId,
                            "username", username,
                            "roles", roles
                    ));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("User authenticated: {} with roles: {}", username, roles);
                }

                filterChain.doFilter(request, response);
            } catch (ExpiredJwtException e) {
                log.error("JWT token is expired: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Token expired\"}");
                return;
            } catch (JwtException e) {
                log.error("Invalid JWT token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid token\"}");
                return;
            }

        } catch (Exception ex) {
            log.error("Error processing JWT token: {}", ex.getMessage(), ex);
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
