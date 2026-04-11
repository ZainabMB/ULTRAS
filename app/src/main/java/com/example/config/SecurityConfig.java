package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF — required for Vaadin to work
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Public routes
                        .requestMatchers("/", "/login", "/signup").permitAll()
                        // Vaadin internal routes — must be permitted
                        .requestMatchers("/VAADIN/**", "/vaadinServlet/**", "/frontend/**").permitAll()
                        // Everything else requires login
                        .anyRequest().authenticated()
                )

                // Redirect unauthenticated users back to home
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/"))
                );

        return http.build();
    }
}