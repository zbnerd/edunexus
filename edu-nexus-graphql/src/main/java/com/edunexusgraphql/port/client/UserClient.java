package com.edunexusgraphql.port.client;

import com.edunexusgraphql.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client interface for user service operations.
 * Abstracts HTTP/RestTemplate concerns from the application layer.
 */
public interface UserClient {

    /**
     * Create a new user.
     *
     * @param name User name
     * @param email User email
     * @param password User password
     * @return Created user
     */
    User createUser(String name, String email, String password);

    /**
     * Find a user by ID.
     *
     * @param userId User ID
     * @return Optional user
     */
    Optional<User> findById(Long userId);

    /**
     * Change user password.
     *
     * @param userId User ID
     * @param oldPassword Current password
     * @param newPassword New password
     */
    void changePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * Find multiple users by their IDs.
     *
     * @param userIds List of user IDs
     * @return Map of user ID to User details
     */
    Map<Long, User> findUsersByIds(List<Long> userIds);
}
