-- ============================================
-- Test Teachers Data - Insert sample teachers
-- ============================================
-- Purpose: Provides test teachers for integration testing of teacher-related operations
--
-- Common Test Data:
-- - User ID: References test users from test-users.sql
-- - Employment Type ID: 1 (Full-time) or 2 (Adjunct/Part-time)
-- - Max Hours: Maximum teaching hours per semester
-- ============================================

INSERT INTO teacher (
    id,                 -- Primary key (auto-incremented)
    user_id,            -- FK to users table
    employment_type_id, -- FK to employment_type table (1=Full-time, 2=Adjunct)
    max_hours           -- Maximum teaching hours allowed per semester
) VALUES
-- ==================
-- 1. Full-Time Teacher
-- ==================
-- Purpose: Primary full-time teacher for comprehensive testing
-- User: testTeacher@example.com (user_id will be 4 after test-users.sql)
(
    1,                  -- Teacher ID
    4,                  -- Teacher user
    1,                  -- Full-time employment
    40                  -- 40 hours maximum per semester
);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. User IDs reference test users created by test-users.sql
-- 2. Employment type IDs reference parametric data (init-parameters.sql):
--    - 1 = Full-time (typically 35-40 hours)
--    - 2 = Adjunct/Part-time (typically 15-20 hours)
-- 3. Max hours represent maximum teaching load per semester
-- 4. testTeacher@example.com (user_id 4) has ROLE_TEACHER in test-users.sql
-- 5. Other users have teaching assignments for multi-role testing
-- 6. This data should be loaded AFTER test-users.sql
-- 7. Used by teacher_class table for assigning teachers to classes
-- 8. Load order: test-users.sql → test-teachers.sql
-- ============================================
