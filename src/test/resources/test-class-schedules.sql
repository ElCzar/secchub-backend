-- ============================================
-- Test Class Schedules Data - Insert sample class schedules
-- ============================================
-- Purpose: Provides test class schedules for integration testing
--
-- Common Test Data:
-- - Class IDs: References test classes from test-classes.sql
-- - Classroom IDs: References test classrooms from test-classrooms.sql
-- - Modality IDs: 1 (Presencial), 2 (Online)
-- - Days: Lunes-Viernes
-- - Times: Use LocalTime format (HH:mm:ss)
-- ============================================

-- ==================
-- Class 1 (Data Structures) - Lunes/Miercoles
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (1, 1, 1, 'Lunes', '08:00:00', '10:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (2, 1, 1, 'Miercoles', '08:00:00', '10:00:00', 1, FALSE);

-- ==================
-- Class 2 (Algorithms) - Martes/Jueves
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (3, 2, 2, 'Martes', '14:00:00', '16:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (4, 2, 2, 'Jueves', '14:00:00', '16:00:00', 1, FALSE);

-- ==================
-- Class 3 (Database Systems) - Viernes (Lab)
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (5, 3, 5, 'Viernes', '10:00:00', '14:00:00', 1, TRUE);

-- ==================
-- Class 4 (Operating Systems) - Lunes/Miercoles (Planned)
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (6, 4, 3, 'Lunes', '16:00:00', '18:00:00', 1, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (7, 4, 3, 'Miercoles', '16:00:00', '18:00:00', 1, FALSE);

-- ==================
-- Class 5 (Web Development - Previous Semester) - Martes/Jueves (Online)
-- ==================
INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (8, 5, NULL, 'Martes', '18:00:00', '20:00:00', 2, FALSE);

INSERT INTO class_schedule (id, class_id, classroom_id, day, start_time, end_time, modality_id, disability)
VALUES (9, 5, NULL, 'Jueves', '18:00:00', '20:00:00', 2, FALSE);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Schedule IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. All times use 24-hour format (HH:mm:ss) and LocalTime type
-- 3. Modality IDs: 1=Presencial, 2=Online (from init-parameters.sql)
-- 4. Online classes (modality=2) have NULL classroom_id
-- 5. Disability flag indicates if classroom is accessible
-- 6. Lab sessions (Database Systems) have longer duration (4 hours)
-- 7. Load order: test-classes → test-classrooms → test-class-schedules.sql
-- ============================================
