package com.edunexususerservice.domain.user.service;

import com.edunexususerservice.domain.user.exception.InvalidPasswordException;
import com.edunexususerservice.domain.user.exception.NotFoundException;
import com.edunexususerservice.domain.user.entity.User;
import com.edunexususerservice.domain.user.repository.UserLoginTokenRedisRepository;
import com.edunexususerservice.domain.user.repository.UserRepository;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JWTService {

    @Value("${jwt.secret}")
    private String secretKey;

    private final PasswordEncoder passwordEncoder;
    private final UserLoginTokenRedisRepository redisRepository;
    private final UserRepository userRepository;

    public String login(User existingUser, String requestPassword) {
        if (!passwordEncoder.matches(requestPassword, existingUser.getPasswordHash())) {
            throw new InvalidPasswordException("Invalid credentials");
        }

        String cachedToken = redisRepository.findLoginToken(existingUser.getId());
        if (StringUtils.hasText(cachedToken)) {
            log.info("cachedToken: {}", cachedToken);
            return cachedToken;
        }

        long currentTimeMillis = System.currentTimeMillis();

        String jwtToken = Jwts.builder()
                .setSubject(existingUser.getEmail())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + JwtConstants.TOKEN_EXPIRATION_MS))
                .signWith(jwtSigningKey())
                .compact();

        redisRepository.saveLoginToken(existingUser.getId(), jwtToken, JwtConstants.TOKEN_EXPIRATION_SECONDS);
        return jwtToken;
    }

    public boolean validateToken(String token) {
        try {
            parseJwtClaims(token);
            return true;
        } catch (Exception e) {
            log.error("validateToken error : ", e);
            return false;
        }
    }

    public String refreshToken(String token) {
        Claims claims = parseJwtClaims(token);
        String userEmail = claims.getSubject();

        User foundUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new NotFoundException("User Not Found"));
        Long userId = foundUser.getId();

        redisRepository.deleteLoginToken(userId);

        long currentTimeMillis = System.currentTimeMillis();
        String jwtToken = Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + JwtConstants.TOKEN_EXPIRATION_MS))
                .signWith(jwtSigningKey())
                .compact();

        redisRepository.saveLoginToken(userId, jwtToken, JwtConstants.TOKEN_EXPIRATION_SECONDS);

        return jwtToken;
    }

    public Claims parseJwtClaims(String token) {

        String claims = Jwts.parserBuilder()
                .setSigningKey(jwtSigningKey()) // 서명 키 설정
                .build()
                .parseClaimsJws(token) // JWT 파싱
                .getBody().toString();
        log.info("token: {}, claims = {}", token, claims);

        return Jwts.parserBuilder()
                .setSigningKey(jwtSigningKey()) // 서명 키 설정
                .build()
                .parseClaimsJws(token) // JWT 파싱
                .getBody(); // Claims 객체 반환
    }

    private Key jwtSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
