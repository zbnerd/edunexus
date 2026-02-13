package com.edunexusgraphql.port.client.impl;

import com.edunexusgraphql.model.User;
import com.edunexusgraphql.port.client.UserClient;
import com.edunexusgraphql.service.dto.PasswordChangeDTO;
import com.edunexusgraphql.service.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RestTemplate-based implementation of UserClient.
 * Handles HTTP communication with the user service.
 */
@Slf4j
@Component
public class UserRestClient implements UserClient {

    private static final String BASE_URL = "https://edu-nexus-user-service/users";
    private final RestTemplate restTemplate;

    public UserRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public User createUser(String name, String email, String password) {
        log.debug("Creating user: email={}", email);
        UserDTO userDTO = UserDTO.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();
        return restTemplate.postForObject(BASE_URL, userDTO, User.class);
    }

    @Override
    @Cacheable(value = "user", key = "#userId")
    public Optional<User> findById(Long userId) {
        log.debug("Finding user by id: {}", userId);
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .path("/{userId}")
                .buildAndExpand(userId)
                .toUriString();
        User user = restTemplate.getForObject(url, User.class);
        return Optional.ofNullable(user);
    }

    @Override
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        log.debug("Changing password for userId={}", userId);
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .oldPassword(oldPassword)
                .newPassword(newPassword)
                .build();
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .path("/{userId}/password-change")
                .buildAndExpand(userId)
                .toUriString();
        restTemplate.postForLocation(url, passwordChangeDTO);
    }

    @Override
    public Map<Long, User> findUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        log.debug("Finding {} users by ids", userIds.size());
        String ids = userIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .path("/batch")
                .queryParam("ids", ids)
                .build()
                .toUriString();

        User[] users = restTemplate.getForObject(url, User[].class);
        if (users == null) {
            return Collections.emptyMap();
        }

        return Arrays.stream(users)
                .collect(Collectors.toMap(User::getId, user -> user));
    }
}
