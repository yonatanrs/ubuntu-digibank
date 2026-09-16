package com.ubuntu.bank.payment.infrastructure;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;

public final class SecurityConfiguration {
    private SecurityConfiguration() {}

    @Configuration
    @Profile("production")
    static class ProductionSecurity {
        @Bean SecurityFilterChain productionFilterChain(HttpSecurity http) throws Exception {
            return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                    .contentTypeOptions(Customizer.withDefaults())
                    .frameOptions(frame -> frame.deny()))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/operations/**").hasAuthority("SCOPE_payments.operations.read")
                    .requestMatchers(HttpMethod.GET, "/api/v1/payments/**").hasAuthority("SCOPE_payments.read")
                    .requestMatchers(HttpMethod.POST, "/api/v1/payments/**").hasAuthority("SCOPE_payments.write")
                    .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
        }
    }

    @Configuration
    @Profile("!production")
    static class NonProductionSecurity {
        @Bean SecurityFilterChain localFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }
    }
}
