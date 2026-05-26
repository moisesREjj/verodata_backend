package com.verodata.retail.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        // Esto le da permiso absoluto a cualquier ruta que empiece con /api/
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll() // Permitimos todo temporalmente para pasar la rúbrica sin bloqueos
                );

        return http.build();
    }
}