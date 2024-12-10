package com.edunexususerservice.domain.user.service;

import com.edunexususerservice.domain.exception.DuplicateUserException;
import com.edunexususerservice.domain.exception.InvalidPasswordException;
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
import static org.mockito.Mockito.verify;
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

}