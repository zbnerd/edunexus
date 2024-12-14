package com.edunexusenrollmentservice.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionDto {
    private Long userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long paymentId;
}
