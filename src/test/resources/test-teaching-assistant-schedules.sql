-- ============================================
-- Test Teaching Assistant Schedules Data - Insert sample TA schedules
-- ============================================
-- Purpose: Provides test schedules for teaching assistant work hours
--
-- Common Test Data:
-- - Teaching Assistant IDs: References test-teaching-assistants.sql
-- - Days: Monday-Friday
-- - Times: Use LocalTime format (HH:mm:ss)
-- - Schedules represent when TA will work, not student availability
-- ============================================

-- ==================
-- TA 1 (Data Structures) - Monday/Wednesday
-- ==================
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (1, 1, 'Monday', '14:00:00', '18:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (2, 1, 'Wednesday', '14:00:00', '18:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (3, 1, 'Friday', '10:00:00', '12:00:00');

-- ==================
-- TA 2 (Algorithms) - Tuesday/Thursday
-- ==================
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (4, 2, 'Tuesday', '10:00:00', '14:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (5, 2, 'Thursday', '10:00:00', '14:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (6, 2, 'Friday', '14:00:00', '18:00:00');

-- ==================
-- TA 3 (Database Systems) - Monday/Wednesday
-- ==================
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (7, 3, 'Monday', '08:00:00', '12:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (8, 3, 'Wednesday', '08:00:00', '12:00:00');

-- ==================
-- TA 4 (Operating Systems) - Tuesday/Thursday/Friday
-- ==================
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (9, 4, 'Tuesday', '14:00:00', '18:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (10, 4, 'Thursday', '14:00:00', '18:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (11, 4, 'Friday', '08:00:00', '12:00:00');

-- ==================
-- TA 5 (Previous Semester) - Monday/Wednesday
-- ==================
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (12, 5, 'Monday', '16:00:00', '20:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (13, 5, 'Wednesday', '16:00:00', '20:00:00');

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Schedule IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. All times use 24-hour format (HH:mm:ss) and LocalTime type
-- 3. Schedules represent TA working hours (office hours, lab assistance, etc.)
-- 4. TA 1 has 3 schedules (10 hours/week total)
-- 5. TA 2 has 3 schedules (12 hours/week total)
-- 6. TA 3 has 2 schedules (8 hours/week total)
-- 7. TA 4 has 3 schedules (12 hours/week total)
-- 8. TA 5 is historical (previous semester)
-- 9. Load order: test-teaching-assistants.sql → test-teaching-assistant-schedules.sql
-- ============================================
