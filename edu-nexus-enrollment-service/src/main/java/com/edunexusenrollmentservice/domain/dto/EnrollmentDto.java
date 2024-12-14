package com.edunexusenrollmentservice.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnrollmentDto {
    private Long userId;
    private Long courseId;
    private Long paymentId;
}
