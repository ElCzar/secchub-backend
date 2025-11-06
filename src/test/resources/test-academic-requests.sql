-- ============================================
-- Test Academic Requests Data - Insert sample academic requests
-- ============================================
-- Purpose: Provides test academic requests for integration testing
--
-- Common Test Data:
-- - User ID: References test users (3 = testProgram@example.com)
-- - Course IDs: References test courses from test-courses.sql
-- - Semester ID: 2 (current semester from test-semesters.sql)
-- - Dates: Within semester boundaries
-- - Capacity: Typical class sizes (20-40 students)
-- - Status flags: accepted, combined
-- ============================================

-- ==================
-- 1. Pending Request - Data Structures
-- ==================
-- Purpose: New request that hasn't been processed yet
INSERT INTO academic_request (id, user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation, accepted, combined)
VALUES (1, 3, 1, 2, '2025-08-01', '2025-12-15', 35, '2025-07-15', 'Standard undergraduate section', FALSE, FALSE);

-- ==================
-- 2. Accepted Request - Algorithms
-- ==================
-- Purpose: Request that has been accepted and moved to planning
INSERT INTO academic_request (id, user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation, accepted, combined)
VALUES (2, 3, 2, 2, '2025-08-01', '2025-12-15', 30, '2025-07-10', 'Core required course for CS majors', TRUE, FALSE);

-- ==================
-- 3. Combined Request - Database Systems
-- ==================
-- Purpose: Request that was combined with another section
INSERT INTO academic_request (id, user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation, accepted, combined)
VALUES (3, 3, 3, 2, '2025-08-01', '2025-12-15', 25, '2025-07-12', 'Evening section preferred', FALSE, TRUE);

-- ==================
-- 4. High Capacity Request - Operating Systems
-- ==================
-- Purpose: Large class request for testing capacity filtering
INSERT INTO academic_request (id, user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation, accepted, combined)
VALUES (4, 3, 4, 2, '2025-08-01', '2025-12-15', 40, '2025-07-08', 'High demand course, need large classroom', FALSE, FALSE);

-- ==================
-- 5. Previous Semester Request - Web Development
-- ==================
-- Purpose: Historical request from past semester
INSERT INTO academic_request (id, user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation, accepted, combined)
VALUES (5, 3, 6, 1, '2025-01-15', '2025-06-30', 30, '2024-12-10', 'Practical lab sessions required', TRUE, FALSE);

-- ============================================
-- Test Request Schedules Data
-- ============================================
-- Purpose: Schedule slots associated with academic requests
-- Each request can have multiple schedule slots (days/times)
-- ============================================

-- Request 1 (Data Structures) - Monday/Wednesday schedule
INSERT INTO request_schedule (id, academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability)
VALUES (1, 1, 1, '08:00:00', '10:00:00', 'Monday', 1, FALSE);

INSERT INTO request_schedule (id, academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability)
VALUES (2, 1, 1, '08:00:00', '10:00:00', 'Wednesday', 1, FALSE);

-- Request 2 (Algorithms) - Tuesday/Thursday schedule
INSERT INTO request_schedule (id, academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability)
VALUES (3, 2, 1, '10:00:00', '12:00:00', 'Tuesday', 1, FALSE);

INSERT INTO request_schedule (id, academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability)
VALUES (4, 2, 1, '10:00:00', '12:00:00', 'Thursday', 1, FALSE);

-- Request 3 (Database Systems) - Monday/Wednesday evening
INSERT INTO request_schedule (id, academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability)
VALUES (5, 3, 2, '18:00:00', '20:00:00', 'Monday', 2, FALSE);

INSERT INTO request_schedule (id, academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability)
VALUES (6, 3, 2, '18:00:00', '20:00:00', 'Wednesday', 2, FALSE);

-- Request 4 (Operating Systems) - Tuesday/Thursday
INSERT INTO request_schedule (id, academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability)
VALUES (7, 4, 1, '14:00:00', '16:00:00', 'Tuesday', 1, TRUE);

INSERT INTO request_schedule (id, academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability)
VALUES (8, 4, 1, '14:00:00', '16:00:00', 'Thursday', 1, TRUE);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Request IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. All requests reference user_id = 3 (testProgram@example.com with ROLE_PROGRAM)
-- 3. Most requests are for current semester (id = 2) for active testing
-- 4. Request 5 is historical (semester id = 1) for filtering tests
-- 5. Status combinations cover all scenarios: pending, accepted, combined
-- 6. Schedules reference parametric data:
--    - classroom_type_id: 1 (Standard), 2 (Lab)
--    - modality_id: 1 (In-person), 2 (Virtual), 3 (Hybrid)
-- 7. Load order: test-users.sql → test-courses.sql → test-semesters.sql → test-academic-requests.sql
-- 8. Dates are within semester boundaries for data consistency
-- 9. Capacity values range from 25-40 for realistic testing
-- 10. Times use 24-hour format (HH:MM:SS)
-- ============================================
