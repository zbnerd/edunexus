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
