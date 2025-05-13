package com.tienda.tienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // desactiva CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // permite todas las rutas
                )
                .formLogin(form -> form.disable())  // 🔥 desactiva el login por formulario
                .httpBasic(httpBasic -> httpBasic.disable());  // 🔒 desactiva la autenticación básica

        return http.build();
    }
    }


