package com.edunexusgraphql.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;
}
