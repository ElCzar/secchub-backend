-- ============================================
-- Test Student Applications Data - Insert sample student applications
-- ============================================
-- Purpose: Provides test student applications for integration testing
--
-- Common Test Data:
-- - User ID: 5 (testStudent@example.com with ROLE_STUDENT)
-- - Course IDs: References test courses from test-courses.sql
-- - Section ID: 1 (Engineering section)
-- - Semester ID: 2 (current semester from test-semesters.sql)
-- - Status IDs: 4 (Pending), 8 (Confirmed), 9 (Rejected)
-- ============================================

-- ==================
-- 1. Pending Application - Data Structures
-- ==================
-- Purpose: New application that hasn't been processed yet
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, program, student_semester, academic_average, phone_number, alternate_phone_number, address, personal_email, was_teaching_assistant, course_average, course_teacher, application_date, status_id)
VALUES (1, 5, 1, 1, 2, 'Computer Science', 6, 4.2, '555-0101', '555-0102', '123 Main St, City', 'student@personal.com', FALSE, 4.5, 'Dr. Smith', '2025-07-15', 4);

-- ==================
-- 2. Approved Application - Algorithms
-- ==================
-- Purpose: Application that has been approved/confirmed
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, program, student_semester, academic_average, phone_number, alternate_phone_number, address, personal_email, was_teaching_assistant, course_average, course_teacher, application_date, status_id)
VALUES (2, 5, 2, 1, 2, 'Computer Science', 6, 4.2, '555-0101', '555-0102', '123 Main St, City', 'student@personal.com', TRUE, 4.8, 'Dr. Johnson', '2025-07-10', 8);

-- ==================
-- 3. Rejected Application - Database Systems
-- ==================
-- Purpose: Application that was rejected
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, program, student_semester, academic_average, phone_number, alternate_phone_number, address, personal_email, was_teaching_assistant, course_average, course_teacher, application_date, status_id)
VALUES (3, 5, 3, 1, 2, 'Computer Science', 6, 4.2, '555-0101', '555-0102', '123 Main St, City', 'student@personal.com', FALSE, 3.8, 'Dr. Williams', '2025-07-12', 9);

-- ==================
-- 4. High GPA Application - Operating Systems
-- ==================
-- Purpose: Application with excellent academic record
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, program, student_semester, academic_average, phone_number, alternate_phone_number, address, personal_email, was_teaching_assistant, course_average, course_teacher, application_date, status_id)
VALUES (4, 5, 4, 1, 2, 'Computer Science', 7, 4.7, '555-0101', '555-0102', '123 Main St, City', 'student@personal.com', TRUE, 4.9, 'Dr. Brown', '2025-07-08', 4);

-- ==================
-- 5. Previous Semester Application - Web Development
-- ==================
-- Purpose: Historical application from past semester
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, program, student_semester, academic_average, phone_number, alternate_phone_number, address, personal_email, was_teaching_assistant, course_average, course_teacher, application_date, status_id)
VALUES (5, 5, 6, 1, 1, 'Computer Science', 5, 4.0, '555-0101', '555-0102', '123 Main St, City', 'student@personal.com', FALSE, 4.3, 'Dr. Davis', '2024-12-10', 8);

-- ============================================
-- Test Student Application Schedules Data
-- ============================================
-- Purpose: Availability schedules for student applications
-- Students declare when they are available to work as TAs
-- ============================================

-- Application 1 (Data Structures) - Monday/Wednesday availability
INSERT INTO student_application_schedule (id, student_application_id, day, start_time, end_time)
VALUES (1, 1, 'Monday', '14:00:00', '18:00:00');

INSERT INTO student_application_schedule (id, student_application_id, day, start_time, end_time)
VALUES (2, 1, 'Wednesday', '14:00:00', '18:00:00');

-- Application 2 (Algorithms) - Tuesday/Thursday availability
INSERT INTO student_application_schedule (id, student_application_id, day, start_time, end_time)
VALUES (3, 2, 'Tuesday', '10:00:00', '14:00:00');

INSERT INTO student_application_schedule (id, student_application_id, day, start_time, end_time)
VALUES (4, 2, 'Thursday', '10:00:00', '14:00:00');

-- Application 3 (Database Systems) - Friday availability
INSERT INTO student_application_schedule (id, student_application_id, day, start_time, end_time)
VALUES (5, 3, 'Friday', '08:00:00', '12:00:00');

-- Application 4 (Operating Systems) - Flexible schedule
INSERT INTO student_application_schedule (id, student_application_id, day, start_time, end_time)
VALUES (6, 4, 'Monday', '08:00:00', '12:00:00');

INSERT INTO student_application_schedule (id, student_application_id, day, start_time, end_time)
VALUES (7, 4, 'Tuesday', '08:00:00', '12:00:00');

INSERT INTO student_application_schedule (id, student_application_id, day, start_time, end_time)
VALUES (8, 4, 'Wednesday', '08:00:00', '12:00:00');

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Application IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. All applications reference user_id = 5 (testStudent@example.com with ROLE_STUDENT)
-- 3. Most applications are for current semester (id = 2) for active testing
-- 4. Application 5 is historical (semester id = 1) for filtering tests
-- 5. Status combinations:
--    - status_id = 4: Pending (new applications)
--    - status_id = 8: Confirmed (approved applications)
--    - status_id = 9: Rejected (denied applications)
-- 6. Academic averages range from 4.0-4.7 (Colombian scale, max 5.0)
-- 7. Course averages range from 3.8-4.9 for realistic testing
-- 8. Load order: test-users.sql → test-sections.sql → test-courses.sql → test-semesters.sql → test-student-applications.sql
-- 9. Schedules show student availability (not class times)
-- 10. Times use 24-hour format (HH:MM:SS)
-- ============================================
