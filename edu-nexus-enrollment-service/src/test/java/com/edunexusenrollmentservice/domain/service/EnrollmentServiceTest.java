package com.edunexusenrollmentservice.domain.service;

import com.edunexusenrollmentservice.domain.dto.EnrollmentDto;
import com.edunexusenrollmentservice.domain.dto.SubscriptionDto;
import com.edunexusenrollmentservice.domain.entity.Enrollment;
import com.edunexusenrollmentservice.domain.entity.Subscription;
import com.edunexusenrollmentservice.domain.repository.EnrollmentRepository;
import com.edunexusenrollmentservice.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void testRegisterCourse() {
        // given
        Enrollment enrollment = new Enrollment();
        EnrollmentDto enrollmentDto = EnrollmentDto.builder()
                .userId(1L)
                .courseId(100L)
                .paymentId(200L)
                .build();

        enrollment.setEnrollmentInfo(enrollmentDto);

        // when
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        // then
        Enrollment result = enrollmentService.registerCourse(enrollmentDto);
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(enrollment.getUserId());
        assertThat(result.getCourseId()).isEqualTo(enrollment.getCourseId());
        assertThat(result.getPaymentId()).isEqualTo(enrollment.getPaymentId());

        verify(enrollmentRepository).save(any(Enrollment.class));
    }


    @Test
    void testManageSubscription() {
        //given
        Subscription subscription = new Subscription();
        SubscriptionDto subscriptionDto = SubscriptionDto.builder()
                .userId(1L)
                .startDate(LocalDateTime.of(2024, 5, 19, 0, 0, 0))
                .endDate(LocalDateTime.of(2024, 5, 20, 0, 0, 0))
                .build();
        subscription.setSubscriptionInfo(subscriptionDto);

        //when
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        //then
        Subscription managedSubscription = enrollmentService.manageSubscription(subscriptionDto);
        assertThat(managedSubscription).isNotNull();
        assertThat(managedSubscription.getUserId()).isEqualTo(subscription.getUserId());
        assertThat(managedSubscription.getStartDate()).isEqualTo(subscription.getStartDate());
        assertThat(managedSubscription.getEndDate()).isEqualTo(subscription.getEndDate());

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testRenewSubscription() {
        // given
        Subscription exsitingSubscription = new Subscription();
        SubscriptionDto subscriptionDto = SubscriptionDto.builder()
                .userId(1L)
                .startDate(LocalDateTime.of(2024, 5, 19, 0, 0, 0))
                .endDate(LocalDateTime.of(2024, 5, 20, 0, 0, 0))
                .build();
        exsitingSubscription.setSubscriptionInfo(subscriptionDto);

        // when
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(exsitingSubscription));

        // then
        Subscription renewedSubscription = enrollmentService.renewSubscription(10L,
                LocalDateTime.of(2024, 5, 20, 0, 0, 0),
                LocalDateTime.of(2024, 5, 21, 0, 0, 0)
        );
        assertThat(renewedSubscription).isNotNull();
        assertThat(renewedSubscription.getStartDate()).isEqualTo(LocalDateTime.of(2024, 5, 20, 0, 0, 0));
        assertThat(renewedSubscription.getEndDate()).isEqualTo(LocalDateTime.of(2024, 5, 21, 0, 0, 0));

    }

    @Test
    void testRenewSubscriptionNotFound() {
        long subscriptionId = 300L;
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.renewSubscription(subscriptionId, LocalDateTime.now(), LocalDateTime.now().plusDays(30)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checkCourseAccess() {
        // given
        Enrollment enrollment = new Enrollment();
        EnrollmentDto enrollmentDto = EnrollmentDto.builder()
                .userId(1L)
                .courseId(100L)
                .paymentId(200L)
                .build();

        enrollment.setEnrollmentInfo(enrollmentDto);

        // when
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 100L)).thenReturn(Optional.of(enrollment));

        // then
        boolean result = enrollmentService.checkCourseAccess(1L, 100L);
        assertThat(result).isTrue();
    }

    @Test
    void testCheckSubscriptionAccess() {
        long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = new Subscription();
        SubscriptionDto subscriptionDto = SubscriptionDto.builder()
                .userId(userId)
                .startDate(now)
                .endDate(now.plusDays(5))
                .build();

        subscription.setSubscriptionInfo(subscriptionDto);

        when(subscriptionRepository.findTopByUserIdAndEndDateAfterOrderByEndDateDesc(eq(userId), any())).thenReturn(Optional.of(subscription));

        assertThat(enrollmentService.checkSubscriptionAccess(userId, now)).isTrue();
        verify(subscriptionRepository).findTopByUserIdAndEndDateAfterOrderByEndDateDesc(userId, now);
    }
}