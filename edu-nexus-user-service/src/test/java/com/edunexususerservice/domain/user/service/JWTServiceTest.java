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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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

    //region Enhanced Test Cases

    @Test
    void login_WhenCachedTokenExists_ShouldReturnCachedToken() {
        // given
        User user = new User();
        user.setUserInfo(
                UserDto.builder()
                        .name("test")
                        .email("test@edunexus.com")
                        .password("hashedPassword")
                        .build()
        );

        String cachedToken = "cached.jwt.token";
        when(passwordEncoder.matches("validPassword", user.getPasswordHash())).thenReturn(true);
        when(userLoginTokenRedisRepository.findLoginToken(user.getId())).thenReturn(cachedToken);

        // when
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        String token = jwtService.login(user, "validPassword");

        // then
        assertEquals(cachedToken, token);
        verify(userLoginTokenRedisRepository).findLoginToken(user.getId());
        verify(passwordEncoder).matches("validPassword", user.getPasswordHash());
        verify(userLoginTokenRedisRepository, never()).saveLoginToken(any(), any(), any());
    }

    @Test
    void refreshToken_WhenTokenIsExpired_ShouldGenerateNewToken() {
        // given
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@example.com");
        when(jwtService.parseJwtClaims("expired.token")).thenReturn(claims);

        User user = new User();
        user.setUserInfo(
                UserDto.builder()
                        .name("test")
                        .email("user@example.com")
                        .password("hashedPassword")
                        .build()
        );
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        String newToken = "new.refreshed.token";
        when(jwtService.refreshToken("expired.token")).thenReturn(newToken);

        // when
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        String result = jwtService.refreshToken("expired.token");

        // then
        assertEquals(newToken, result);
        verify(jwtService).parseJwtClaims("expired.token");
        verify(userRepository).findByEmail("user@example.com");
        verify(userLoginTokenRedisRepository).deleteLoginToken(user.getId());
        verify(userLoginTokenRedisRepository).saveLoginToken(user.getId(), newToken, 3600);
    }

    @Test
    void parseJwtClaims_WhenTokenIsMalformed_ShouldThrowException() {
        // given
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // when & then
        assertThrows(Exception.class, () -> {
            ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
            jwtService.parseJwtClaims(malformedToken);
        });
    }

    @Test
    void validateToken_WhenTokenIsExpired_ShouldReturnFalse() {
        // given
        long expiredTime = System.currentTimeMillis() - 3600000; // 1 hour ago
        String expiredToken = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(new Date(expiredTime))
                .setExpiration(new Date(expiredTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // when
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        boolean result = jwtService.validateToken(expiredToken);

        // then
        assertFalse(result);
    }

    @Test
    void validateToken_WhenTokenIsTampered_ShouldReturnFalse() {
        // given
        String validToken = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Tamper the token by changing a character
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // when
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        boolean result = jwtService.validateToken(tamperedToken);

        // then
        assertFalse(result);
    }

    @Test
    void jwtSigningKey_WhenSecretKeyIsNull_ShouldThrowException() {
        // given
        ReflectionTestUtils.setField(jwtService, "secretKey", null);

        // when & then
        assertThrows(Exception.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtService, "jwtSigningKey");
        });
    }

    @Test
    void jwtSigningKey_WhenSecretKeyIsEmpty_ShouldThrowException() {
        // given
        ReflectionTestUtils.setField(jwtService, "secretKey", "");

        // when & then
        assertThrows(Exception.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtService, "jwtSigningKey");
        });
    }

    @Test
    void login_WhenPasswordEncoderThrowsException_ShouldPropagate() {
        // given
        User user = new User();
        user.setUserInfo(
                UserDto.builder()
                        .name("test")
                        .email("test@edunexus.com")
                        .password("hashedPassword")
                        .build()
        );

        when(passwordEncoder.matches("validPassword", user.getPasswordHash()))
                .thenThrow(new RuntimeException("Encoding failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
            jwtService.login(user, "validPassword");
        });

        verify(passwordEncoder).matches("validPassword", user.getPasswordHash());
        verify(userLoginTokenRedisRepository, never()).findLoginToken(any());
    }

    @Test
    void refreshToken_WhenUserNotFound_ShouldThrowException() {
        // given
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("nonexistent@example.com");
        when(jwtService.parseJwtClaims("valid.token")).thenReturn(claims);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(Exception.class, () -> {
            ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
            jwtService.refreshToken("valid.token");
        });

        verify(jwtService).parseJwtClaims("valid.token");
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void validateToken_WhenSecretKeyIsNull_ShouldReturnFalse() {
        // given
        String validToken = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // when
        ReflectionTestUtils.setField(jwtService, "secretKey", null);
        boolean result = jwtService.validateToken(validToken);

        // then
        assertFalse(result);
    }

    @Test
    void parseJwtClaims_WhenTokenIsNull_ShouldThrowException() {
        // when & then
        assertThrows(Exception.class, () -> {
            ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
            jwtService.parseJwtClaims(null);
        });
    }

    @Test
    void validateToken_WhenTokenIsNull_ShouldReturnFalse() {
        // when
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        boolean result = jwtService.validateToken(null);

        // then
        assertFalse(result);
    }

    @Test
    void login_WhenRedisOperationFails_ShouldStillReturnGeneratedToken() {
        // given
        User user = new User();
        user.setUserInfo(
                UserDto.builder()
                        .name("test")
                        .email("test@edunexus.com")
                        .password("hashedPassword")
                        .build()
        );

        when(passwordEncoder.matches("validPassword", user.getPasswordHash())).thenReturn(true);
        when(userLoginTokenRedisRepository.findLoginToken(user.getId())).thenReturn(null);
        doThrow(new RuntimeException("Redis connection failed"))
                .when(userLoginTokenRedisRepository).saveLoginToken(any(), any(), any());

        // when
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        String token = jwtService.login(user, "validPassword");

        // then
        assertNotNull(token);
        verify(passwordEncoder).matches("validPassword", user.getPasswordHash());
        verify(userLoginTokenRedisRepository).findLoginToken(user.getId());
        verify(userLoginTokenRedisRepository).saveLoginToken(any(), any(), any());
    }
    //endregion
}