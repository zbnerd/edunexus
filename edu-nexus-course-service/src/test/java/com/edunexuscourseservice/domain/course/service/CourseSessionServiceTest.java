package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseSessionRepository;
import com.edunexuscourseservice.application.service.CourseSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseSessionServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSessionRepository courseSessionRepository;

    @InjectMocks
    private CourseSessionService courseSessionService;

    @Test
    void testAddSessionToCourse() throws Exception {
        // given
        Course course = new Course();
        setId(course, 1L);

        CourseSession courseSession = new CourseSession();
        setId(courseSession, 1L);
        courseSession.setCourse(course);

        // when
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseSessionRepository.save(any(CourseSession.class))).thenReturn(courseSession);

        // then
        CourseSession result = courseSessionService.addSessionToCourse(course.getId(), courseSession);
        assertNotNull(result);
        verify(courseSessionRepository).save(courseSession);
        assertEquals(1L, result.getId());

    }

    @Test
    void testUpdateSession() throws Exception {
        CourseSession courseSession = new CourseSession();
        courseSession.setCourseSessionInfo("session1");
        setId(courseSession, 1L);

        CourseSession updateCourseSession = new CourseSession();
        updateCourseSession.setCourseSessionInfo("session2");

        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(courseSession));

        CourseSession result = courseSessionService.updateSession(1L, updateCourseSession);

        assertNotNull(result);
        assertEquals("session2", result.getTitle());
    }

    @Test
    void testGetRatingById() throws Exception{
        CourseSession courseSession = new CourseSession();

        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(courseSession));

        Optional<CourseSession> result = courseSessionService.getSession(1L);
        assertTrue(result.isPresent());
        assertEquals(courseSession, result.get());
        verify(courseSessionRepository).findById(1L);
    }


    @Test
    void testGetAllRatingsByCourseId() throws Exception {
        List<CourseSession> sessions = Arrays.asList(new CourseSession(), new CourseSession());

        when(courseSessionRepository.findByCourseId(1L)).thenReturn(sessions);

        List<CourseSession> result = courseSessionService.getAllSessionsByCourseId(1L);
        assertNotNull(result);
        assertEquals(sessions.size(), result.size());
        verify(courseSessionRepository).findByCourseId(1L);

    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true); // private 필드 접근 허용
        field.set(target, id);
    }
}