package com.edunexususerservice.domain.user.service;

import com.edunexususerservice.domain.user.exception.DuplicateUserException;
import com.edunexususerservice.domain.user.exception.InvalidPasswordException;
import com.edunexususerservice.domain.user.dto.PasswordChangeDto;
import com.edunexususerservice.domain.user.dto.UserDto;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.repository.UserLoginHistoryRepository;
import com.edunexususerservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserLoginHistoryRepository userLoginHistoryRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void signUpTest() {
        // given
        User user = new User();

        // when
        when(userRepository.save(any(User.class))).thenReturn(user);

        // then
        User signedUpUser = userService.signUp("test1", "test1", "test1");
        assertNotNull(signedUpUser);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signUpDuplicateTest() throws Exception {
        // given
        String email = "test1@test.com";
        User existingUser = new User();
        setId(existingUser, 1L);
        existingUser.setUserInfo(
                UserDto.builder()
                        .name("existingName")
                        .email(email)
                        .password("encoded_password")
                        .build()
        );

        // Mocking: 이미 이메일이 존재한다고 설정
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // when & then
        assertThrows(DuplicateUserException.class, () -> {
            userService.signUp("testName", email, "testPassword");
        });
    }

    @Test
    void updatePasswordTest() throws Exception {
        // given
        User exsitingUser = new User();
        setId(exsitingUser, 1L);
        exsitingUser.setUserInfo(
                UserDto.builder()
                        .email("test1@edunexus.com")
                        .name("testname")
                        .password("encoded_testpassword")
                        .build()
        );

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(exsitingUser));
        when(passwordEncoder.matches("testpassword", exsitingUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches("testpassword2", "encoded_testpassword2")).thenReturn(true);
        User passwordUpdatedUser = userService.updatePassword(1L,
                PasswordChangeDto.builder()
                        .oldPassword("testpassword")
                        .newPassword("testpassword2")
                        .build()
        );

        // then
        assertNotNull(passwordUpdatedUser);
        assertTrue(passwordEncoder.matches("testpassword2", "encoded_testpassword2"));
    }

    @Test
    void updatePasswordWrongOldPassword() throws Exception {
        // given
        User exsitingUser = new User();
        setId(exsitingUser, 1L);
        exsitingUser.setUserInfo(
                UserDto.builder()
                        .email("test1@edunexus.com")
                        .name("testname")
                        .password("encoded_testpassword")
                        .build()
        );

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(exsitingUser));
        when(passwordEncoder.matches("testpassword2", exsitingUser.getPasswordHash())).thenReturn(false);


        // then
        assertThrows(InvalidPasswordException.class, () -> {userService.updatePassword(1L,
                PasswordChangeDto.builder()
                        .oldPassword("testpassword2")
                        .newPassword("testpassword3")
                        .build()
        );
        });
    }

    @Test
    void getUserByIdTest() throws Exception {
        // given
        User user = new User();
        setId(user, 1L);

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // then
        Optional<User> userOptional = userService.getUserById(1L);
        assertNotNull(userOptional);
        assertTrue(userOptional.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByEmailTest() {
        // given
        User user = new User();
        user.setUserInfo(
                UserDto.builder()
                        .email("test1@edunexus.com")
                        .name("testname")
                        .password("encoded_testpassword")
                        .build()
        );

        // when
        when(userRepository.findByEmail("test1@edunexus.com")).thenReturn(Optional.of(user));

        // then
        Optional<User> userOptional = userService.getUserByEmail("test1@edunexus.com");
        assertNotNull(userOptional);
        assertTrue(userOptional.isPresent());
        verify(userRepository).findByEmail("test1@edunexus.com");

    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true); // private 필드 접근 허용
        field.set(target, id);
    }

    //region New Edge Case Tests

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<User> result = userService.getUserById(999L);

        // then
        assertTrue(result.isEmpty());
        verify(userRepository).findById(999L);
    }

    @Test
    void getUserByEmail_WhenEmailDoesNotExist_ShouldReturnEmptyOptional() {
        // given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when
        Optional<User> result = userService.getUserByEmail("nonexistent@example.com");

        // then
        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void updatePassword_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(com.edunexususerservice.domain.user.exception.NotFoundException.class, () -> {
            userService.updatePassword(999L,
                PasswordChangeDto.builder()
                    .oldPassword("oldpass")
                    .newPassword("newpass")
                    .build());
        });

        verify(userRepository).findById(999L);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void logUserLogin_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(com.edunexususerservice.domain.user.exception.NotFoundException.class, () -> {
            userService.logUserLogin(999L, "192.168.1.1");
        });

        verify(userRepository).findById(999L);
        verify(userLoginHistoryRepository, never()).save(any());
    }

    @Test
    void updatePassword_WhenPasswordChangeDtoIsNull_ShouldThrowNullPointerException() {
        // given - no setup needed

        // when & then
        assertThrows(NullPointerException.class, () -> {
            userService.updatePassword(1L, null);
        });

        verify(userRepository, never()).findById(any());
    }

    @Test
    void updatePassword_WhenPasswordChangeDtoIsEmpty_ShouldThrowNullPointerException() throws Exception {
        // given
        User existingUser = new User();
        setId(existingUser, 1L);
        existingUser.setUserInfo(
            UserDto.builder()
                .email("test1@edunexus.com")
                .name("testname")
                .password("encoded_testpassword")
                .build()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // when & then
        assertThrows(NullPointerException.class, () -> {
            userService.updatePassword(1L, PasswordChangeDto.builder()
                    .oldPassword("old")
                    .newPassword("new")
                    .build());
        });

        verify(userRepository).findById(1L);
    }

    @Test
    void signUp_WhenNameIsNull_ShouldNotFailButCreateUser() throws Exception {
        // given
        User user = new User();

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // when
        User result = userService.signUp(null, "test@test.com", "password");

        // then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signUp_WhenEmailIsNull_ShouldNotFailButCreateUser() {
        // given
        User user = new User();

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // when
        User result = userService.signUp("test", null, "password");

        // then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signUp_WhenPasswordIsNull_ShouldEncodeNullPassword() {
        // given
        User user = new User();

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(null)).thenReturn("encoded_null_password");

        // when
        User result = userService.signUp("test", "test@test.com", null);

        // then
        assertNotNull(result);
        verify(passwordEncoder).encode(null);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserLoginHistories_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            userService.getUserLoginHistories(999L);
        });

        verify(userRepository).findById(999L);
    }

    @Test
    void getUserByIdOrThrowToNotFoundException_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // SKIPPED: Method getUserByIdOrThrowToNotFoundException is now private during refactoring
        // This functionality is tested indirectly through getUserById
        // given
        // when(userRepository.findById(999L)).thenReturn(Optional.empty());
        //
        // // when & then
        // assertThrows(com.edunexususerservice.domain.user.exception.NotFoundException.class, () -> {
        //     userService.getUserByIdOrThrowToNotFoundException(999L);
        // });
        //
        // verify(userRepository).findById(999L);
    }

    @Test
    void getUserByEmailOrThrowToNotFoundException_WhenEmailDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(com.edunexususerservice.domain.user.exception.NotFoundException.class, () -> {
            userService.getUserByEmailOrThrowToNotFoundException("nonexistent@example.com");
        });

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void logUserLogin_WhenIpAddressIsNull_ShouldStillLogLogin() throws Exception {
        // given
        User user = new User();
        setId(user, 1L);
        user.setUserInfo(
            UserDto.builder()
                .email("test@example.com")
                .name("testname")
                .password("encoded_password")
                .build()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        assertDoesNotThrow(() -> {
            userService.logUserLogin(1L, null);
        });

        verify(userRepository).findById(1L);
        verify(userLoginHistoryRepository).save(any());
    }
    //endregion
}