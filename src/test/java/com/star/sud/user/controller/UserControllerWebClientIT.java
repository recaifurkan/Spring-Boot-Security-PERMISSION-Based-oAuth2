package com.star.sud.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.star.sud.user.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
@ActiveProfiles("test")
class UserControllerWebClientIT {

    private static final Logger log = LoggerFactory.getLogger(UserControllerWebClientIT.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    @Value("${test.webclient.base-url:http://localhost:8090}")
    private String baseUrl;

    @Value("${test.credentials.username:admin}")
    private String username;

    @Value("${test.credentials.password:password}")
    private String password;

    @Value("${test.credentials.client-id:admin}")
    private String clientId;

    @Value("${test.credentials.client-secret:password}")
    private String clientSecret;

    @BeforeEach
    void setUpClient() {
        restTemplate = new RestTemplate();
    }

    @Test
    @DisplayName("should fetch users list with valid token via WebClient")
    void shouldListUsers() {
        String token = requestToken();

        ResponseEntity<UserApiResponse> response = restTemplate.exchange(
            baseUrl + "/users",
            HttpMethod.GET,
            new HttpEntity<>(bearerHeaders(token)),
            UserApiResponse.class
        );

        logResponse("shouldListUsers", response);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getResult()).isNotNull();
    }

    @Test
    @DisplayName("should fetch user by id")
    void shouldGetUserById() {
        String token = requestToken();
        UserDto createdUser = createTestUser(token);

        ResponseEntity<UserApiResponse> response = restTemplate.exchange(
            baseUrl + "/users/" + createdUser.getUserId(),
            HttpMethod.GET,
            new HttpEntity<>(bearerHeaders(token)),
            UserApiResponse.class
        );

        logResponse("shouldGetUserById", response);
        assertThat(response.getBody()).isNotNull();
        UserDto fetched = convertResult(response.getBody().getResult());
        assertThat(fetched.getUserId()).isEqualTo(createdUser.getUserId());
        assertThat(fetched.getEmail()).isEqualTo(createdUser.getEmail());

        deleteUser(token, createdUser.getUserId());
    }

    @Test
    @DisplayName("should create user")
    void shouldCreateUser() {
        String token = requestToken();
        UserDto created = createTestUser(token);

        assertThat(created.getUserId()).isNotNull();
        assertThat(created.getUsername()).isNotBlank();

        deleteUser(token, created.getUserId());
    }

    @Test
    @DisplayName("should update existing user")
    void shouldUpdateUser() {
        String token = requestToken();
        UserDto created = createTestUser(token);

        UserDto updateRequest = new UserDto();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");

        ResponseEntity<UserApiResponse> updateResponse = restTemplate.exchange(
            baseUrl + "/users/" + created.getUserId(),
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, bearerHeaders(token)),
            UserApiResponse.class
        );

        logResponse("shouldUpdateUser", updateResponse);
        assertThat(updateResponse.getBody()).isNotNull();
        UserDto updated = convertResult(updateResponse.getBody().getResult());
        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getLastName()).isEqualTo("User");

        deleteUser(token, created.getUserId());
    }

    @Test
    @DisplayName("should delete user")
    void shouldDeleteUser() {
        String token = requestToken();
        UserDto created = createTestUser(token);

        deleteUser(token, created.getUserId());

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl + "/users/" + created.getUserId(),
            HttpMethod.GET,
            new HttpEntity<>(bearerHeaders(token)),
            UserApiResponse.class
        )).isInstanceOf(HttpClientErrorException.class);
    }

    private String requestToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);
        formData.add("scope", "read write trust");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
            baseUrl + "/oauth/token",
            new HttpEntity<>(formData, headers),
            Map.class
        );

        assertThat(tokenResponse.getBody()).isNotNull();
        Object accessToken = tokenResponse.getBody().get("access_token");
        assertThat(accessToken).isInstanceOf(String.class);
        return (String) accessToken;
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private UserDto createTestUser(String token) {
        UserDto request = randomUserDto();

        ResponseEntity<UserApiResponse> response = restTemplate.exchange(
            baseUrl + "/users",
            HttpMethod.POST,
            new HttpEntity<>(request, bearerHeaders(token)),
            UserApiResponse.class
        );

        logResponse("createTestUser", response);
        assertThat(response.getBody()).isNotNull();
        return convertResult(response.getBody().getResult());
    }

    private void deleteUser(String token, Long userId) {
        ResponseEntity<UserApiResponse> response = restTemplate.exchange(
            baseUrl + "/users/" + userId,
            HttpMethod.DELETE,
            new HttpEntity<>(bearerHeaders(token)),
            UserApiResponse.class
        );

        logResponse("deleteUser", response);
    }

    private UserDto convertResult(Object result) {
        return objectMapper.convertValue(result, UserDto.class);
    }

    private UserDto randomUserDto() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UserDto dto = new UserDto();
        dto.setFirstName("Integration");
        dto.setLastName("User" + suffix);
        dto.setUsername("it_user_" + suffix);
        dto.setPassword("P@ss" + suffix);
        dto.setEmail("it_user_" + suffix + "@example.com");
        dto.setRole(Collections.singletonList("USER"));
        return dto;
    }

    private void logResponse(String label, ResponseEntity<UserApiResponse> response) {
        if (response == null) {
            log.warn("{}: response is null", label);
            return;
        }
        try {
            log.info("{} -> status={}, body={}", label, response.getStatusCodeValue(),
                objectMapper.writeValueAsString(response.getBody()));
        } catch (Exception ex) {
            log.warn("{} -> status={}, body serialization failed: {}", label,
                response.getStatusCodeValue(), ex.getMessage());
        }
    }

    static class UserApiResponse {
        private int status;
        private String message;
        private Object result;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }
    }
}
