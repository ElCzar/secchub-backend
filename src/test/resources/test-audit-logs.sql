-- ============================================
-- Test Audit Logs Data - Insert sample audit log entries
-- ============================================
-- Purpose: Provides test audit logs for integration testing
--
-- Common Test Data:
-- - Emails: References test users from test-users.sql
-- - Actions: CREATE, UPDATE, DELETE
-- - Method Names: Various service methods that generate audit logs
-- - Timestamps: Various dates for date range filtering
-- ============================================

-- ===================
-- Drop existing data to avoid conflicts
-- ===================
DELETE FROM audit_log;

-- ==================
-- 1. Admin CREATE actions
-- ==================
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (1, 'testAdmin@example.com', 'CREATE', 'createSemester', TIMESTAMP('2025-10-01', '10:00:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (2, 'testAdmin@example.com', 'CREATE', 'createSection', TIMESTAMP('2025-10-05', '14:30:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (3, 'testAdmin@example.com', 'CREATE', 'createCourse', TIMESTAMP('2025-10-10', '09:15:00'));

-- ==================
-- 2. Admin UPDATE actions
-- ==================
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (4, 'testAdmin@example.com', 'UPDATE', 'updateSemester', TIMESTAMP('2025-10-15', '11:45:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (5, 'testAdmin@example.com', 'UPDATE', 'updateCourse', TIMESTAMP('2025-10-20', '16:20:00'));

-- ==================
-- 3. Admin DELETE actions
-- ==================
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (6, 'testAdmin@example.com', 'DELETE', 'deleteCourse', TIMESTAMP('2025-10-25', '13:00:00'));

-- ==================
-- 4. User CREATE actions
-- ==================
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (7, 'testUser@example.com', 'CREATE', 'createTeacher', TIMESTAMP('2025-10-12', '10:30:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (8, 'testUser@example.com', 'CREATE', 'createTeacherClass', TIMESTAMP('2025-10-18', '15:00:00'));

-- ==================
-- 5. User UPDATE actions
-- ==================
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (9, 'testUser@example.com', 'UPDATE', 'updateTeacher', TIMESTAMP('2025-10-22', '12:15:00'));

-- ==================
-- 6. Teacher actions
-- ==================
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (10, 'testTeacher@example.com', 'UPDATE', 'acceptTeacherClass', TIMESTAMP('2025-10-28', '09:00:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (11, 'testTeacher@example.com', 'UPDATE', 'rejectTeacherClass', TIMESTAMP('2025-10-29', '14:45:00'));

-- ==================
-- 7. Student actions
-- ==================
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (12, 'testStudent@example.com', 'CREATE', 'createStudentApplication', TIMESTAMP('2025-11-01', '10:00:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (13, 'testStudent@example.com', 'UPDATE', 'updateStudentApplication', TIMESTAMP('2025-11-02', '11:30:00'));

-- ==================
-- 8. Program actions
-- ==================
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (14, 'testProgram@example.com', 'CREATE', 'createAcademicRequest', TIMESTAMP('2025-11-03', '08:45:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (15, 'testProgram@example.com', 'UPDATE', 'updateAcademicRequest', TIMESTAMP('2025-11-04', '16:00:00'));

-- ==================
-- 9. Additional mixed actions for date range testing
-- ==================
-- Early October entries
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (16, 'testAdmin@example.com', 'CREATE', 'createClassroom', TIMESTAMP('2025-10-02', '08:00:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (17, 'testUser@example.com', 'DELETE', 'deleteTeacher', TIMESTAMP('2025-10-03', '17:30:00'));

-- Late October entries
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (18, 'testAdmin@example.com', 'UPDATE', 'updateSection', TIMESTAMP('2025-10-30', '10:00:00'));

INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (19, 'testUser@example.com', 'CREATE', 'createClass', TIMESTAMP('2025-10-31', '14:00:00'));

-- Early November entries
INSERT INTO audit_log (id, email, action, method_name, timestamp)
VALUES (20, 'testAdmin@example.com', 'DELETE', 'deleteSemester', TIMESTAMP('2025-11-05', '09:30:00'));

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Audit log IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. Emails reference test users from test-users.sql:
--    - testAdmin@example.com (ROLE_ADMIN)
--    - testUser@example.com (ROLE_USER)
--    - testTeacher@example.com (ROLE_TEACHER)
--    - testStudent@example.com (ROLE_STUDENT)
--    - testProgram@example.com (ROLE_PROGRAM)
-- 3. Actions: CREATE, UPDATE, DELETE (standard audit actions)
-- 4. Method names follow actual service method naming conventions
-- 5. Timestamps span October-November 2025 for date range testing
-- 6. Timestamp format: 'YYYY-MM-DD HH:MM:SS' (MySQL DATETIME format)
-- 7. Action distribution:
--    - CREATE: 10 entries
--    - UPDATE: 8 entries
--    - DELETE: 2 entries
-- 8. Email distribution:
--    - testAdmin@example.com: 9 entries
--    - testUser@example.com: 5 entries
--    - testTeacher@example.com: 2 entries
--    - testStudent@example.com: 2 entries
--    - testProgram@example.com: 2 entries
-- 9. Load order: test-users.sql → test-audit-logs.sql
-- 10. All endpoints require ROLE_ADMIN for access
-- ============================================
