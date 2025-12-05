package com.example.authserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

class ProductFlowIT {

    private final RestClient restClient = RestClient.builder().build();

    @Test
    void shouldFetchProductsWithPasswordGrantToken() {
        String formBody = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "password")
                .queryParam("username", "ahmet")
                .queryParam("password", "12345")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

        Map<?, ?> tokenResponse = restClient.post()
                .uri("http://localhost:9000/oauth2/token")
                .headers(headers -> {
                    headers.setBasicAuth("my-client", "my-secret");
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body(formBody)
                .retrieve()
                .body(Map.class);

        String accessToken = tokenResponse != null ? String.valueOf(tokenResponse.get("access_token")) : null;
        assertThat(accessToken).isNotBlank();

        String products = restClient.get()
                .uri("http://localhost:8081/products")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(String.class);

        assertThat(products).isNotBlank();
    }


    @Test
    void shouldNotAuthorizedWithScopeNotPrivilege() {
        String formBody = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "password")
                .queryParam("username", "mehmet")
                .queryParam("password", "12345")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

        Map<?, ?> tokenResponse = restClient.post()
                .uri("http://localhost:9000/oauth2/token")
                .headers(headers -> {
                    headers.setBasicAuth("my-client", "my-secret");
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body(formBody)
                .retrieve()
                .body(Map.class);

        String accessToken = tokenResponse != null ? String.valueOf(tokenResponse.get("access_token")) : null;
        assertThat(accessToken).isNotBlank();

        try {
            String products = restClient.get()
                    .uri("http://localhost:8081/products")
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .body(String.class);
        }
        catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode().value()).isEqualTo(403);
            System.out.println(e.getResponseHeaders().get(HttpHeaders.WWW_AUTHENTICATE));
            return;
        }
    }
}

