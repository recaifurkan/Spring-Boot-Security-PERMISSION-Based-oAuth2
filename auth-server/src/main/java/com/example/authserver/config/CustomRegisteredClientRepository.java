package com.example.authserver.config;

import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Primary
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final Map<String, RegisteredClient> users = new HashMap<>();

    public CustomRegisteredClientRepository(PasswordEncoder encoder) {

        // Listeyi istediğin gibi doldur
        users.put("ahmet",
                RegisteredClient
                        .withId(UUID.randomUUID().toString())
                        .clientId("ahmet")
                        .clientSecret(encoder.encode("12345"))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scopes((scopes) -> scopes.addAll(List.of("ROLE_USER", "product.read", "product.write")))
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .reuseRefreshTokens(false)
                                .build())
                        .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(false)
                                .build())
                        .build()
        );

        users.put("mehmet",
                RegisteredClient
                        .withId(UUID.randomUUID().toString())
                        .clientId("mehmet")
                        .clientSecret(encoder.encode("12345"))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scopes((scopes) -> scopes.add("ROLE_USER"))
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .reuseRefreshTokens(false)
                                .build())
                        .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(false)
                                .build())
                        .build()
        );
    }


    @Override
    public void save(RegisteredClient registeredClient) {
        this.users.put(registeredClient.getClientId(), registeredClient);

    }

    @Override
    public RegisteredClient findById(String id) {
        return this.users.get(id);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {


        return this.users.get(clientId);
    }
}
