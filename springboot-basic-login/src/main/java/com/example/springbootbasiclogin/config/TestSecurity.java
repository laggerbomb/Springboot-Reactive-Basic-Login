package com.example.springbootbasiclogin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class TestSecurity {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http,
                                                       ReactiveUserDetailsService reactiveUserDetailsService) {
        http
            .authorizeExchange(exchanges ->
                exchanges
                    .pathMatchers(HttpMethod.GET, "/v3/api-docs",
                    "/configuration/**", "/swagger/**", "/webjars/**").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                    .anyExchange().permitAll()
            )
            .httpBasic(Customizer.withDefaults())
            .authenticationManager(authenticationManager(reactiveUserDetailsService))
            .csrf(ServerHttpSecurity.CsrfSpec::disable); // Disable CSRF for simplicity, adapt as needed

        return http.build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService) {
        return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    }
}
