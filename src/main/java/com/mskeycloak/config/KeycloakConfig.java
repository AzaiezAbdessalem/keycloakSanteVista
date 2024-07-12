package com.mskeycloak.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    @Getter
    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Bean
    protected Keycloak initKeycloakAdmin() {
        log.info("Initializing Keycloak admin client for realm: {}", realm);
        log.debug("Client ID: {}", clientId);

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(adminUsername)
                .password(adminPassword)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
        try {
            log.debug("Keycloak instance: {}", keycloak);

            AccessTokenResponse accessTokenResponse = keycloak.tokenManager().grantToken();
            if (accessTokenResponse != null) {
                log.info("Access token obtained successfully");
            } else {
                log.error("Failed to obtain access token: accessTokenResponse is null");
            }
        } catch (jakarta.ws.rs.BadRequestException e) {
            String responseBody = e.getResponse().readEntity(String.class);
            log.error("HTTP 400 Bad Request: {}", responseBody);
            e.printStackTrace();
        } catch (jakarta.ws.rs.NotFoundException e) {
            log.error("HTTP 404 Not Found: {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            e.printStackTrace();
        }

        return keycloak;
    }

    public Keycloak instantiateKeycloakUser(String username, String password) {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("password")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .build();
    }
}
