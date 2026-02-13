package com.edunexususerservice.domain.user.controller;

import com.edunexususerservice.domain.user.controller.request.LoginRequest;
import com.edunexususerservice.domain.user.controller.request.TokenRequest;
import com.edunexususerservice.domain.user.dto.UserDto;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.exception.NotFoundException;
import com.edunexususerservice.domain.user.service.JWTService;
import com.edunexususerservice.domain.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController
 *
 * Tests the authentication controller endpoints including login, token validation,
 * and refresh functionality. Verifies proper HTTP responses and service interactions.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private LoginRequest loginRequest;
    private TokenRequest tokenRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUserInfo(UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashed_password")
                .build());

        // Create login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Create token request
        tokenRequest = new TokenRequest();
        tokenRequest.setToken("valid.jwt.token");
    }

    //region Login Tests
    @Test
    void login_WhenValidCredentials_ShouldReturnTokenWith200Status() {
        // given
        when(userService.requireByEmail("test@example.com"))
                .thenReturn(testUser);
        when(jwtService.login(testUser, "password123"))
                .thenReturn("jwt.token.string");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // when
        ResponseEntity<Map<String, String>> response = authController.login(request, loginRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("jwt.token.string", response.getBody().get("token"));

        verify(userService).requireByEmail("test@example.com");
        verify(jwtService).login(testUser, "password123");
        verify(userService).logUserLogin(testUser.getId(), "192.168.1.1");
    }

    @Test
    void login_WhenUserServiceThrowsException_ShouldPropagateException() {
        // given
        when(userService.requireByEmail("test@example.com"))
                .thenThrow(new NotFoundException("User not found"));

        // when & then
        assertThrows(NotFoundException.class, () -> {
            authController.login(request, loginRequest);
        });

        verify(userService).requireByEmail("test@example.com");
        verify(jwtService, never()).login(any(), any());
    }

    @Test
    void login_WhenJwtServiceThrowsException_ShouldPropagateException() {
        // given
        when(userService.requireByEmail("test@example.com"))
                .thenReturn(testUser);
        when(jwtService.login(testUser, "password123"))
                .thenThrow(new RuntimeException("JWT generation failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            authController.login(request, loginRequest);
        });

        verify(userService).requireByEmail("test@example.com");
        verify(jwtService).login(testUser, "password123");
    }
    //endregion

    //region Validate Token Tests
    @Test
    void validateToken_WhenValidToken_ShouldReturnUserWith200Status() {
        // given
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("test@example.com");
        when(jwtService.parseJwtClaims("valid.jwt.token")).thenReturn(claims);
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // when
        ResponseEntity<User> response = authController.validateToken(tokenRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testUser, response.getBody());

        verify(jwtService).parseJwtClaims("valid.jwt.token");
        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    void validateToken_WhenInvalidToken_ShouldThrowException() {
        // given
        when(jwtService.parseJwtClaims("invalid.token"))
                .thenThrow(new RuntimeException("Invalid token"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            authController.validateToken(tokenRequest);
        });

        verify(jwtService).parseJwtClaims("invalid.token");
        verify(userService, never()).getUserByEmail(any());
    }

    @Test
    void validateToken_WhenUserNotFound_ShouldThrowException() {
        // given
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("nonexistent@example.com");
        when(jwtService.parseJwtClaims("valid.jwt.token")).thenReturn(claims);
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(Exception.class, () -> {
            authController.validateToken(tokenRequest);
        });

        verify(jwtService).parseJwtClaims("valid.jwt.token");
        verify(userService).getUserByEmail("nonexistent@example.com");
    }
    //endregion

    //region Verify Token Tests
    @Test
    void verifyToken_WhenValidToken_ShouldReturnTrueWith200Status() {
        // given
        when(jwtService.validateToken("valid.jwt.token")).thenReturn(true);

        // when
        ResponseEntity<Map<String, Boolean>> response = authController.verifyToken(tokenRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("isValid"));

        verify(jwtService).validateToken("valid.jwt.token");
    }

    @Test
    void verifyToken_WhenInvalidToken_ShouldReturnFalseWith200Status() {
        // given
        when(jwtService.validateToken("invalid.token")).thenReturn(false);
        TokenRequest invalidTokenRequest = new TokenRequest();
        invalidTokenRequest.setToken("invalid.token");

        // when
        ResponseEntity<Map<String, Boolean>> response = authController.verifyToken(invalidTokenRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("isValid"));

        verify(jwtService).validateToken("invalid.token");
    }
    //endregion

    //region Refresh Token Tests
    @Test
    void refreshToken_WhenValidToken_ShouldReturnNewTokenWith200Status() {
        // given
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("test@example.com");
        when(jwtService.parseJwtClaims("valid.jwt.token")).thenReturn(claims);
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.refreshToken("valid.jwt.token")).thenReturn("new.jwt.token");

        // when
        ResponseEntity<Map<String, String>> response = authController.refreshToken(tokenRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("new.jwt.token", response.getBody().get("token"));

        verify(jwtService).parseJwtClaims("valid.jwt.token");
        verify(userService).getUserByEmail("test@example.com");
        verify(jwtService).refreshToken("valid.jwt.token");
    }

    @Test
    void refreshToken_WhenInvalidToken_ShouldThrowException() {
        // given
        when(jwtService.parseJwtClaims("invalid.token"))
                .thenThrow(new RuntimeException("Invalid token"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            authController.refreshToken(tokenRequest);
        });

        verify(jwtService).parseJwtClaims("invalid.token");
        verify(userService, never()).getUserByEmail(any());
    }

    @Test
    void refreshToken_WhenUserNotFound_ShouldThrowException() {
        // given
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("nonexistent@example.com");
        when(jwtService.parseJwtClaims("valid.jwt.token")).thenReturn(claims);
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(Exception.class, () -> {
            authController.refreshToken(tokenRequest);
        });

        verify(jwtService).parseJwtClaims("valid.jwt.token");
        verify(userService).getUserByEmail("nonexistent@example.com");
    }
    //endregion

    //region Edge Cases and Error Scenarios
    @Test
    void login_WhenRequestIsNull_ShouldThrowNullPointerException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            authController.login(null, loginRequest);
        });

        verify(userService, never()).requireByEmail(any());
    }

    @Test
    void login_WhenLoginRequestIsNull_ShouldThrowNullPointerException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            authController.login(request, null);
        });

        verify(userService, never()).requireByEmail(any());
    }

    @Test
    void validateToken_WhenTokenRequestIsNull_ShouldThrowNullPointerException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            authController.validateToken(null);
        });

        verify(jwtService, never()).parseJwtClaims(any());
    }

    @Test
    void verifyToken_WhenTokenRequestIsNull_ShouldThrowNullPointerException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            authController.verifyToken(null);
        });

        verify(jwtService, never()).validateToken(any());
    }

    @Test
    void refreshToken_WhenTokenRequestIsNull_ShouldThrowNullPointerException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            authController.refreshToken(null);
        });

        verify(jwtService, never()).parseJwtClaims(any());
    }

    @Test
    void login_WhenRemoteAddrIsNull_ShouldStillWork() {
        // given
        when(userService.requireByEmail("test@example.com"))
                .thenReturn(testUser);
        when(jwtService.login(testUser, "password123"))
                .thenReturn("jwt.token.string");
        when(request.getRemoteAddr()).thenReturn(null);

        // when
        ResponseEntity<Map<String, String>> response = authController.login(request, loginRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        verify(userService).logUserLogin(testUser.getId(), null);
    }
    //endregion
}