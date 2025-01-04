package com.edunexuscourseservice.application.service.kafka;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRepository;
import com.edunexuscourseservice.application.saga.event.*;
import com.edunexuscourseservice.application.service.CourseRatingService;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.domain.course.util.KafkaCourseRatingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRatingRedisResponseConsumerService {

    private final ObjectMapper objectMapper;
    private final CourseRatingService courseRatingService;
    private final CourseRatingRepository courseRatingRepository;

    @KafkaListener(topics = "course-rating-redis-add-response", groupId = "course-rating-group")
    public void courseRatingResponseAdd(String message) throws JsonProcessingException {

        CourseRatingAddResponseEvent event = objectMapper.readValue(message, CourseRatingAddResponseEvent.class);

        if (event.getResponse().equals(KafkaCourseRatingResponse.FAIL.getResponse())) {
            courseRatingRepository.deleteById(event.getCourseRatingId());
            log.info("Course rating redis adding failed. Compensating Transaction was activated. deleting courseRatingId: {}", event.getCourseRatingId());
        } else {
            log.info("Course rating adding succeed. added courseRatingId: {}", event.getCourseRatingId());
        }

    }

    @KafkaListener(topics = "course-rating-redis-update-response", groupId = "course-rating-group")
    public void courseRatingResponseUpdate(String message) throws JsonProcessingException {

        CourseRatingUpdateResponseEvent event = objectMapper.readValue(message, CourseRatingUpdateResponseEvent.class);

        if (event.getResponse().equals(KafkaCourseRatingResponse.FAIL.getResponse())) {
            CourseRating courseRating = courseRatingService.getRating(event.getCourseId()).orElseThrow(NotFoundException::new);
            courseRating.setRating(event.getOldRating());

            log.info("Course rating redis updating failed. Compensating Transaction was activated.");
        } else {
            log.info("Course rating updating succeed.");
        }

    }

}
