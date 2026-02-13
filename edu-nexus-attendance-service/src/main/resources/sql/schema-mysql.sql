CREATE TABLE attendances
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'Attendance record unique identifier',
    user_id     BIGINT      NOT NULL COMMENT 'User ID of the student',
    course_id   BIGINT      NOT NULL COMMENT 'Course ID for the attendance',
    session_id  BIGINT      NOT NULL COMMENT 'Session ID for the attendance',
    check_in_time  DATETIME NULL COMMENT 'Time when student checked in',
    check_out_time DATETIME NULL COMMENT 'Time when student checked out',
    status      VARCHAR(20) NOT NULL DEFAULT 'PRESENT' COMMENT 'Attendance status: PRESENT, LATE, ABSENT',
    created_at  DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    updated_at  DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update time',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_course_id (course_id),
    INDEX idx_session_id (session_id),
    INDEX idx_user_course (user_id, course_id),
    INDEX idx_user_session (user_id, session_id)
) COMMENT 'Student attendance tracking table';

CREATE TABLE attendance_sessions
(
    id                          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'Attendance session unique identifier',
    course_id                   BIGINT      NOT NULL COMMENT 'Course ID for the session',
    session_id                  BIGINT      NOT NULL COMMENT 'Session ID from course service',
    scheduled_start             DATETIME    NOT NULL COMMENT 'Scheduled start time for the session',
    scheduled_end               DATETIME    NOT NULL COMMENT 'Scheduled end time for the session',
    attendance_window_minutes   INT         NOT NULL DEFAULT 15 COMMENT 'Minutes after scheduled start when check-in is allowed',
    auto_mark_absent            BOOLEAN     NOT NULL DEFAULT TRUE COMMENT 'Automatically mark absences after session ends',
    created_at                  DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    updated_at                  DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_course_id (course_id),
    INDEX idx_scheduled_start (scheduled_start),
    INDEX idx_scheduled_end (scheduled_end)
) COMMENT 'Attendance session configuration with time windows';

CREATE TABLE attendance_summaries
(
    id                  BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'Summary record unique identifier',
    user_id             BIGINT      NOT NULL COMMENT 'User ID of the student',
    course_id           BIGINT      NOT NULL COMMENT 'Course ID for the summary',
    total_sessions      INT         NOT NULL DEFAULT 0 COMMENT 'Total number of sessions in the course',
    attended_sessions   INT         NOT NULL DEFAULT 0 COMMENT 'Number of sessions attended (PRESENT)',
    late_sessions       INT         NOT NULL DEFAULT 0 COMMENT 'Number of sessions attended late',
    absent_sessions     INT         NOT NULL DEFAULT 0 COMMENT 'Number of sessions missed',
    attendance_rate     DOUBLE      NOT NULL DEFAULT 0.0 COMMENT 'Attendance rate as percentage (0-100)',
    last_updated_at     DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT 'Last time summary was recalculated',
    created_at          DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_course (user_id, course_id),
    INDEX idx_course_id (course_id),
    INDEX idx_attendance_rate (attendance_rate)
) COMMENT 'Pre-calculated attendance summary for quick queries';
