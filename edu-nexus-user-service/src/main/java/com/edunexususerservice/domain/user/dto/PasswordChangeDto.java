package com.edunexususerservice.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordChangeDto {
    private String oldPassword;
    private String newPassword;
}
