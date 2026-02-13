package com.edunexusgraphql.service;

import com.edunexusgraphql.model.User;
import com.edunexusgraphql.port.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for user-related operations.
 * Delegates HTTP client operations to UserClient interface.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserClient userClient;

    public User createUser(String name, String email, String password) {
        return userClient.createUser(name, email, password);
    }

    @Cacheable(value = "user", key = "#userId")
    public Optional<User> findById(Long userId) {
        return userClient.findById(userId);
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        userClient.changePassword(userId, oldPassword, newPassword);
    }

    public Map<Long, User> findUsersByIds(List<Long> userIds) {
        return userClient.findUsersByIds(userIds);
    }
}