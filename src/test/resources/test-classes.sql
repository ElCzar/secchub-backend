-- ============================================
-- Test Classes Data - Insert sample classes
-- ============================================
-- Purpose: Provides test classes for integration testing
--
-- Common Test Data:
-- - Section: 1 (Engineering section from test-sections.sql)
-- - Course IDs: References test courses from test-courses.sql
-- - Semester ID: 2 (current semester), 1 (previous semester)
-- - Status IDs: 1 (Active), 10 (Planned), 11 (Completed)
-- ============================================

-- ==================
-- 1. Current Semester - Data Structures (Active)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (1, 1, 1, 2, '2025-08-01', '2025-12-15', 'Morning section', 30, 1);

-- ==================
-- 2. Current Semester - Algorithms (Active)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (2, 1, 2, 2, '2025-08-01', '2025-12-15', 'Afternoon section', 35, 1);

-- ==================
-- 3. Current Semester - Database Systems (Active)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (3, 1, 3, 2, '2025-08-01', '2025-12-15', 'Evening section', 28, 1);

-- ==================
-- 4. Current Semester - Operating Systems (Planned)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (4, 1, 4, 2, '2025-08-01', '2025-12-15', 'Lab-intensive course', 25, 10);

-- ==================
-- 5. Previous Semester - Web Development (Completed)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (5, 1, 6, 1, '2024-08-01', '2024-12-15', 'Completed class', 30, 11);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Class IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. Section 1 references Engineering section from test-sections.sql
-- 3. Course IDs reference test-courses.sql (1=Data Structures, 2=Algorithms, etc.)
-- 4. Semester IDs: 2=Current, 1=Previous (from test-semesters.sql)
-- 5. Status IDs: 1=Active, 10=Planned, 11=Completed (from init-parameters.sql)
-- 6. Date ranges span typical semester duration (August-December)
-- 7. Capacity matches typical classroom sizes
-- 8. Load order: test-sections → test-courses → test-semesters → test-classes.sql
-- ============================================
