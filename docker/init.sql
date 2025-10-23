-- Status of the users and the state of processes --
INSERT INTO status (id, name) VALUES
    (1, 'Active'),
    (2, 'Inactive'),
    (3, 'Finished'),
    (4, 'Pending'),
    (5, 'Cancelled'),
    (6, 'In Progress'),
    (7, 'On Hold'),
    (8, 'Confirmed'),
    (9, 'Rejected'),
    (10, 'Completed'),
    (11, 'Created'),
    (12, 'Change'),
    (13, 'Deleted'),
    (14,'Uploaded');
    

-- Roles based on user permissions --
INSERT INTO role (id, name) VALUES 
    (1, 'ROLE_ADMIN'), 
    (2, 'ROLE_USER'), 
    (3, 'ROLE_STUDENT'), 
    (4, 'ROLE_TEACHER'),
    (5, 'ROLE_PROGRAM');

-- Document Types valid in Colombia --
INSERT INTO document_type (id, name) VALUES
    (1, 'CC'),
    (2, 'TI'),
    (3, 'CE'),
    (4, 'RC'),
    (5, 'NIT'),
    (6, 'Passport');

-- The types of employment --
INSERT INTO employment_type (id, name) VALUES
    (1, 'Full-Time'),
    (2, 'Part-Time');

-- The modalities of the courses --
INSERT INTO modality (id, name) VALUES
    (1, 'In-Person'),
    (2, 'Online');

-- The types of classrooms --
INSERT INTO classroom_type (id, name) VALUES
    (1, 'Lecture'),
    (2, 'Lab');

