package com.edunexususerservice.domain.user.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for User entity.
 * Exposes safe subset of user fields without sensitive data like password hash.
 */
@Builder
public record UserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Maps User entity to UserResponse DTO.
     *
     * @param user the user entity
     * @return UserResponse DTO
     */
    public static UserResponse from(com.edunexususerservice.domain.user.entity.User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
