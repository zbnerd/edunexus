-- Sample data for testing
INSERT INTO attendances (user_id, course_id, session_id, check_in_time, check_out_time, status) VALUES
(1, 1, 1, '2026-01-15 09:00:00', '2026-01-15 12:00:00', 'PRESENT'),
(1, 1, 2, '2026-01-16 09:05:00', '2026-01-16 12:00:00', 'LATE'),
(1, 1, 3, '2026-01-17 09:00:00', NULL, 'PRESENT'),
(2, 1, 1, '2026-01-15 09:00:00', '2026-01-15 12:00:00', 'PRESENT'),
(2, 1, 2, NULL, NULL, 'ABSENT');
