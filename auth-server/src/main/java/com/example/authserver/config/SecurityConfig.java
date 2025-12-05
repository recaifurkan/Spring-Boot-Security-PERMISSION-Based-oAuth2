package com.example.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurity(HttpSecurity http,
                                                  RegisteredClientRepository registeredClientRepository,
                                                  OAuth2AuthorizationService authorizationService,
                                                  OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                  AuthenticationManager authenticationManager) throws Exception {

        // ---- CRITICAL ----
        http.securityMatcher("/oauth2/**", "/.well-known/**");

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http.apply(authorizationServerConfigurer);

        // Password grant
        authorizationServerConfigurer
                .tokenEndpoint(endpoint -> endpoint
                        .accessTokenRequestConverter(
                                new AuthorizationServerConfig.PasswordGrantAuthenticationConverter()
                        )
                        .authenticationProvider(
                                new AuthorizationServerConfig.PasswordGrantAuthenticationProvider(
                                        registeredClientRepository,
                                        authorizationService,
                                        tokenGenerator,
                                        authenticationManager
                                )
                        )
                );

        // Client auth hataları
        http.exceptionHandling(ex ->
                ex.authenticationEntryPoint(new CustomClientAuthEntryPoint())
        );

        // token POST'u için CSRF kapatma
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/oauth2/token"));

        return http.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurity(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/**")   // tüm diğer pathler
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

}
