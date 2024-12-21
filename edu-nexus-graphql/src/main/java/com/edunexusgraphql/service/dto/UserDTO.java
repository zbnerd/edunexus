package com.edunexusgraphql.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDTO {
    private String name;
    private String email;
    private String password;
}
