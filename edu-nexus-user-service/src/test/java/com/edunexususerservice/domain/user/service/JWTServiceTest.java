package com.edunexususerservice.domain.user.service;

import com.edunexususerservice.domain.user.exception.InvalidPasswordException;
import com.edunexususerservice.domain.user.dto.UserDto;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.repository.UserLoginTokenRedisRepository;
import com.edunexususerservice.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserLoginTokenRedisRepository userLoginTokenRedisRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JWTService jwtService;
    private final String secretKey = "gP1hx!82&fD4z@V9X%YqL#m6kP*o$w3B5E7Jr^N+T2a8ZyC-WxQ#vK@LdFt&R!rt";

    @BeforeEach
    void setUp() {
        jwtService = new JWTService(passwordEncoder, userLoginTokenRedisRepository, userRepository);
    }

    @Test
    void loginTest() {
        // given
        User user = new User();
        user.setUserInfo(
                UserDto.builder()
                        .name("test")
                        .email("test@edunexus.com")
                        .password("hashedPassword")
                        .build()
        );

        // when
        when(passwordEncoder.matches("validPassword", user.getPasswordHash())).thenReturn(true);
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);

        // then
        String token = jwtService.login(user, "validPassword");
        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(user.getEmail(), claims.getSubject());
    }

    @Test
    void loginTest_InvalidCredentials() {
        // given
        User user = new User();
        user.setUserInfo(
                UserDto.builder()
                        .name("test")
                        .email("test@edunexus.com")
                        .password("hashedPassword")
                        .build()
        );

        // when
        when(passwordEncoder.matches("invalidPassword", user.getPasswordHash())).thenReturn(false);

        // then
        assertThrows(InvalidPasswordException.class, () -> jwtService.login(user, "invalidPassword"));
    }
    
    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // given
        String subject = "user@example.com";
        long currentTimeMillis = System.currentTimeMillis();
        
        // when
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + 3600000))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
        
        // then
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        String token = "invalid.token.here";
        assertFalse(jwtService.validateToken(token));
    }

}