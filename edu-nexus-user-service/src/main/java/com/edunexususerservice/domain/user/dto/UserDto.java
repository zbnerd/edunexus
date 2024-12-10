package com.edunexususerservice.domain.user.dto;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class UserDto {
    private String name;
    private String email;
    private String password;
}
