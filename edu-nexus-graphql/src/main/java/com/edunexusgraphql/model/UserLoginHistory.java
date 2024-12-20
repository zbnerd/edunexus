package com.edunexusgraphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginHistory {
    private Long id;
    private String name;
    private String loginTime;
    private String ipAddress;
}
