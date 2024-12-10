package com.edunexususerservice.domain.user.service;

import com.edunexususerservice.domain.exception.InvalidPasswordException;
import com.edunexususerservice.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.sql.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JWTService {

    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secretKey;

    public String login(User existingUser, String requestPassword) {
        if (!passwordEncoder.matches(requestPassword, existingUser.getPasswordHash())) {
            throw new InvalidPasswordException("Invalid credentials");
        }



        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(existingUser.getEmail())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + 3600000)) // Token expires in 1 hour
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
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
        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(claims.getSubject())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + 3600000)) // Token expires in 1 hour
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public Claims parseJwtClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))) // 서명 키 설정
                .build()
                .parseClaimsJws(token) // JWT 파싱
                .getBody(); // Claims 객체 반환
    }

}