-- Sample users with password hashed --
INSERT INTO users (username, password, name, last_name, email, status_id, role_id, document_type_id, document_number)
VALUES 
('admin', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Admin', 'User', 'admin@secchub.com', 1, 1, 1, '12345678'),
('user', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Regular', 'User', 'user@secchub.com', 1, 2, 1, '87654321'),
('student', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Student', 'User', 'student@secchub.com', 1, 3, 1, '11111111'),
('teacher', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Teacher', 'User', 'teacher@secchub.com', 1, 4, 1, '22222222'),
('program', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Program', 'User', 'program@secchub.com', 1, 5, 1, '33333333'),
('maria.garcia', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Maria', 'Garcia', 'maria.garcia@secchub.com', 1, 4, 1, '44444444'),
('carlos.lopez', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Carlos', 'Lopez', 'carlos.lopez@secchub.com', 1, 4, 1, '55555555'),
('ana.rodriguez', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Ana', 'Rodriguez', 'ana.rodriguez@secchub.com', 1, 3, 1, '66666666'),
('luis.martinez', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Luis', 'Martinez', 'luis.martinez@secchub.com', 1, 3, 1, '77777777'),
('sofia.hernandez', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Sofia', 'Hernandez', 'sofia.hernandez@secchub.com', 1, 3, 1, '88888888'),
('user-is', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Regular', 'User', 'user-is@secchub.com', 1, 2, 1, '87654321'),
('user-si', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Regular', 'User', 'user-si@secchub.com', 1, 2, 1, '87654321');

-- Teachers data --
INSERT INTO teacher (user_id, employment_type_id, max_hours) VALUES
(4, 1, 40),  -- teacher user
(6, 1, 40),  -- maria.garcia
(7, 2, 20);  -- carlos.lopez

-- Sections data --
INSERT INTO section (user_id, name) VALUES
(2, 'Computer Science'),
(11, 'Information Systems'),
(12, 'Software Engineering');

-- Courses data --
INSERT INTO course (section_id, name, credits, description, requirement, is_valid, recommendation, status_id) VALUES
(1, 'Database Systems', 3, 'Introduction to database design and management', 'Data Structures', true, 'Good understanding of programming concepts', 1),
(1, 'Software Engineering', 4, 'Software development methodologies and practices', 'Object-Oriented Programming', true, 'Experience with programming projects', 1),
(1, 'Computer Networks', 3, 'Fundamentals of computer networking and protocols', 'Operating Systems', true, 'Basic knowledge of systems administration', 1),
(2, 'Information Security', 3, 'Cybersecurity principles and practices', 'Computer Networks', true, 'Understanding of network protocols', 1),
(2, 'Data Analytics', 3, 'Statistical analysis and data mining techniques', 'Statistics, Database Systems', true, 'Strong mathematical background', 1),
(3, 'Agile Development', 2, 'Agile methodologies and project management', 'Software Engineering', true, 'Team collaboration experience', 1);

-- Semesters from 2025 to 2030 --
INSERT INTO semester (period, year, is_current, start_date, end_date) VALUES
-- 2024
(30, 2024, false, '2024-08-15', '2024-12-15'),
-- 2025
(10, 2025, true, '2025-01-15', '2025-05-30');

-- Classrooms data --
INSERT INTO classroom (classroom_type_id, campus, location, room, capacity) VALUES
(1, 'Main Campus', 'Building A', 'A101', 40),
(1, 'Main Campus', 'Building A', 'A102', 35),
(1, 'Main Campus', 'Building B', 'B201', 50),
(2, 'Main Campus', 'Building C', 'C301', 25),
(2, 'Main Campus', 'Building C', 'C302', 30),
(2, 'Main Campus', 'Building D', 'D401', 20);

-- Classes data for current semester --
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id) VALUES
-- 2024
(1, 1, 1, 1, '2024-08-15', '2024-12-15', 'Database Systems - Fall 2024', 40, 1),
(2, 2, 1, 1, '2024-08-15', '2024-12-15', 'Software Engineering - Fall 2024', 35, 1),
(3, 3, 1, 1, '2024-08-15', '2024-12-15', 'Computer Networks - Fall 2024', 50, 1),
(4, 4, 1, 1, '2024-08-15', '2024-12-15', 'Information Security - Fall 2024', 25, 1),
(5, 5, 1, 1, '2024-08-15', '2024-12-15', 'Data Analytics - Fall 2024', 30, 1),
(6, 6, 1, 1, '2024-08-15', '2024-12-15', 'Agile Development - Fall 2024', 20, 1),
-- 2025
(7, 1, 1, 2, '2025-01-15', '2025-05-30', 'Database Systems - Spring 2025', 40, 1),
(8, 2, 2, 2, '2025-01-15', '2025-05-30', 'Software Engineering - Spring 2025', 35, 1),
(9, 3, 2, 2, '2025-01-15', '2025-05-30', 'Computer Networks - Spring 2025', 50, 1),
(10, 4, 2, 2, '2025-01-15', '2025-05-30', 'Information Security - Spring 2025', 25, 1),
(11, 5, 2, 2, '2025-01-15', '2025-05-30', 'Data Analytics - Spring 2025', 30, 1),
(12, 6, 2, 2, '2025-01-15', '2025-05-30', 'Agile Development - Spring 2025', 20, 1);

-- Class schedules (in pairs as requested) --
INSERT INTO class_schedule (class_id, classroom_id, day, start_time, end_time, modality_id, disability) VALUES
-- 2024
-- Database Systems (Monday-Wednesday)
(1, 1, 'Monday', '08:00:00', '10:00:00', 1, false),
(1, 1, 'Wednesday', '08:00:00', '10:00:00', 1, false),
-- Software Engineering (Tuesday-Thursday)
(2, 2, 'Tuesday', '10:00:00', '12:00:00', 1, false),
(2, 2, 'Thursday', '10:00:00', '12:00:00', 1, false),
-- Computer Networks (Monday-Friday)
(3, 3, 'Monday', '14:00:00', '16:00:00', 1, false),
(3, 3, 'Friday', '14:00:00', '16:00:00', 1, false),
-- Information Security (Tuesday-Thursday)
(4, 4, 'Tuesday', '16:00:00', '18:00:00', 1, false),
(4, 4, 'Thursday', '16:00:00', '18:00:00', 1, false),
-- Data Analytics (Wednesday-Friday)
(5, 5, 'Wednesday', '10:00:00', '12:00:00', 1, false),
(5, 5, 'Friday', '10:00:00', '12:00:00', 1, false),
-- Agile Development (Monday-Wednesday)
(6, 6, 'Monday', '16:00:00', '18:00:00', 1, false),
(6, 6, 'Wednesday', '16:00:00', '18:00:00', 1, false),
-- 2025
-- Database Systems (Monday-Wednesday)
(7, 1, 'Monday', '08:00:00', '10:00:00', 1, false),
(7, 1, 'Wednesday', '08:00:00', '10:00:00', 1, false),
-- Software Engineering (Tuesday-Thursday)
(8, 2, 'Tuesday', '10:00:00', '12:00:00', 1, false),
(8, 2, 'Thursday', '10:00:00', '12:00:00', 1, false),
-- Computer Networks (Monday-Friday)
(9, 3, 'Monday', '14:00:00', '16:00:00', 1, false),
(9, 3, 'Friday', '14:00:00', '16:00:00', 1, false),
-- Information Security (Tuesday-Thursday)
(10, 4, 'Tuesday', '16:00:00', '18:00:00', 1, false),
(10, 4, 'Thursday', '16:00:00', '18:00:00', 1, false),
-- Data Analytics (Wednesday-Friday)
(11, 5, 'Wednesday', '10:00:00', '12:00:00', 1, false),
(11, 5, 'Friday', '10:00:00', '12:00:00', 1, false),
-- Agile Development (Monday-Wednesday)
(12, 6, 'Monday', '16:00:00', '18:00:00', 1, false),
(12, 6, 'Wednesday', '16:00:00', '18:00:00', 1, false);

-- Student applications --
INSERT INTO student_application (user_id, course_id, section_id, semester_id, program, student_semester, academic_average, phone_number, alternate_phone_number, address, personal_email, was_teaching_assistant, course_average, course_teacher, application_date, status_id) VALUES
-- Past semester (Fall 2024) - these will be teaching assistants
(8, 1, NULL, 1, 'Computer Science', 6, 4.5, '555-0101', '555-0102', '123 Main St, Bogotá', 'ana.rod@gmail.com', false, 4.8, 'Dr. Smith', '2024-07-15', 8),
(9, NULL, 1, 1, 'Information Systems', 7, NULL, '555-0201', '555-0202', '456 Oak Ave, Medellín', 'luis.mar@gmail.com', true, NULL, NULL, '2024-07-20', 8),
-- Current semester (Spring 2025) - regular applications
(10, 2, NULL, 2, 'Software Engineering', 5, 4.7, '555-0301', '555-0302', '789 Pine St, Cali', 'sofia.her@gmail.com', false, 4.9, 'Dr. Williams', '2025-01-08', 4),
(3, NULL, 2, 2, 'Computer Science', 8, NULL, '555-0401', '555-0402', '321 Elm St, Barranquilla', 'student@secchub.com', false, NULL, NULL, '2025-01-10', 4);

-- Student application schedules (in pairs) --
INSERT INTO student_application_schedule (student_application_id, day, start_time, end_time) VALUES
-- Ana Rodriguez availability (past semester, section_id = null)
(1, 'Monday', '06:00:00', '08:00:00'),
(1, 'Wednesday', '06:00:00', '08:00:00'),
(1, 'Friday', '12:00:00', '14:00:00'),
(1, 'Friday', '18:00:00', '20:00:00'),
-- Luis Martinez availability (past semester, course_id/average/teacher = null)
(2, 'Tuesday', '08:00:00', '10:00:00'),
(2, 'Thursday', '08:00:00', '10:00:00'),
(2, 'Monday', '18:00:00', '20:00:00'),
(2, 'Wednesday', '18:00:00', '20:00:00'),
-- Sofia Hernandez availability (current semester)
(3, 'Monday', '12:00:00', '14:00:00'),
(3, 'Friday', '12:00:00', '14:00:00'),
(3, 'Tuesday', '18:00:00', '20:00:00'),
(3, 'Thursday', '18:00:00', '20:00:00'),
-- Student user availability (current semester)
(4, 'Wednesday', '14:00:00', '16:00:00'),
(4, 'Friday', '14:00:00', '16:00:00'),
(4, 'Monday', '20:00:00', '22:00:00'),
(4, 'Wednesday', '20:00:00', '22:00:00');

-- Teaching assistants (only for past semester applications) --
INSERT INTO teaching_assistant (class_id, student_application_id, weekly_hours, weeks, total_hours) VALUES
(1, 1, 10, 16, 160),  -- Ana Rodriguez for Database Systems (Fall 2024)
(2, 2, 15, 16, 240);  -- Luis Martinez for Software Engineering (Fall 2024)

-- Teaching assistant schedules (in pairs) --
INSERT INTO teaching_assistant_schedule (teaching_assistant_id, day, start_time, end_time) VALUES
-- Ana Rodriguez TA schedule
(1, 'Monday', '06:00:00', '08:00:00'),
(1, 'Wednesday', '06:00:00', '08:00:00'),
(1, 'Friday', '12:00:00', '14:00:00'),
(1, 'Friday', '18:00:00', '20:00:00'),
-- Luis Martinez TA schedule
(2, 'Tuesday', '08:00:00', '10:00:00'),
(2, 'Thursday', '08:00:00', '10:00:00'),
(2, 'Monday', '18:00:00', '20:00:00'),
(2, 'Wednesday', '18:00:00', '20:00:00');

-- Teacher class assignments (only for past semester - Fall 2024) --
INSERT INTO teacher_class (semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id) VALUES
(1, 1, 1, 8, 0, 0, true, 'Database Systems assignment - Fall 2024', 8),
(1, 2, 2, 10, 2, 0, true, 'Software Engineering assignment - Fall 2024', 8),
(1, 2, 4, 6, 0, 0, true, 'Information Security assignment - Fall 2024', 8),
(1, 3, 3, 8, 0, 4, true, 'Computer Networks assignment - Fall 2024', 8),
(1, 3, 5, 6, 0, 2, true, 'Data Analytics assignment - Fall 2024', 8),
(1, 1, 6, 4, 0, 0, true, 'Agile Development assignment - Fall 2024', 8);

-- Academic requests (for semester 2 - Spring 2025) --
INSERT INTO academic_request (user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation) VALUES
(4, 1, 2, '2025-08-15', '2025-12-15', 45, '2025-06-01', 'Request for Database Systems - Spring 2025'),
(6, 2, 2, '2025-08-15', '2025-12-15', 40, '2025-06-03', 'Request for Software Engineering - Spring 2025'),
(7, 4, 2, '2025-08-15', '2025-12-15', 30, '2025-06-05', 'Request for Information Security - Spring 2025');

-- Request schedules (in pairs) --
INSERT INTO request_schedule (academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability) VALUES
-- Database Systems request
(1, 1, '09:00:00', '11:00:00', 'Tuesday', 1, false),
(1, 1, '09:00:00', '11:00:00', 'Thursday', 1, false),
-- Software Engineering request
(2, 1, '14:00:00', '16:00:00', 'Monday', 1, false),
(2, 1, '14:00:00', '16:00:00', 'Wednesday', 1, false),
-- Information Security request
(3, 2, '16:00:00', '18:00:00', 'Tuesday', 1, false),
(3, 2, '16:00:00', '18:00:00', 'Friday', 1, false);

-- Academic requests (for past semester 1 - Fall 2024)
INSERT INTO academic_request (user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation) VALUES
(4, 1, 1, '2024-08-15', '2024-12-15', 40, '2024-06-01', 'Request for Database Systems - Fall 2024'),
(6, 2, 1, '2024-08-15', '2024-12-15', 35, '2024-06-03', 'Request for Software Engineering - Fall 2024'),
(7, 3, 1, '2024-08-15', '2024-12-15', 50, '2024-06-05', 'Request for Computer Networks - Fall 2024');

-- Request schedules for the Fall 2024 requests (paired entries)
INSERT INTO request_schedule (academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability) VALUES
-- Database Systems (Fall 2024) request -> will be academic_request id 4
(4, 1, '09:00:00', '11:00:00', 'Tuesday', 1, false),
(4, 1, '09:00:00', '11:00:00', 'Thursday', 1, false),
-- Software Engineering (Fall 2024) request -> id 5
(5, 1, '14:00:00', '16:00:00', 'Monday', 1, false),
(5, 1, '14:00:00', '16:00:00', 'Wednesday', 1, false),
-- Computer Networks (Fall 2024) request -> id 6
(6, 1, '14:00:00', '16:00:00', 'Monday', 1, false),
(6, 1, '14:00:00', '16:00:00', 'Friday', 1, false);