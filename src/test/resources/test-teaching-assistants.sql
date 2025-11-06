-- ============================================
-- Test Teaching Assistants Data - Insert sample teaching assistant assignments
-- ============================================
-- Purpose: Provides test teaching assistant assignments for integration testing
--
-- Common Test Data:
-- - Class IDs: References test classes from test-classes.sql
-- - Student Application IDs: References test-student-applications.sql
-- - Weekly hours: Typical 8-12 hours per week
-- - Weeks: Standard semester length (16 weeks)
-- - Total hours: weekly_hours × weeks
-- ============================================

-- ==================
-- 1. TA Assignment for Data Structures (Application 2 - Approved)
-- ==================
-- Purpose: Active TA assignment from approved application
INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (1, 1, 2, 10, 16, 160);

-- ==================
-- 2. TA Assignment for Algorithms (Application 1 - Pending)
-- ==================
-- Purpose: TA assignment for pending application
INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (2, 2, 1, 12, 16, 192);

-- ==================
-- 3. TA Assignment for Database Systems (Application 4 - High GPA)
-- ==================
-- Purpose: Part-time TA assignment for high GPA student
INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (3, 3, 4, 8, 16, 128);

-- ==================
-- 4. TA Assignment for Operating Systems (Application 3 - Rejected but assigned)
-- ==================
-- Purpose: TA assignment even though application was rejected (edge case)
INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (4, 4, 3, 12, 16, 192);

-- ==================
-- 5. Previous Semester TA Assignment (Application 5 - Historical)
-- ==================
-- Purpose: Historical TA assignment from past semester
INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours)
VALUES (5, 5, 5, 10, 16, 160);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. TA IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. All TAs reference valid class_id and student_application_id
-- 3. Weekly hours typically range from 8-12 hours
-- 4. Standard semester length is 16 weeks
-- 5. Total hours = weekly_hours × weeks
-- 6. Each student application has exactly ONE teaching assistant assignment
-- 7. Applications 1-5 each have one TA assignment (one-to-one relationship)
-- 8. Load order: test-classes.sql → test-student-applications.sql → test-teaching-assistants.sql
-- ============================================
