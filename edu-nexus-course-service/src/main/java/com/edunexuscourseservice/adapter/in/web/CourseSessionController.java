package com.edunexuscourseservice.adapter.in.web;

import com.edunexuscourseservice.adapter.in.web.response.CourseSessionResponse;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;
import com.edunexus.common.exception.NotFoundException;
import com.edunexuscourseservice.port.in.CourseSessionUseCase;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses/{courseId}/sessions")
@RequiredArgsConstructor
public class CourseSessionController {

    private final CourseSessionUseCase courseSessionUseCase;

    // 강의 세션 추가
    @PostMapping
    public ResponseEntity<CourseSessionResponse> addSession(@PathVariable Long courseId,
                                                            @RequestBody CourseSessionCreateRequest request) {
        CourseSession courseSession = courseSessionUseCase.addSessionToCourse(courseId, request.toEntity());
        CourseSessionResponse response = CourseSessionResponse.from(courseSession);

        return ResponseEntity.created(URI.create("/courses/" + courseId + "/sessions/" + courseSession.getId()))
                .body(response);
    }

    // 강의 세션 정보 업데이트
    @PutMapping("/{sessionId}")
    public ResponseEntity<CourseSessionResponse> updateSession(
            @PathVariable Long sessionId,
            @RequestBody CourseSessionUpdateRequest request
    ) {
        CourseSession updatedSession = courseSessionUseCase.updateSession(sessionId, request.toEntity());
        return ResponseEntity.ok(CourseSessionResponse.from(updatedSession));
    }

    // 특정 강의 세션 정보 조회
    @GetMapping("/{sessionId}")
    public ResponseEntity<CourseSessionResponse> getSession(
            @PathVariable Long sessionId
    ) {
        CourseSession courseSession = courseSessionUseCase.getSession(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found with id = " + sessionId));
        return ResponseEntity.ok(CourseSessionResponse.from(courseSession));
    }

    // 특정 강의의 모든 세션 목록 조회
    @GetMapping
    public ResponseEntity<List<CourseSessionResponse>> getAllSessions(@PathVariable Long courseId) {
        List<CourseSession> sessions = courseSessionUseCase.getAllSessionsByCourseId(courseId);
        List<CourseSessionResponse> responses = sessions.stream()
                .map(CourseSessionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Getter
    @Builder
    static class CourseSessionCreateRequest {
        private String title;

        public CourseSession toEntity() {
            CourseSession courseSession = new CourseSession();
            courseSession.setCourseSessionInfo(this.title);
            return courseSession;
        }
    }

    @Getter
    @Builder
    static class CourseSessionUpdateRequest {
        private String title;

        public CourseSession toEntity() {
            CourseSession courseSession = new CourseSession();
            courseSession.setCourseSessionInfo(this.title);
            return courseSession;
        }
    }

}
