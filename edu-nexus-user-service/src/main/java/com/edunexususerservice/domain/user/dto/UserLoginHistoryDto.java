package com.edunexususerservice.domain.user.dto;

import com.edunexususerservice.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserLoginHistoryDto {
    private User user;
    private LocalDateTime loginTime;
    private String ipAddress;
}
