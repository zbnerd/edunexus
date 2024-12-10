package com.edunexususerservice.domain.user.controller;

import com.edunexususerservice.domain.exception.NotFoundException;
import com.edunexususerservice.domain.user.dto.PasswordChangeDto;
import com.edunexususerservice.domain.user.dto.UserDto;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.entity.UserLoginHistory;
import com.edunexususerservice.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
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
    public ResponseEntity<User> signUp(
            @RequestBody UserDto userDto
    ) {
        User user = userService.signUp(userDto.getName(), userDto.getEmail(), userDto.getPassword());
        return ResponseEntity.created(URI.create("/users/" + user.getId())).body(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/password-change")
    public ResponseEntity<User> changePassword(
            @PathVariable Long userId,
            @RequestBody PasswordChangeDto passwordChangeDto
    ) {
        return ResponseEntity.ok(userService.updatePassword(userId, passwordChangeDto));
    }

    @GetMapping("/{userId}/login-histories")
    public ResponseEntity<List<UserLoginHistory>> getLoginHistories(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserLoginHistories(userId));
    }
}
