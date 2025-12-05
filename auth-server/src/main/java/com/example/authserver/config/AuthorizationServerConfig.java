package com.example.authserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class AuthorizationServerConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator(
            JWKSource<SecurityContext> jwkSource,
            OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {

        // JWT encoder
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);

        // Access token generator
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtCustomizer);

        // Refresh token generator
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        // Coppose generators (JWT + Refresh)
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
    }


    // Authorization server settings
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .tokenEndpoint("/oauth2/token")
                .jwkSetEndpoint("/oauth2/jwks")
                .issuer("http://auth-server:9000")
                .build();
    }

    // JWK source for JWT signing (simple RSA keypair)
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    // Authorization persistence (in-memory) — prod'ta JdbcOAuth2AuthorizationService kullan
    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    // Token generator — use default Jwt generator if available, otherwise fallback
    // Spring Boot usually auto-configures an OAuth2TokenGenerator bean; if not, you'll need to create one.
    // For most setups the default auto-config will provide one because jwkSource is present.

    // AuthenticationManager for user authentication (DAO)
    @Bean
    public AuthenticationManager userAuthenticationManager(UserDetailsService userDetailsService,
                                                           PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider(userDetailsService);
        dao.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(dao);
    }

    // Put user scopes into JWT claims (so resource server can authorize based on SCOPE_xxx)
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                Authentication principal = context.getPrincipal();
                // Convert authorities SCOPE_xxx -> xxx and put into 'scope' claim (array)
                List<String> scopes = principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(a -> a.startsWith("SCOPE_"))
                        .map(a -> a.substring("SCOPE_".length()))
                        .distinct()
                        .collect(Collectors.toList());

                if (!scopes.isEmpty()) {
                    // Add scopes as normal space separated for compatibility and also as array
                    String scopeStr = String.join(" ", scopes);
                    context.getClaims().claim(OAuth2ParameterNames.SCOPE, scopeStr);
                    context.getClaims().claim("scope", scopes);
                }

            }
        };
    }


    // RSA key generation helper
    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    // -------------------------
    // Inner classes: converter + token + provider
    // -------------------------

    /**
     * Authentication token that carries client principal + username/password
     */
    public static class PasswordGrantAuthenticationToken extends AbstractAuthenticationToken {

        private final Authentication clientPrincipal;
        private final String username;
        private final String password;

        // before authentication
        public PasswordGrantAuthenticationToken(Authentication clientPrincipal, String username, String password) {
            super(clientPrincipal.getAuthorities());
            this.clientPrincipal = clientPrincipal;
            this.username = username;
            this.password = password;
            setAuthenticated(false);
        }


        @Override
        public Object getCredentials() {
            return this.password;
        }

        @Override
        public Object getPrincipal() {
            return this.username;
        }

        public Authentication getClientPrincipal() {
            return this.clientPrincipal;
        }
    }

    /**
     * Converter: HttpServletRequest -> PasswordGrantAuthenticationToken
     */
    public static class PasswordGrantAuthenticationConverter implements AuthenticationConverter {

        @Override
        public Authentication convert(HttpServletRequest request) {
            String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
            if (!"password".equals(grantType)) {
                return null;
            }

            Authentication clientPrincipal = (Authentication) request.getUserPrincipal();

            String username = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
            String password = request.getParameter(OAuth2ParameterNames.CLIENT_SECRET);

            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                return null; // let framework handle missing param errors
            }

            return new PasswordGrantAuthenticationToken(clientPrincipal, username, password);
        }
    }

    /**
     * Provider: authenticate the resource owner and issue an access token (no refresh token here).
     */
    public static class PasswordGrantAuthenticationProvider implements AuthenticationProvider {

        private final RegisteredClientRepository registeredClientRepository;
        private final OAuth2AuthorizationService authorizationService;
        private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
        private final AuthenticationManager authenticationManager;

        public PasswordGrantAuthenticationProvider(RegisteredClientRepository registeredClientRepository,
                                                   OAuth2AuthorizationService authorizationService,
                                                   OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                   AuthenticationManager authenticationManager) {
            this.registeredClientRepository = registeredClientRepository;
            this.authorizationService = authorizationService;
            this.tokenGenerator = tokenGenerator;
            this.authenticationManager = authenticationManager;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            if (!(authentication instanceof PasswordGrantAuthenticationToken)) {
                return null;
            }

            PasswordGrantAuthenticationToken resourceAuth = (PasswordGrantAuthenticationToken) authentication;

            Authentication clientPrincipal = resourceAuth.getClientPrincipal();
            String clientId = null;

            if (clientPrincipal instanceof OAuth2ClientAuthenticationToken) {
                OAuth2ClientAuthenticationToken clientAuth = (OAuth2ClientAuthenticationToken) clientPrincipal;
                clientId = clientAuth.getRegisteredClient().getClientId();
            } else if (clientPrincipal != null && clientPrincipal.getName() != null) {
                clientId = clientPrincipal.getName();
            }

            if (clientId == null) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
            }

            RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
            if (registeredClient == null) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
            }



            // Authenticate the resource owner (username/password)
            UsernamePasswordAuthenticationToken userToken =
                    new UsernamePasswordAuthenticationToken(resourceAuth.getPrincipal(), resourceAuth.getCredentials());
            Authentication userAuth = this.authenticationManager.authenticate(userToken);
            if (userAuth == null || !userAuth.isAuthenticated()) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
            }

            // Determine scopes: use client-registered scopes intersected with user's authorities (SCOPE_)
            Set<String> authorizedScopes = new HashSet<>(registeredClient.getScopes());

            // Narrow by user's SCOPE_* authorities if present
            Set<String> userScopes = userAuth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("SCOPE_"))
                    .map(a -> a.substring("SCOPE_".length()))
                    .collect(Collectors.toSet());

            // intersect registered client scopes and user scopes (if userScopes not empty)
            if (!userScopes.isEmpty()) {
                authorizedScopes.retainAll(userScopes);
            }

            // Build token context for access token generation
            Instant issuedAt = Instant.now();
            // Let tokenGenerator produce proper token (JWT etc.)
            DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(userAuth)
                    .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                    .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .authorizedScopes(authorizedScopes);

            OAuth2TokenContext tokenContext = tokenContextBuilder.build();

            OAuth2Token generated = this.tokenGenerator.generate(tokenContext);
            if (generated == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "token_generation_failed", null));
            }

            OAuth2AccessToken accessToken;
            if (generated instanceof OAuth2AccessToken) {
                accessToken = (OAuth2AccessToken) generated;
            } else {
                // wrap generic token as access token if necessary
                accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                        generated.getTokenValue(), generated.getIssuedAt(), generated.getExpiresAt(), authorizedScopes);
            }

            // Build authorization and persist
            OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                    .principalName(userAuth.getName())
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .token(accessToken, metadata -> {
                    })
                    .build();

            this.authorizationService.save(authorization);

            // Return OAuth2AccessTokenAuthenticationToken (no refresh token)
            return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken);
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return PasswordGrantAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }
}
