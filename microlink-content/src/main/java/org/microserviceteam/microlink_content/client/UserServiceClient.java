package org.microserviceteam.microlink_content.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {
    
    private final RestTemplate restTemplate;

    @Value("${microlink.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    public UserServiceClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public UserDTO getUser(String userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String token = null;
            if (authentication != null && authentication.getCredentials() instanceof String) {
                token = (String) authentication.getCredentials();
            }

            if (token != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                return restTemplate.exchange(
                    userServiceUrl + "/api/user/" + userId,
                    HttpMethod.GET,
                    entity,
                    UserDTO.class
                ).getBody();
            } else {
                return restTemplate.getForObject(userServiceUrl + "/api/user/" + userId, UserDTO.class);
            }
        } catch (Exception e) {
            // Fallback
            UserDTO fallback = new UserDTO();
            try {
                fallback.setId(Long.parseLong(userId));
            } catch (NumberFormatException ex) {
                fallback.setId(0L);
            }
            fallback.setUsername("Unknown User");
            return fallback;
        }
    }
}
