package com.edunexusenrollmentservice.domain.entity;

import com.edunexusenrollmentservice.domain.dto.SubscriptionDto;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceOuterClass;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", insertable = false, updatable = false)
    private Payment payment;

    public void setSubscriptionInfo(SubscriptionDto subscriptionDto) {
        this.userId = subscriptionDto.getUserId();
        this.startDate = subscriptionDto.getStartDate();
        this.endDate = subscriptionDto.getEndDate();
        this.paymentId = subscriptionDto.getPaymentId();
    }

    public void renewSubscription(LocalDateTime newStartDate, LocalDateTime newEndDate) {
        this.startDate = newStartDate;
        this.endDate = newEndDate;
    }

    public EnrollmentServiceOuterClass.Subscription toProto() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        String status = (now.isAfter(this.startDate) && now.isBefore(this.endDate)) ? "Active" : "Expired";

        return EnrollmentServiceOuterClass.Subscription.newBuilder()
                .setSubscriptionId(this.subscriptionId)
                .setUserId(this.userId)
                .setPaymentId(this.paymentId)
                .setStartDate(this.startDate.atZone(ZoneId.systemDefault()).toEpochSecond())
                .setEndDate(this.endDate.atZone(ZoneId.systemDefault()).toEpochSecond())
                .setStatus(status)
                .build();
    }
}