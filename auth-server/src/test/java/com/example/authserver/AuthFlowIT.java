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
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

        Map<?, ?> tokenResponse = restClient.post()
                .uri("http://localhost:9000/oauth2/token")
                .headers(headers -> {
                    headers.setBasicAuth("ahmet", "12345");
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
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

       try {
           Map<?, ?> tokenResponse = restClient.post()
                   .uri("http://localhost:9000/oauth2/token")
                   .headers(headers -> {
                       headers.setBasicAuth("ahmett", "12345");
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
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "product.read")
                .build()
                .getQuery();

        try {
            Map<?, ?> tokenResponse = restClient.post()
                    .uri("http://localhost:9000/oauth2/token")
                    .headers(headers -> {
                        headers.setBasicAuth("ahmet", "123455");
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

