package com.edunexususerservice.domain.user.controller;

import com.edunexus.common.exception.NotFoundException;
import com.edunexususerservice.domain.user.dto.PasswordChangeDto;
import com.edunexususerservice.domain.user.dto.UserDto;
import com.edunexususerservice.domain.user.dto.UserResponse;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.entity.UserLoginHistory;
import com.edunexususerservice.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> signUp(
            @RequestBody @jakarta.validation.Valid UserDto userDto
    ) {
        User user = userService.signUp(userDto.getName(), userDto.getEmail(), userDto.getPassword());
        return ResponseEntity.created(URI.create("/users/" + user.getId())).body(UserResponse.from(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/{userId}/password-change")
    public ResponseEntity<UserResponse> changePassword(
            @PathVariable Long userId,
            @RequestBody @jakarta.validation.Valid PasswordChangeDto passwordChangeDto
    ) {
        User user = userService.updatePassword(userId, passwordChangeDto);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/{userId}/login-histories")
    public ResponseEntity<List<UserLoginHistory>> getLoginHistories(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserLoginHistories(userId));
    }
}
