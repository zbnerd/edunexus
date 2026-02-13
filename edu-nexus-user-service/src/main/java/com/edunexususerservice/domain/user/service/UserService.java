package com.edunexususerservice.domain.user.service;

import com.edunexususerservice.domain.user.exception.DuplicateUserException;
import com.edunexususerservice.domain.user.exception.InvalidPasswordException;
import com.edunexususerservice.domain.user.exception.NotFoundException;
import com.edunexususerservice.domain.user.dto.PasswordChangeDto;
import com.edunexususerservice.domain.user.dto.UserDto;
import com.edunexususerservice.domain.user.dto.UserLoginHistoryDto;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.entity.UserLoginHistory;
import com.edunexususerservice.domain.user.repository.UserLoginHistoryRepository;
import com.edunexususerservice.domain.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserLoginHistoryRepository userLoginHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user account.
     * Password is encrypted before storage using bcrypt.
     *
     * @param name the user's display name
     * @param email the user's email address (must be unique)
     * @param password the raw password (will be encrypted)
     * @return the created User entity
     * @throws DuplicateUserException if a user with this email already exists
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    @Transactional
    public User signUp(String name, String email, String password) {
        User user = new User();

        userRepository.findByEmail(email).ifPresent(existing -> {
            throw new DuplicateUserException("User already exists. " + email);
        });

        user.setUserInfo(
                UserDto.builder()
                        .name(name)
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .build()
        );

        return userRepository.save(user);
    }

    /**
     * Retrieves the login history for a specific user.
     *
     * @param userId the user's unique identifier
     * @return list of login history records
     * @throws NotFoundException if user doesn't exist
     */
    public List<UserLoginHistory> getUserLoginHistories(Long userId) {
        return getUserById(userId)
                .map(User::getLoginHistories)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }

    /**
     * Updates a user's password after validating the old password.
     * Uses optimistic locking to prevent concurrent modifications.
     *
     * @param userId the user's unique identifier
     * @param passwordChangeDto containing old and new passwords
     * @return the updated User entity
     * @throws NotFoundException if user doesn't exist
     * @throws InvalidPasswordException if old password doesn't match
     * @throws IllegalStateException if optimistic lock conflict occurs
     */
    @Transactional
    public User updatePassword(Long userId, PasswordChangeDto passwordChangeDto) {
        try {
            User user = requireById(userId);

            if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), user.getPasswordHash())) {
                throw new InvalidPasswordException("User password does not match");
            }

            user.updatePassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));

            return user;
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            log.warn("Optimistic lock conflict while updating password for user: {}", userId, e);
            throw new IllegalStateException("User was modified by another transaction. Please try again.", e);
        }
    }



    /**
     * Retrieves a user by their ID.
     *
     * @param userId the user's unique identifier
     * @return Optional containing the user if found
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param userEmail the user's email address
     * @return Optional containing the user if found
     */
    public Optional<User> getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail);
    }

    /**
     * Records a user login event with timestamp and IP address.
     *
     * @param userId the user's unique identifier
     * @param ipAddress the IP address from which the user logged in
     * @throws NotFoundException if user doesn't exist
     */
    @Transactional
    public void logUserLogin(Long userId, String ipAddress) {
        UserLoginHistory userLoginHistory = new UserLoginHistory();
        User user = requireById(userId);
        userLoginHistory.setUserLoginHistory(
                UserLoginHistoryDto.builder()
                        .user(user)
                        .loginTime(LocalDateTime.now())
                        .ipAddress(ipAddress)
                        .build()
        );

        userLoginHistoryRepository.save(userLoginHistory);
    }

    private User requireById(Long userId) {
        return getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * Retrieves a user by email or throws exception if not found.
     * Convenience method for cases where user existence is required.
     *
     * @param userEmail the user's email address
     * @return the User entity
     * @throws NotFoundException if no user exists with this email
     */
    public User requireByEmail(String userEmail) {
        return getUserByEmail(userEmail).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
