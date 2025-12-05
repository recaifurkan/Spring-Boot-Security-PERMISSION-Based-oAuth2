package com.example.authserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

class AuthFlowIT {

    private final RestClient restClient = RestClient.builder().build();

    @Test
    void shouldFetchAccesToken() {
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

    }

    @Test
    void shouldNotFetchWithWrongClientId() {
        String formBody = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "password")
                .queryParam("username", "mehmet")
                .queryParam("password", "12345")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

       try {
           Map<?, ?> tokenResponse = restClient.post()
                   .uri("http://localhost:9000/oauth2/token")
                   .headers(headers -> {
                       headers.setBasicAuth("my-clientt", "my-secret");
                       headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                   })
                   .body(formBody)
                   .retrieve()
                   .body(Map.class);

           String accessToken = tokenResponse != null ? String.valueOf(tokenResponse.get("access_token")) : null;
           assertThat(accessToken).isNotBlank();
       }catch (HttpClientErrorException e) {
           assertThat(e.getStatusCode().value()).isEqualTo(401);
           System.out.println(e.getResponseBodyAsString());
       }

    }

    @Test
    void shouldNotFetchWithWrongClientSecret() {
        String formBody = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "password")
                .queryParam("username", "mehmet")
                .queryParam("password", "12345")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

        try {
            Map<?, ?> tokenResponse = restClient.post()
                    .uri("http://localhost:9000/oauth2/token")
                    .headers(headers -> {
                        headers.setBasicAuth("my-client", "my-secrett");
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .body(formBody)
                    .retrieve()
                    .body(Map.class);

            String accessToken = tokenResponse != null ? String.valueOf(tokenResponse.get("access_token")) : null;
            assertThat(accessToken).isNotBlank();
        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode().value()).isEqualTo(401);
            System.out.println(e.getResponseBodyAsString());
        }

    }

    @Test
    void shouldNotFetchWithWrongUsername() {
        String formBody = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "password")
                .queryParam("username", "mehmett")
                .queryParam("password", "12345")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

        try {
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
        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode().value()).isEqualTo(401);
            System.out.println(e.getResponseBodyAsString());
        }

    }
    @Test
    void shouldNotFetchWithWrongUserPassword() {
        String formBody = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "password")
                .queryParam("username", "mehmet")
                .queryParam("password", "123456")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

        try {
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
        }catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode().value()).isEqualTo(401);
            System.out.println(e.getResponseBodyAsString());
        }

    }
}

