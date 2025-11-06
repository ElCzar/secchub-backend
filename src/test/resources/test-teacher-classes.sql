-- ============================================
-- Test Teacher Classes Data - Insert sample teacher-class assignments
-- ============================================
-- Purpose: Provides test teacher-class assignments for integration testing
--
-- Common Test Data:
-- - Semester ID: 2 (current semester), 1 (previous semester)
-- - Teacher ID: 1 (testTeacher from test-teachers.sql)
-- - Class IDs: References test classes from test-classes.sql
-- - Status IDs: 4 (Pending), 6 (Accepted), 7 (Rejected)
-- - Decision: NULL (pending), TRUE (accepted), FALSE (rejected)
-- ============================================

-- ==================
-- 1. Pending Assignment - Data Structures
-- ==================
-- Purpose: New assignment awaiting teacher confirmation
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id)
VALUES (1, 2, 1, 1, 4, 0, 0, NULL, NULL, 4);

-- ==================
-- 2. Accepted Assignment - Algorithms
-- ==================
-- Purpose: Assignment accepted by teacher
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id)
VALUES (2, 2, 1, 2, 4, 0, 0, TRUE, 'Looking forward to teaching this course', 6);

-- ==================
-- 3. Rejected Assignment - Database Systems
-- ==================
-- Purpose: Assignment rejected by teacher with reason
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id)
VALUES (3, 2, 1, 3, 6, 0, 0, FALSE, 'Schedule conflict with research project', 7);

-- ==================
-- 4. Pending Assignment with Extra Hours - Operating Systems
-- ==================
-- Purpose: Assignment with additional hours compensation
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id)
VALUES (4, 2, 1, 4, 4, 2, 0, NULL, NULL, 4);

-- ==================
-- 5. Previous Semester - Web Development (Accepted, Completed)
-- ==================
-- Purpose: Historical assignment for filtering tests
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id)
VALUES (5, 1, 1, 5, 3, 0, 0, TRUE, 'Successfully completed', 6);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Teacher-class IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. Teacher ID 1 references testTeacher@example.com (user_id 4, ROLE_TEACHER)
-- 3. Semester IDs: 2=Current, 1=Previous (from test-semesters.sql)
-- 4. Class IDs reference test-classes.sql
-- 5. Status IDs:
--    - 4 = Pending (awaiting teacher decision)
--    - 6 = Accepted (teacher confirmed)
--    - 7 = Rejected (teacher declined)
-- 6. Decision field:
--    - NULL = Pending decision
--    - TRUE = Accepted
--    - FALSE = Rejected
-- 7. Work hours represent weekly teaching load
-- 8. Extra hours compensate for additional responsibilities
-- 9. Observations contain teacher's comments about the assignment
-- 10. Load order: test-teachers → test-classes → test-teacher-classes.sql
-- ============================================
