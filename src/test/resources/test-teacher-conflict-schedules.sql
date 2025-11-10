-- ============================================
-- Test Teacher Conflict Schedules Data - Schedules with intentional teacher conflicts
-- ============================================
-- Purpose: Provides test data for teacher schedule conflict detection
--
-- Conflict Scenarios:
-- 1. Simple overlap: Two classes assigned to same teacher with overlapping times
-- 2. Complex overlaps: Multiple classes with transitive overlaps (same 5 schedules as classroom tests)
-- 3. Different sections: Classes from different sections (for ROLE_SECTION filtering)
-- 4. No conflicts: Classes with different days or times
--
-- Note: Creates teacher ID 1 (user_id 4 - testTeacher@example.com) for testing
-- ============================================

-- ==================
-- Teacher for Conflict Testing
-- ==================
-- User ID 4 is testTeacher@example.com from test-users.sql
INSERT INTO teacher (id, user_id, employment_type_id, max_hours)
VALUES (1, 4, 1, 40);

-- ==================
-- Additional Classes for Conflict Testing (Section 1)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (10, 1, 1, 2, '2025-08-01', '2025-12-15', 'Teacher Conflict Test Class 1', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (20, 1, 2, 2, '2025-08-01', '2025-12-15', 'Teacher Conflict Test Class 2', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (30, 1, 3, 2, '2025-08-01', '2025-12-15', 'Teacher Conflict Test Class 3', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (40, 1, 4, 2, '2025-08-01', '2025-12-15', 'Teacher Conflict Test Class 4', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (50, 1, 5, 2, '2025-08-01', '2025-12-15', 'Teacher Conflict Test Class 5', 30, 1);

-- ==================
-- Additional Classes for Section Filtering (Section 2)
-- ==================
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (60, 2, 1, 2, '2025-08-01', '2025-12-15', 'Section 2 Teacher Class 1', 30, 1);

INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id)
VALUES (70, 2, 2, 2, '2025-08-01', '2025-12-15', 'Section 2 Teacher Class 2', 30, 1);

-- ==================
-- Teacher Assignments - Simple Overlap (Teacher 1, Monday)
-- Two classes assigned to same teacher with same time
-- Note: Classes 10 and 20 on Monday
-- ==================
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id, start_date, end_date)
VALUES (100, 2, 1, 10, 4, 0, 0, TRUE, 'Accepted', 6, '2025-08-01', '2025-12-15');

INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id, start_date, end_date)
VALUES (101, 2, 1, 20, 4, 0, 0, TRUE, 'Accepted', 6, '2025-08-01', '2025-12-15');

-- ==================
-- Teacher Assignments - Complex Overlaps (Teacher 1, Wednesday)
-- Using same 5-schedule pattern as classroom tests:
-- Class 10: 11:00-13:00 (already assigned for Monday)
-- Class 20: 10:00-12:00 (already assigned for Monday)
-- Class 30: 12:00-15:00
-- Class 40: 14:00-17:00
-- Class 50: 09:00-12:00
-- Expected clusters: [10,20,50], [10,30], [30,40]
-- Note: Reusing classes 10 and 20 that are already assigned
-- ==================
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id, start_date, end_date)
VALUES (130, 2, 1, 30, 4, 0, 0, TRUE, 'Accepted', 6, '2025-08-01', '2025-12-15');

INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id, start_date, end_date)
VALUES (140, 2, 1, 40, 4, 0, 0, TRUE, 'Accepted', 6, '2025-08-01', '2025-12-15');

INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id, start_date, end_date)
VALUES (150, 2, 1, 50, 4, 0, 0, TRUE, 'Accepted', 6, '2025-08-01', '2025-12-15');

-- ==================
-- Teacher Assignments - Section 2 Conflicts (Teacher 1, Friday)
-- For ROLE_SECTION filtering tests
-- Note: Using same teacher for section 2 classes to test filtering
-- ==================
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id, start_date, end_date)
VALUES (160, 2, 1, 60, 4, 0, 0, TRUE, 'Accepted', 6, '2025-08-01', '2025-12-15');

INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id, start_date, end_date)
VALUES (161, 2, 1, 70, 4, 0, 0, TRUE, 'Accepted', 6, '2025-08-01', '2025-12-15');

-- ==================
-- Conflicting Schedules - Scenario 1: Simple Overlap
-- Teacher 1, Monday, 10:00-12:00 (Two classes)
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (200, 10, 1, 'Monday', '10:00:00', '12:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (201, 20, 1, 'Monday', '10:00:00', '12:00:00', 1, FALSE);

-- ==================
-- Conflicting Schedules - Scenario 2: Complex Overlaps
-- Teacher 1, Wednesday - Same 5-schedule pattern as classroom tests
-- Expected 3 clusters: [10,20,50], [10,30], [30,40]
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (210, 10, 2, 'Wednesday', '11:00:00', '13:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (220, 20, 2, 'Wednesday', '10:00:00', '12:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (230, 30, 2, 'Wednesday', '12:00:00', '15:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (240, 40, 2, 'Wednesday', '14:00:00', '17:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (250, 50, 2, 'Wednesday', '09:00:00', '12:00:00', 1, FALSE);

-- ==================
-- Conflicting Schedules - Scenario 3: Section 2 Conflicts
-- Teacher 1, Friday - For ROLE_SECTION filtering
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (260, 60, 3, 'Friday', '08:00:00', '10:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (261, 70, 3, 'Friday', '09:00:00', '11:00:00', 1, FALSE);

-- ==================
-- Non-Conflicting Schedules - Different Days
-- Teacher 1, Tuesday/Thursday - Same time but different days
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (270, 10, 4, 'Tuesday', '10:00:00', '12:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (271, 20, 4, 'Thursday', '10:00:00', '12:00:00', 1, FALSE);

-- ============================================
-- Expected Conflicts Summary:
-- ============================================
-- Teacher 1 (user_id 4 - testTeacher@example.com), Monday:
--   - Cluster: [10, 20] (same time 10:00-12:00)
--
-- Teacher 1, Wednesday (Complex - Same as classroom tests):
--   - Cluster 1: [10, 20, 50] (all three overlap transitively)
--     - 10 (11:00-13:00) overlaps with 20 (10:00-12:00): 11:00-12:00
--     - 10 (11:00-13:00) overlaps with 50 (09:00-12:00): 11:00-12:00
--     - 20 (10:00-12:00) overlaps with 50 (09:00-12:00): 10:00-12:00
--   - Cluster 2: [10, 30] (10 overlaps with 30)
--     - 10 (11:00-13:00) overlaps with 30 (12:00-15:00): 12:00-13:00
--   - Cluster 3: [30, 40] (30 overlaps with 40)
--     - 30 (12:00-15:00) overlaps with 40 (14:00-17:00): 14:00-15:00
--
-- Teacher 1, Friday (Section 2):
--   - Cluster: [60, 70] (overlap 09:00-10:00)
--
-- Teacher 1, Tuesday/Thursday: NO CONFLICTS (different days)
-- ============================================
