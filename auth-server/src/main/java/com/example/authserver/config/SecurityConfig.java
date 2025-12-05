package com.example.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // ---------------------------------------------------------
    // 1) AUTHORIZATION SERVER SECURITY (Order 1)
    // ---------------------------------------------------------
    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurity(HttpSecurity http,
                                                  RegisteredClientRepository registeredClientRepository,
                                                  OAuth2AuthorizationService authorizationService,
                                                  OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                  AuthenticationManager authenticationManager) throws Exception {

        // Default AS security
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // Enable form login for AS pages
        http.formLogin(Customizer.withDefaults());

        // Password grant config
        OAuth2AuthorizationServerConfigurer configurer =
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);


        // Password Grant
        configurer
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

        // CLIENT auth errorları YAKALAYAN yer
        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(new CustomClientAuthEntryPoint());
        });


        return http.build();
    }

    // ---------------------------------------------------------
    // 2) NORMAL WEB SECURITY (Order 2)
    // LOGIN PAGE, CONSENT PAGE BURADA ÇALIŞIR
    // ---------------------------------------------------------
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurity(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults()); // <-- login page HERE


        return http.build();
    }
}
