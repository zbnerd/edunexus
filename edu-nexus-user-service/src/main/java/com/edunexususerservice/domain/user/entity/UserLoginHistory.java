package com.edunexususerservice.domain.user.entity;

import com.edunexususerservice.domain.user.dto.UserLoginHistoryDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_login_histories")
public class UserLoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(name = "login_time", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime loginTime;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public void setUserLoginHistory(UserLoginHistoryDto userLoginHistoryDto) {
        this.user = userLoginHistoryDto.getUser();
        this.loginTime = userLoginHistoryDto.getLoginTime();
        this.ipAddress = userLoginHistoryDto.getIpAddress();
    }
}