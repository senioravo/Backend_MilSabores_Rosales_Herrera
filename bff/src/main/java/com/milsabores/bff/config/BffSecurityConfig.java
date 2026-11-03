package com.milsabores.bff.config;

import com.milsabores.bff.security.BffJwtAuthenticationFilter;
import com.milsabores.bff.security.BffPublicRoutePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class BffSecurityConfig {

    @Bean
    SecurityFilterChain bffSecurityFilterChain(
            HttpSecurity http, BffJwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(BffPublicRoutePolicy.publicPathMatcher())
                        .permitAll()
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/**", "/bff/**")
                        .authenticated()
                        .anyRequest()
                        .denyAll())
                .exceptionHandling(ex -> ex.accessDeniedHandler((request, response, denied) -> {
                    String path = request.getRequestURI();
                    String message = path != null && path.startsWith("/api/admin")
                            ? "Se requiere el rol ADMIN"
                            : "No tienes permiso para esta ruta";
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"" + message + "\"}");
                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
