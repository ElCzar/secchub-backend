-- ============================================
-- Test Cleanup SQL - Delete all data
-- ============================================
-- IMPORTANT: Order matters due to foreign key constraints
-- Delete child tables first, then parent tables

-- Delete from tables with no dependencies first
DELETE FROM `teaching_assistant_schedule`;
DELETE FROM `student_application_schedule`;
DELETE FROM `class_schedule`;
DELETE FROM `request_schedule`;

-- Delete from intermediate tables
DELETE FROM `teacher_class`;
DELETE FROM `teaching_assistant`;

-- Delete from main operational tables
DELETE FROM `class`;
DELETE FROM `academic_request`;
DELETE FROM `student_application`;

-- Delete from entity tables
DELETE FROM `teacher`;
DELETE FROM `classroom`;
DELETE FROM `semester`;
DELETE FROM `course`;
DELETE FROM `section`;

-- Delete from users table
DELETE FROM `users`;