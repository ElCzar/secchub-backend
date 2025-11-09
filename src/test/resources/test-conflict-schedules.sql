-- ============================================
-- Test Conflict Schedules Data - Schedules with intentional conflicts
-- ============================================
-- Purpose: Provides test data for classroom schedule conflict detection
--
-- Conflict Scenarios:
-- 1. Simple overlap: Two classes in same classroom/day/time
-- 2. Complex overlaps: Multiple classes with overlaps
-- 3. Different sections: Classes from different sections (for ROLE_SECTION filtering)
-- 4. No conflicts: Classes with different days or times
-- ============================================

-- ==================
-- Additional Classes for Conflict Testing (Section 1)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (10, 1, 1, 2, '2025-08-01', '2025-12-15', 'Conflict Test Class 1', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (20, 1, 2, 2, '2025-08-01', '2025-12-15', 'Conflict Test Class 2', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (30, 1, 3, 2, '2025-08-01', '2025-12-15', 'Conflict Test Class 3', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (40, 1, 4, 2, '2025-08-01', '2025-12-15', 'Conflict Test Class 4', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (50, 1, 5, 2, '2025-08-01', '2025-12-15', 'Conflict Test Class 5', 30, 1);

-- ==================
-- Additional Classes for Section Filtering (Section 2)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (60, 2, 1, 2, '2025-08-01', '2025-12-15', 'Section 2 Class 1', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (70, 2, 2, 2, '2025-08-01', '2025-12-15', 'Section 2 Class 2', 30, 1);

-- ==================
-- Conflicting Schedules - Scenario 1: Simple Overlap
-- Classroom 1, Monday, 10:00-12:00 (Two classes)
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (100, 10, 1, 'Monday', '10:00:00', '12:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (101, 20, 1, 'Monday', '10:00:00', '12:00:00', 1, FALSE);

-- ==================
-- Conflicting Schedules - Scenario 2: Complex Overlaps
-- Classroom 2, Wednesday - Creates 4 conflict clusters
-- Class 10: 11:00-13:00
-- Class 20: 10:00-12:00 (overlaps with 10 and 50)
-- Class 30: 12:00-15:00 (no overlap with others - standalone)
-- Class 40: 14:00-17:00 (no overlap with others - standalone)
-- Class 50: 09:00-12:00 (overlaps with 20)
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (110, 10, 2, 'Wednesday', '11:00:00', '13:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (120, 20, 2, 'Wednesday', '10:00:00', '12:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (130, 30, 2, 'Wednesday', '12:00:00', '15:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (140, 40, 2, 'Wednesday', '14:00:00', '17:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (150, 50, 2, 'Wednesday', '09:00:00', '12:00:00', 1, FALSE);

-- ==================
-- Conflicting Schedules - Scenario 3: Section 2 Conflicts
-- Classroom 3, Friday - For ROLE_SECTION filtering
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (160, 60, 3, 'Friday', '08:00:00', '10:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (161, 70, 3, 'Friday', '09:00:00', '11:00:00', 1, FALSE);

-- ==================
-- Non-Conflicting Schedules - Different Days
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (170, 10, 4, 'Tuesday', '10:00:00', '12:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (171, 20, 4, 'Thursday', '10:00:00', '12:00:00', 1, FALSE);

-- ============================================
-- Expected Conflicts Summary:
-- ============================================
-- Classroom 1, Monday:
--   - Cluster: [10, 20] (same time 10:00-12:00)
--
-- Classroom 2, Wednesday:
--   - Cluster 1: [10, 20] (10 overlaps with 20: 11:00-12:00)
--   - Cluster 2: [10, 50] (10 overlaps with 50: 11:00-12:00)
--   - Cluster 3: [20, 50] (20 overlaps with 50: 10:00-12:00)
--   - Cluster 4: [30, 40] (30 overlaps with 40: 14:00-15:00)
--
-- Classroom 3, Friday (Section 2):
--   - Cluster: [60, 70] (overlap 09:00-10:00)
--
-- Classroom 4: NO CONFLICTS (different days)
-- ============================================
