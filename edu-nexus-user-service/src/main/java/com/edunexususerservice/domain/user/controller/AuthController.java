package com.edunexususerservice.domain.user.controller;

import com.edunexususerservice.domain.user.controller.request.LoginRequest;
import com.edunexususerservice.domain.user.controller.request.TokenRequest;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.service.JWTService;
import com.edunexususerservice.domain.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JWTService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            HttpServletRequest request,
            @RequestBody @jakarta.validation.Valid LoginRequest loginRequest
    ) {
        User existingUser = userService.requireByEmail(loginRequest.getEmail());
        String token = jwtService.login(existingUser, loginRequest.getPassword());
        String ipAddress = request.getRemoteAddr();

        userService.logUserLogin(existingUser.getId(), ipAddress);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @PostMapping("/validate")
    public ResponseEntity<User> validateToken(@RequestBody @jakarta.validation.Valid TokenRequest tokenRequest) {
        Claims claims = jwtService.parseJwtClaims(tokenRequest.getToken());
        return ResponseEntity.ok(userService.getUserByEmail(claims.getSubject())
                .orElseThrow(() -> new com.edunexususerservice.domain.user.exception.NotFoundException("User not found")));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Boolean>> verifyToken(
            @RequestBody @jakarta.validation.Valid TokenRequest tokenRequest
    ) {
        boolean isValid = jwtService.validateToken(tokenRequest.getToken());
        return ResponseEntity.ok(Collections.singletonMap("isValid", isValid));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestBody @jakarta.validation.Valid TokenRequest tokenRequest
    ) {
        String newToken = jwtService.refreshToken(tokenRequest.getToken());
        return ResponseEntity.ok(Collections.singletonMap("token", newToken));
    }
}
