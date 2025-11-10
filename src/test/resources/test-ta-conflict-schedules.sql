-- ============================================
-- Test TA Conflict Schedules Data - TA Schedules with intentional conflicts
-- ============================================
-- Purpose: Provides test data for teaching assistant schedule conflict detection
--
-- Conflict Scenarios:
-- 1. Complex overlaps: Same user as TA for multiple classes with overlapping schedules
-- 2. Different sections: TAs from different sections (for ROLE_SECTION filtering)
-- 3. No conflicts: TAs with different days or times
-- ============================================

-- ==================
-- Additional Student Applications for TA Conflict Testing
-- ==================
-- User 3 (Student) will have 5 separate applications for 5 different TA positions in Section 1
-- Each application is for a different course, and each will have ONE TA assignment
-- All are Status 8 = Confirmed (required by conflict detection query)

-- Student Application 10 (User 3, Course 1, Section 1)
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, status_id)
VALUES (10, 3, 1, 1, 2, 8);

-- Student Application 20 (User 3, Course 2, Section 1)
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, status_id)
VALUES (20, 3, 2, 1, 2, 8);

-- Student Application 30 (User 3, Course 3, Section 1)
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, status_id)
VALUES (30, 3, 3, 1, 2, 8);

-- Student Application 40 (User 3, Course 4, Section 1)
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, status_id)
VALUES (40, 3, 4, 1, 2, 8);

-- Student Application 50 (User 3, Course 5, Section 1)
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, status_id)
VALUES (50, 3, 5, 1, 2, 8);

-- Student Applications for Section 2 (User 3)
-- Student Application 60 (User 3, Course 1, Section 2)
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, status_id)
VALUES (60, 3, 1, 2, 2, 8);

-- Student Application 70 (User 3, Course 2, Section 2)
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, status_id)
VALUES (70, 3, 2, 2, 2, 8);

-- ==================
-- Teaching Assistant Assignments for Conflict Testing
-- User 3 (Student) will be TA for 5 different classes in Section 1
-- Each TA has its own unique student application (one-to-one relationship)
-- ==================
INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (10, 10, 10, 10, 15, 150);

INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (20, 20, 20, 10, 15, 150);

INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (30, 30, 30, 10, 15, 150);

INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (40, 40, 40, 10, 15, 150);

INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (50, 50, 50, 10, 15, 150);

-- ==================
-- Teaching Assistant Assignments for Section 2 (User 3 - Student)
-- ==================
INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (60, 60, 60, 10, 15, 150);

INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (70, 70, 70, 10, 15, 150);

-- ==================
-- TA Conflicting Schedules - Complex Overlaps (User 3 - Student, Section 1)
-- Same schedules as classroom conflicts but for teaching assistants
-- Monday - Creates 3 conflict clusters for user 3
-- TA 10: 11:00-13:00
-- TA 20: 10:00-12:00 (overlaps with 10 and 50)
-- TA 30: 12:00-15:00 (overlaps with 10 and 40)
-- TA 40: 14:00-17:00 (overlaps with 30)
-- TA 50: 09:00-12:00 (overlaps with 10 and 20)
-- ==================
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (100, 10, 'Monday', '11:00:00', '13:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (101, 20, 'Monday', '10:00:00', '12:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (102, 30, 'Monday', '12:00:00', '15:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (103, 40, 'Monday', '14:00:00', '17:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (104, 50, 'Monday', '09:00:00', '12:00:00');

-- ==================
-- TA Conflicting Schedules - Section 2 (User 3 - Student, Section 2)
-- Tuesday - For ROLE_SECTION filtering
-- TA 60: 08:00-10:00
-- TA 70: 09:00-11:00 (overlaps with 60)
-- ==================
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (110, 60, 'Tuesday', '08:00:00', '10:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (111, 70, 'Tuesday', '09:00:00', '11:00:00');

-- ==================
-- Non-Conflicting Schedules - Different Days (User 3 - Student)
-- ==================
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (120, 10, 'Wednesday', '10:00:00', '12:00:00');

INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time)
VALUES (121, 20, 'Thursday', '10:00:00', '12:00:00');

-- ============================================
-- Expected Conflicts Summary:
-- ============================================
-- User 3 - Student (Section 1), Monday:
--   - Cluster 1: [10, 20, 50] - All three overlap with each other
--     * TA 10 (11:00-13:00) overlaps with TA 20 (10:00-12:00): 11:00-12:00
--     * TA 10 (11:00-13:00) overlaps with TA 50 (09:00-12:00): 11:00-12:00
--     * TA 20 (10:00-12:00) overlaps with TA 50 (09:00-12:00): 10:00-12:00
--   - Cluster 2: [10, 30] - Both overlap
--     * TA 10 (11:00-13:00) overlaps with TA 30 (12:00-15:00): 12:00-13:00
--   - Cluster 3: [30, 40] - Both overlap
--     * TA 30 (12:00-15:00) overlaps with TA 40 (14:00-17:00): 14:00-15:00
--
-- User 3 - Student (Section 2), Tuesday:
--   - Cluster: [60, 70] - Both overlap (09:00-10:00)
--
-- Wednesday/Thursday: NO CONFLICTS (different days)
-- ============================================
