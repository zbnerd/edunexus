package com.edunexususerservice.domain.user.service;

import com.edunexususerservice.domain.exception.DuplicateUserException;
import com.edunexususerservice.domain.exception.InvalidPasswordException;
import com.edunexususerservice.domain.exception.NotFoundException;
import com.edunexususerservice.domain.user.dto.PasswordChangeDto;
import com.edunexususerservice.domain.user.dto.UserDto;
import com.edunexususerservice.domain.user.dto.UserLoginHistoryDto;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.entity.UserLoginHistory;
import com.edunexususerservice.domain.user.repository.UserLoginHistoryRepository;
import com.edunexususerservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserLoginHistoryRepository userLoginHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signUp(String name, String email, String password) {
        User user = new User();

        if (userRepository.findByEmail(email).isPresent()){
            throw new DuplicateUserException("User already exists. " + email);
        }

        user.setUserInfo(
                UserDto.builder()
                        .name(name)
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .build()
        );

        return userRepository.save(user);
    }

    public List<UserLoginHistory> getUserLoginHistories(Long userId) {
        return getUserById(userId)
                .map(User::getLoginHistories)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updatePassword(Long userId, PasswordChangeDto passwordChangeDto) {
        User user = getUserByIdOrThrowToNotFoundException(userId);

        if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("User password does not match");
        }

        user.updatePassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));

        return user;
    }



    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail);
    }

    @Transactional
    public void logUserLogin(Long userId, String ipAddress) {
        UserLoginHistory userLoginHistory = new UserLoginHistory();
        User user = getUserByIdOrThrowToNotFoundException(userId);
        userLoginHistory.setUserLoginHistory(
                UserLoginHistoryDto.builder()
                        .user(user)
                        .loginTime(LocalDateTime.now())
                        .ipAddress(ipAddress)
                        .build()
        );

        userLoginHistoryRepository.save(userLoginHistory);
    }

    private User getUserByIdOrThrowToNotFoundException(Long userId) {
        return getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getUserByEmailOrThrowToNotFoundException(String userEmail) {
        return getUserByEmail(userEmail).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
