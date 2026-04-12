package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // Tell Spring Security to use Vaadin's request cache
                .requestCache(cache -> cache
                        .requestCache(new HttpSessionRequestCache()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup").permitAll() //public
                        .requestMatchers("/VAADIN/**", "/vaadinServlet/**", "/frontend/**").permitAll()
                        .requestMatchers("/connect/**", "/PUSH/**").permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/"))
                );

        return http.build();
    }
}