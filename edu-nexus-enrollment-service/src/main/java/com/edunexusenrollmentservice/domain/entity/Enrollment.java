package com.edunexusenrollmentservice.domain.entity;

import com.edunexusenrollmentservice.domain.dto.EnrollmentDto;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceOuterClass;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Entity
@Table(name = "enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long courseId;

    @Column(name = "payment_id",nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", insertable = false, updatable = false)
    private Payment payment;

    public void setEnrollmentInfo(EnrollmentDto enrollmentDto) {
        this.userId = enrollmentDto.getUserId();
        this.courseId = enrollmentDto.getCourseId();
        this.paymentId = enrollmentDto.getPaymentId();
        this.registrationDate = LocalDateTime.now();
    }

    public EnrollmentServiceOuterClass.Enrollment toProto() {
        return EnrollmentServiceOuterClass.Enrollment.newBuilder()
                .setEnrollmentId(this.enrollmentId)
                .setUserId(this.userId)
                .setCourseId(this.courseId)
                .setPaymentId(this.paymentId)
                .setRegistrationDate(this.registrationDate.atZone(ZoneId.systemDefault()).toEpochSecond())
                .build();
    }
}
