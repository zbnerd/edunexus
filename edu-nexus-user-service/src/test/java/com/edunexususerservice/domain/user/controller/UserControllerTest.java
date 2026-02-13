package com.edunexususerservice.domain.user.controller;

import com.edunexususerservice.domain.user.dto.PasswordChangeDto;
import com.edunexususerservice.domain.user.dto.UserDto;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.entity.UserLoginHistory;
import com.edunexususerservice.domain.user.exception.NotFoundException;
import com.edunexususerservice.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController
 *
 * Tests the user management controller endpoints including user creation, retrieval,
 * password changes, and login history. Verifies proper HTTP responses and service interactions.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private PasswordChangeDto passwordChangeDto;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUserInfo(UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashed_password")
                .build());

        // Create password change DTO
        passwordChangeDto = PasswordChangeDto.builder()
                .oldPassword("oldpass")
                .newPassword("newpass")
                .build();
    }

    //region Sign Up Tests
    @Test
    void signUp_WhenValidData_ShouldReturnCreatedStatusWithUser() {
        // given
        UserDto userDto = UserDto.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .build();

        when(userService.signUp("New User", "newuser@example.com", "password123"))
                .thenReturn(testUser);

        // when
        ResponseEntity<User> response = userController.signUp(userDto);

        // then
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testUser, response.getBody());

        // Check Location header
        assertNotNull(response.getHeaders().getLocation());
        assertTrue(response.getHeaders().getLocation().toString().contains("/users/"));

        verify(userService).signUp("New User", "newuser@example.com", "password123");
    }

    @Test
    void signUp_WhenServiceThrowsException_ShouldPropagateException() {
        // given
        UserDto userDto = UserDto.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .build();

        when(userService.signUp("New User", "newuser@example.com", "password123"))
                .thenThrow(new RuntimeException("User creation failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            userController.signUp(userDto);
        });

        verify(userService).signUp("New User", "newuser@example.com", "password123");
    }
    //endregion

    //region Get User Tests
    @Test
    void getUser_WhenUserExists_ShouldReturnUserWith200Status() {
        // given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // when
        ResponseEntity<User> response = userController.getUser(1L);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testUser, response.getBody());

        verify(userService).getUserById(1L);
    }

    @Test
    void getUser_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            userController.getUser(999L);
        });

        verify(userService).getUserById(999L);
    }

    @Test
    void getUser_WhenIdIsNull_ShouldThrowException() {
        // when & then
        assertThrows(Exception.class, () -> {
            userController.getUser(null);
        });

        verify(userService, never()).getUserById(any());
    }
    //endregion

    //region Change Password Tests
    @Test
    void changePassword_WhenValidData_ShouldReturnUpdatedUserWith200Status() {
        // given
        when(userService.updatePassword(1L, passwordChangeDto))
                .thenReturn(testUser);

        // when
        ResponseEntity<User> response = userController.changePassword(1L, passwordChangeDto);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testUser, response.getBody());

        verify(userService).updatePassword(1L, passwordChangeDto);
    }

    @Test
    void changePassword_WhenUserIdDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(userService.updatePassword(999L, passwordChangeDto))
                .thenThrow(new NotFoundException("User not found"));

        // when & then
        assertThrows(NotFoundException.class, () -> {
            userController.changePassword(999L, passwordChangeDto);
        });

        verify(userService).updatePassword(999L, passwordChangeDto);
    }

    @Test
    void changePassword_WhenPasswordChangeDtoIsNull_ShouldThrowException() {
        // when & then
        assertThrows(Exception.class, () -> {
            userController.changePassword(1L, null);
        });

        verify(userService, never()).updatePassword(any(), any());
    }

    @Test
    void changePassword_WhenPasswordChangeDtoIsEmpty_ShouldThrowException() {
        // when & then
        assertThrows(Exception.class, () -> {
            userController.changePassword(1L, PasswordChangeDto.builder()
                    .oldPassword("")
                    .newPassword("")
                    .build());
        });

        verify(userService, never()).updatePassword(any(), any());
    }
    //endregion

    //region Get Login Histories Tests
    @Test
    void getLoginHistories_WhenUserExists_ShouldReturnLoginHistoriesWith200Status() {
        // given
        List<UserLoginHistory> histories = List.of(new UserLoginHistory(), new UserLoginHistory());
        when(userService.getUserLoginHistories(1L)).thenReturn(histories);

        // when
        ResponseEntity<List<UserLoginHistory>> response = userController.getLoginHistories(1L);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(histories, response.getBody());

        verify(userService).getUserLoginHistories(1L);
    }

    @Test
    void getLoginHistories_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(userService.getUserLoginHistories(999L))
                .thenThrow(new RuntimeException("User not found"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            userController.getLoginHistories(999L);
        });

        verify(userService).getUserLoginHistories(999L);
    }

    @Test
    void getLoginHistories_WhenUserIdIsNull_ShouldThrowException() {
        // when & then
        assertThrows(Exception.class, () -> {
            userController.getLoginHistories(null);
        });

        verify(userService, never()).getUserLoginHistories(any());
    }

    @Test
    void getLoginHistories_WhenHistoriesAreEmpty_ShouldReturnEmptyList() {
        // given
        when(userService.getUserLoginHistories(1L)).thenReturn(List.of());

        // when
        ResponseEntity<List<UserLoginHistory>> response = userController.getLoginHistories(1L);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(userService).getUserLoginHistories(1L);
    }
    //endregion

    //region Edge Cases
    @Test
    void signUp_WhenUserDtoFieldsAreNull_ShouldStillCreateUser() {
        // given
        UserDto userDto = UserDto.builder()
                .name(null)
                .email(null)
                .password(null)
                .build();

        when(userService.signUp(null, null, null)).thenReturn(testUser);

        // when
        ResponseEntity<User> response = userController.signUp(userDto);

        // then
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        verify(userService).signUp(null, null, null);
    }

    @Test
    void getUser_WhenServiceThrowsUnexpectedException_ShouldPropagate() {
        // given
        when(userService.getUserById(1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            userController.getUser(1L);
        });

        verify(userService).getUserById(1L);
    }

    @Test
    void changePassword_WhenServiceThrowsInvalidPasswordException_ShouldPropagate() {
        // given
        when(userService.updatePassword(1L, passwordChangeDto))
                .thenThrow(new RuntimeException("Invalid password"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            userController.changePassword(1L, passwordChangeDto);
        });

        verify(userService).updatePassword(1L, passwordChangeDto);
    }

    @Test
    void getLoginHistories_WhenServiceThrowsServiceException_ShouldPropagate() {
        // given
        when(userService.getUserLoginHistories(1L))
                .thenThrow(new RuntimeException("Service error"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            userController.getLoginHistories(1L);
        });

        verify(userService).getUserLoginHistories(1L);
    }
    //endregion
}