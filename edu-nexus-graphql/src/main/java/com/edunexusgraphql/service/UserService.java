package com.edunexusgraphql.service;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.dto.PasswordChangeDTO;
import com.edunexusgraphql.service.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private static final String BASE_URL = "https://edu-nexus-user-service/users";
    private final RestTemplate restTemplate;

    @Autowired
    public UserService(@LoadBalanced RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public User createUser(String name, String email, String password) {
        UserDTO userDTO = UserDTO.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();
        return restTemplate.postForObject(BASE_URL, userDTO, User.class);
    }

    @Cacheable(value = "user", key = "#userId")
    public Optional<User> findById(Long userId) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .path("/{userId}")
                .buildAndExpand(userId)
                .toUriString();
        User user = restTemplate.getForObject(url, User.class);
        return Optional.ofNullable(user);
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword) {
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

}