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
('user-si', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Regular', 'User', 'user-si@secchub.com', 1, 2, 1, '87654321'),
('dr.silva', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Roberto', 'Silva', 'dr.silva@secchub.com', 1, 4, 1, '99999999'),
('prof.torres', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Elena', 'Torres', 'prof.torres@secchub.com', 1, 4, 1, '10101010'),
('dr.morales', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Diego', 'Morales', 'dr.morales@secchub.com', 1, 4, 1, '20202020'),
('prof.castro', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Patricia', 'Castro', 'prof.castro@secchub.com', 1, 4, 1, '30303030'),
('dr.vargas', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Fernando', 'Vargas', 'dr.vargas@secchub.com', 1, 4, 1, '40404040'),
('juan.perez', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Juan', 'Perez', 'juan.perez@secchub.com', 1, 3, 1, '50505050'),
('laura.jimenez', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Laura', 'Jimenez', 'laura.jimenez@secchub.com', 1, 3, 1, '60606060'),
('diego.ramirez', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Diego', 'Ramirez', 'diego.ramirez@secchub.com', 1, 3, 1, '70707070'),
('camila.santos', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Camila', 'Santos', 'camila.santos@secchub.com', 1, 3, 1, '80808080'),
('andres.flores', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Andres', 'Flores', 'andres.flores@secchub.com', 1, 3, 1, '90909090'),
('valentina.cruz', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Valentina', 'Cruz', 'valentina.cruz@secchub.com', 1, 3, 1, '11223344'),
('coord.cs', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Carlos', 'Mendez', 'coord.cs@secchub.com', 1, 5, 1, '55443322'),
('coord.is', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Lucia', 'Gonzalez', 'coord.is@secchub.com', 1, 5, 1, '66554433');

-- Teachers data --
INSERT INTO teacher (user_id, employment_type_id, max_hours) VALUES
(4, 1, 40),  -- teacher user
(6, 1, 40),  -- maria.garcia
(7, 2, 20),  -- carlos.lopez
(13, 1, 40), -- dr.silva
(14, 1, 40), -- prof.torres
(15, 1, 40), -- dr.morales
(16, 2, 20), -- prof.castro
(17, 1, 40); -- dr.vargas

-- Sections data --
INSERT INTO section (user_id, name) VALUES
(2, 'Computer Science'),
(11, 'Information Systems'),
(12, 'Software Engineering');

-- Courses data --
INSERT INTO course (section_id, name, credits, description, requirement, is_valid, recommendation, status_id) VALUES
-- Computer Science Section
(1, 'Database Systems', 3, 'Introduction to database design and management', 'Data Structures', true, 'Good understanding of programming concepts', 1),
(1, 'Software Engineering', 4, 'Software development methodologies and practices', 'Object-Oriented Programming', true, 'Experience with programming projects', 1),
(1, 'Computer Networks', 3, 'Fundamentals of computer networking and protocols', 'Operating Systems', true, 'Basic knowledge of systems administration', 1),
(1, 'Machine Learning', 4, 'Introduction to ML algorithms and applications', 'Statistics, Linear Algebra', true, 'Strong mathematical background', 1),
(1, 'Computer Graphics', 3, 'Fundamentals of 2D and 3D graphics programming', 'Linear Algebra, Programming', true, 'Good programming skills', 1),
(1, 'Algorithms and Data Structures', 4, 'Advanced algorithms and data structure analysis', 'Discrete Mathematics', true, 'Strong problem-solving skills', 1),
(1, 'Operating Systems', 3, 'OS concepts, processes, memory management', 'Computer Architecture', true, 'Understanding of low-level programming', 1),
(1, 'Web Development', 3, 'Full-stack web application development', 'Database Systems', true, 'HTML, CSS, JavaScript knowledge', 1),
-- Information Systems Section
(2, 'Information Security', 3, 'Cybersecurity principles and practices', 'Computer Networks', true, 'Understanding of network protocols', 1),
(2, 'Data Analytics', 3, 'Statistical analysis and data mining techniques', 'Statistics, Database Systems', true, 'Strong mathematical background', 1),
(2, 'Business Intelligence', 3, 'BI tools and business data analysis', 'Database Systems, Statistics', true, 'Business process understanding', 1),
(2, 'Systems Analysis', 3, 'Requirements analysis and system design', 'Software Engineering', true, 'Communication skills', 1),
(2, 'Enterprise Architecture', 4, 'Enterprise system design and integration', 'Systems Analysis', true, 'Business and technical knowledge', 1),
(2, 'IT Project Management', 3, 'Project management methodologies for IT', 'Software Engineering', true, 'Leadership and organization skills', 1),
-- Software Engineering Section
(3, 'Agile Development', 2, 'Agile methodologies and project management', 'Software Engineering', true, 'Team collaboration experience', 1),
(3, 'Software Architecture', 4, 'Design patterns and architectural principles', 'Software Engineering', true, 'Advanced programming experience', 1),
(3, 'Mobile Development', 3, 'Android and iOS application development', 'Object-Oriented Programming', true, 'Mobile platform knowledge', 1),
(3, 'DevOps and CI/CD', 3, 'Continuous integration and deployment practices', 'Software Engineering', true, 'Version control and automation', 1),
(3, 'Software Testing', 3, 'Testing methodologies and quality assurance', 'Software Engineering', true, 'Attention to detail', 1),
(3, 'Cloud Computing', 3, 'Cloud platforms and distributed systems', 'Computer Networks', true, 'Understanding of distributed systems', 1);

-- Semesters from 2024 to 2025 --
INSERT INTO semester (period, year, is_current, start_date, end_date) VALUES
-- 2024
(30, 2024, false, '2024-08-15', '2024-12-15'),
-- 2025
(10, 2025, true, '2025-01-15', '2025-05-30');

-- Classrooms data --
INSERT INTO classroom (classroom_type_id, campus, location, room, capacity) VALUES
-- Regular classrooms
(1, 'Main Campus', 'Building A', 'A101', 40),
(1, 'Main Campus', 'Building A', 'A102', 35),
(1, 'Main Campus', 'Building A', 'A103', 45),
(1, 'Main Campus', 'Building A', 'A104', 50),
(1, 'Main Campus', 'Building B', 'B201', 40),
(1, 'Main Campus', 'Building B', 'B202', 35),
(1, 'Main Campus', 'Building B', 'B203', 45),
-- Computer labs
(2, 'Main Campus', 'Building C', 'C301', 25),
(2, 'Main Campus', 'Building C', 'C302', 30),
(2, 'Main Campus', 'Building C', 'C303', 28),
(2, 'Main Campus', 'Building D', 'D401', 20),
(2, 'Main Campus', 'Building D', 'D402', 25),
-- Additional specialized rooms
(1, 'North Campus', 'Building E', 'E501', 60),
(2, 'North Campus', 'Building E', 'E502', 35),
(1, 'South Campus', 'Building F', 'F601', 55),
(1, 'Main Campus', 'Building G', 'G701', 30);

-- Classes data for current semester --
INSERT INTO class (id, section, course_id, semester_id, start_date, end_date, observation, capacity, status_id) VALUES
-- 2024 semester (previous)
(1, 1, 1, 1, '2024-08-15', '2024-12-15', 'Database Systems - Fall 2024', 40, 1),
(2, 2, 1, 1, '2024-08-15', '2024-12-15', 'Software Engineering - Fall 2024', 35, 1),
(3, 3, 1, 1, '2024-08-15', '2024-12-15', 'Computer Networks - Fall 2024', 50, 1),
(4, 4, 1, 1, '2024-08-15', '2024-12-15', 'Information Security - Fall 2024', 25, 1),
(5, 5, 1, 1, '2024-08-15', '2024-12-15', 'Data Analytics - Fall 2024', 30, 1),
(6, 6, 1, 1, '2024-08-15', '2024-12-15', 'Agile Development - Fall 2024', 20, 1),

-- 2025 current semester - extensive class offerings
-- Computer Science Section (section_id = 1)
(7, 1, 1, 2, '2025-01-15', '2025-05-30', 'Database Systems - Spring 2025 - Group 1', 40, 1),
(8, 2, 1, 2, '2025-01-15', '2025-05-30', 'Database Systems - Spring 2025 - Group 2', 35, 1),
(9, 1, 2, 2, '2025-01-15', '2025-05-30', 'Software Engineering - Spring 2025 - Group 1', 45, 1),
(10, 2, 2, 2, '2025-01-15', '2025-05-30', 'Software Engineering - Spring 2025 - Group 2', 40, 1),
(11, 1, 3, 2, '2025-01-15', '2025-05-30', 'Computer Networks - Spring 2025', 50, 1),
(12, 1, 4, 2, '2025-01-15', '2025-05-30', 'Machine Learning - Spring 2025', 35, 1),
(13, 1, 5, 2, '2025-01-15', '2025-05-30', 'Computer Graphics - Spring 2025', 30, 1),
(14, 1, 6, 2, '2025-01-15', '2025-05-30', 'Algorithms and Data Structures - Spring 2025', 40, 1),
(15, 1, 7, 2, '2025-01-15', '2025-05-30', 'Operating Systems - Spring 2025', 35, 1),
(16, 1, 8, 2, '2025-01-15', '2025-05-30', 'Web Development - Spring 2025', 40, 1),

-- Information Systems Section (section_id = 2) 
(17, 1, 9, 2, '2025-01-15', '2025-05-30', 'Information Security - Spring 2025', 30, 1),
(18, 2, 9, 2, '2025-01-15', '2025-05-30', 'Information Security - Spring 2025 - Group 2', 25, 1),
(19, 1, 10, 2, '2025-01-15', '2025-05-30', 'Data Analytics - Spring 2025', 35, 1),
(20, 1, 11, 2, '2025-01-15', '2025-05-30', 'Business Intelligence - Spring 2025', 30, 1),
(21, 1, 12, 2, '2025-01-15', '2025-05-30', 'Systems Analysis - Spring 2025', 35, 1),
(22, 1, 13, 2, '2025-01-15', '2025-05-30', 'Enterprise Architecture - Spring 2025', 25, 1),
(23, 1, 14, 2, '2025-01-15', '2025-05-30', 'IT Project Management - Spring 2025', 30, 1),

-- Software Engineering Section (section_id = 3)
(24, 1, 15, 2, '2025-01-15', '2025-05-30', 'Agile Development - Spring 2025', 25, 1),
(25, 1, 16, 2, '2025-01-15', '2025-05-30', 'Software Architecture - Spring 2025', 30, 1),
(26, 1, 17, 2, '2025-01-15', '2025-05-30', 'Mobile Development - Spring 2025', 35, 1),
(27, 1, 18, 2, '2025-01-15', '2025-05-30', 'DevOps and CI/CD - Spring 2025', 30, 1),
(28, 1, 19, 2, '2025-01-15', '2025-05-30', 'Software Testing - Spring 2025', 35, 1),
(29, 1, 20, 2, '2025-01-15', '2025-05-30', 'Cloud Computing - Spring 2025', 30, 1);

-- Class schedules (in pairs as requested) --
INSERT INTO class_schedule (class_id, classroom_id, day, start_time, end_time, modality_id, disability) VALUES
-- 2024 semester schedules (unchanged)
-- Database Systems (Lunes-Miercoles)
(1, 1, 'Lunes', '08:00:00', '10:00:00', 1, false),
(1, 1, 'Miercoles', '08:00:00', '10:00:00', 1, false),
-- Software Engineering (Martes-Jueves)
(2, 2, 'Martes', '10:00:00', '12:00:00', 1, false),
(2, 2, 'Jueves', '10:00:00', '12:00:00', 1, false),
-- Computer Networks (Lunes-Viernes)
(3, 3, 'Lunes', '14:00:00', '16:00:00', 1, false),
(3, 3, 'Viernes', '14:00:00', '16:00:00', 1, false),
-- Information Security (Martes-Jueves)
(4, 4, 'Martes', '16:00:00', '18:00:00', 1, false),
(4, 4, 'Jueves', '16:00:00', '18:00:00', 1, false),
-- Data Analytics (Miercoles-Viernes)
(5, 5, 'Miercoles', '10:00:00', '12:00:00', 1, false),
(5, 5, 'Viernes', '10:00:00', '12:00:00', 1, false),
-- Agile Development (Lunes-Miercoles)
(6, 6, 'Lunes', '16:00:00', '18:00:00', 1, false),
(6, 6, 'Miercoles', '16:00:00', '18:00:00', 1, false),

-- 2025 current semester schedules (comprehensive)
-- Computer Science Classes
-- Database Systems Group 1 (Lunes-Miercoles)
(7, 1, 'Lunes', '08:00:00', '10:00:00', 1, false),
(7, 1, 'Miercoles', '08:00:00', '10:00:00', 1, false),
-- Database Systems Group 2 (Martes-Jueves)
(8, 2, 'Martes', '08:00:00', '10:00:00', 1, false),
(8, 2, 'Jueves', '08:00:00', '10:00:00', 1, false),
-- Software Engineering Group 1 (Lunes-Viernes)
(9, 3, 'Lunes', '10:00:00', '12:00:00', 1, false),
(9, 3, 'Viernes', '10:00:00', '12:00:00', 1, false),
-- Software Engineering Group 2 (Martes-Jueves)
(10, 4, 'Martes', '10:00:00', '12:00:00', 1, false),
(10, 4, 'Jueves', '10:00:00', '12:00:00', 1, false),
-- Computer Networks (Lunes-Miercoles)
(11, 5, 'Lunes', '14:00:00', '16:00:00', 1, false),
(11, 5, 'Miercoles', '14:00:00', '16:00:00', 1, false),
-- Machine Learning (Martes-Jueves)
(12, 8, 'Martes', '14:00:00', '16:00:00', 1, false),
(12, 8, 'Jueves', '14:00:00', '16:00:00', 1, false),
-- Computer Graphics (Miercoles-Viernes)
(13, 9, 'Miercoles', '16:00:00', '18:00:00', 1, false),
(13, 9, 'Viernes', '16:00:00', '18:00:00', 1, false),
-- Algorithms and Data Structures (Lunes-Viernes)
(14, 6, 'Lunes', '08:00:00', '10:00:00', 1, false),
(14, 6, 'Viernes', '08:00:00', '10:00:00', 1, false),
-- Operating Systems (Martes-Jueves)
(15, 7, 'Martes', '16:00:00', '18:00:00', 1, false),
(15, 7, 'Jueves', '16:00:00', '18:00:00', 1, false),
-- Web Development (Miercoles-Viernes)
(16, 10, 'Miercoles', '08:00:00', '10:00:00', 1, false),
(16, 10, 'Viernes', '08:00:00', '10:00:00', 1, false),

-- Information Systems Classes
-- Information Security Group 1 (Lunes-Miercoles)
(17, 11, 'Lunes', '16:00:00', '18:00:00', 1, false),
(17, 11, 'Miercoles', '16:00:00', '18:00:00', 1, false),
-- Information Security Group 2 (Martes-Jueves)
(18, 12, 'Martes', '18:00:00', '20:00:00', 1, false),
(18, 12, 'Jueves', '18:00:00', '20:00:00', 1, false),
-- Data Analytics (Lunes-Viernes)
(19, 13, 'Lunes', '18:00:00', '20:00:00', 1, false),
(19, 13, 'Viernes', '18:00:00', '20:00:00', 1, false),
-- Business Intelligence (Martes-Jueves)
(20, 14, 'Martes', '12:00:00', '14:00:00', 1, false),
(20, 14, 'Jueves', '12:00:00', '14:00:00', 1, false),
-- Systems Analysis (Miercoles-Viernes)
(21, 15, 'Miercoles', '12:00:00', '14:00:00', 1, false),
(21, 15, 'Viernes', '12:00:00', '14:00:00', 1, false),
-- Enterprise Architecture (Lunes-Miercoles)
(22, 16, 'Lunes', '12:00:00', '14:00:00', 1, false),
(22, 16, 'Miercoles', '12:00:00', '14:00:00', 1, false),
-- IT Project Management (Martes-Jueves)
(23, 1, 'Martes', '20:00:00', '22:00:00', 1, false),
(23, 1, 'Jueves', '20:00:00', '22:00:00', 1, false),

-- Software Engineering Classes
-- Agile Development (Lunes-Viernes)
(24, 2, 'Lunes', '20:00:00', '22:00:00', 1, false),
(24, 2, 'Viernes', '20:00:00', '22:00:00', 1, false),
-- Software Architecture (Martes-Jueves)
(25, 3, 'Martes', '06:00:00', '08:00:00', 1, false),
(25, 3, 'Jueves', '06:00:00', '08:00:00', 1, false),
-- Mobile Development (Miercoles-Viernes)
(26, 8, 'Miercoles', '18:00:00', '20:00:00', 1, false),
(26, 8, 'Viernes', '18:00:00', '20:00:00', 1, false),
-- DevOps and CI/CD (Lunes-Miercoles)
(27, 9, 'Lunes', '06:00:00', '08:00:00', 1, false),
(27, 9, 'Miercoles', '06:00:00', '08:00:00', 1, false),
-- Software Testing (Martes-Jueves)
(28, 10, 'Martes', '20:00:00', '22:00:00', 1, false),
(28, 10, 'Jueves', '20:00:00', '22:00:00', 1, false),
-- Cloud Computing (Lunes-Viernes)
(29, 11, 'Lunes', '22:00:00', '24:00:00', 1, false),
(29, 11, 'Viernes', '22:00:00', '24:00:00', 1, false);

-- Student applications --
INSERT INTO student_application (id, user_id, course_id, section_id, semester_id, program, student_semester, academic_average, phone_number, alternate_phone_number, address, personal_email, was_teaching_assistant, course_average, course_teacher, application_date, status_id) VALUES
-- Past semester (Fall 2024) - these are already teaching assistants (IDs 1-2)
(1, 8, 1, NULL, 1, 'Computer Science', 6, 4.5, '555-0101', '555-0102', '123 Main St, Bogotá', 'ana.rod@gmail.com', false, 4.8, 'Dr. Smith', '2024-07-15', 8),
(2, 9, NULL, 1, 1, 'Information Systems', 7, NULL, '555-0201', '555-0202', '456 Oak Ave, Medellín', 'luis.mar@gmail.com', true, NULL, NULL, '2024-07-20', 8),

-- Current semester (Spring 2025) applications - comprehensive list (IDs 3-20)
-- Applications for Computer Science courses (IDs 3-8)
(3, 10, 1, NULL, 2, 'Computer Science', 5, 4.7, '555-0301', '555-0302', '789 Pine St, Cali', 'sofia.her@gmail.com', false, 4.9, 'Dr. Williams', '2025-01-08', 4), -- Database Systems
(4, 18, 2, NULL, 2, 'Computer Science', 6, 4.3, '555-1001', '555-1002', '100 Tech Ave, Bogotá', 'juan.perez@gmail.com', false, 4.5, 'Dr. Johnson', '2025-01-09', 4), -- Software Engineering
(5, 19, 4, NULL, 2, 'Computer Science', 7, 4.6, '555-2001', '555-2002', '200 Code St, Medellín', 'laura.jimenez@gmail.com', true, 4.8, 'Dr. Brown', '2025-01-10', 4), -- Machine Learning
(6, 20, 6, NULL, 2, 'Computer Science', 8, 4.4, '555-3001', '555-3002', '300 Data Rd, Cali', 'diego.ramirez@gmail.com', false, 4.6, 'Dr. Davis', '2025-01-11', 4), -- Algorithms
(7, 21, 7, NULL, 2, 'Computer Science', 6, 4.5, '555-4001', '555-4002', '400 System Blvd, Barranquilla', 'camila.santos@gmail.com', false, 4.7, 'Dr. Wilson', '2025-01-12', 4), -- Operating Systems
(8, 22, 8, NULL, 2, 'Computer Science', 5, 4.2, '555-5001', '555-5002', '500 Web Way, Cartagena', 'andres.flores@gmail.com', false, 4.4, 'Dr. Miller', '2025-01-13', 4), -- Web Development

-- Applications for Information Systems courses (IDs 9-12)
(9, 23, 9, NULL, 2, 'Information Systems', 6, 4.8, '555-6001', '555-6002', '600 Security St, Bogotá', 'valentina.cruz@gmail.com', true, 4.9, 'Dr. Taylor', '2025-01-14', 4), -- Information Security
(10, 3, 10, NULL, 2, 'Information Systems', 7, 4.5, '555-0401', '555-0402', '321 Elm St, Barranquilla', 'student@secchub.com', false, 4.7, 'Dr. Anderson', '2025-01-15', 4), -- Data Analytics
(11, 8, 11, NULL, 2, 'Information Systems', 8, 4.6, '555-0101', '555-0102', '123 Main St, Bogotá', 'ana.rod@gmail.com', true, 4.8, 'Dr. Thomas', '2025-01-16', 4), -- Business Intelligence
(12, 18, 12, NULL, 2, 'Information Systems', 6, 4.3, '555-1001', '555-1002', '100 Tech Ave, Bogotá', 'juan.perez@gmail.com', false, 4.5, 'Dr. Garcia', '2025-01-17', 4), -- Systems Analysis

-- Applications for Software Engineering courses (IDs 13-16)
(13, 19, 15, NULL, 2, 'Software Engineering', 5, 4.7, '555-2001', '555-2002', '200 Code St, Medellín', 'laura.jimenez@gmail.com', false, 4.8, 'Dr. Martinez', '2025-01-18', 4), -- Agile Development
(14, 20, 16, NULL, 2, 'Software Engineering', 7, 4.4, '555-3001', '555-3002', '300 Data Rd, Cali', 'diego.ramirez@gmail.com', true, 4.6, 'Dr. Rodriguez', '2025-01-19', 4), -- Software Architecture
(15, 21, 17, NULL, 2, 'Software Engineering', 6, 4.5, '555-4001', '555-4002', '400 System Blvd, Barranquilla', 'camila.santos@gmail.com', false, 4.7, 'Dr. Lopez', '2025-01-20', 4), -- Mobile Development
(16, 22, 18, NULL, 2, 'Software Engineering', 8, 4.2, '555-5001', '555-5002', '500 Web Way, Cartagena', 'andres.flores@gmail.com', false, 4.4, 'Dr. Hernandez', '2025-01-21', 4), -- DevOps

-- Section-based applications (no specific course) (IDs 17-19)
(17, 9, NULL, 1, 2, 'Computer Science', 8, NULL, '555-0201', '555-0202', '456 Oak Ave, Medellín', 'luis.mar@gmail.com', true, NULL, NULL, '2025-01-22', 4), -- CS Section
(18, 23, NULL, 2, 2, 'Information Systems', 7, NULL, '555-6001', '555-6002', '600 Security St, Bogotá', 'valentina.cruz@gmail.com', false, NULL, NULL, '2025-01-23', 4), -- IS Section
(19, 10, NULL, 3, 2, 'Software Engineering', 6, NULL, '555-0301', '555-0302', '789 Pine St, Cali', 'sofia.her@gmail.com', true, NULL, NULL, '2025-01-24', 4); -- SE Section

-- Student application schedules (in pairs) --
INSERT INTO student_application_schedule (student_application_id, day, start_time, end_time) VALUES
-- Past semester schedules (already existing)
-- Ana Rodriguez availability (past semester, section_id = null)
(1, 'Lunes', '06:00:00', '08:00:00'),
(1, 'Miercoles', '06:00:00', '08:00:00'),
(1, 'Viernes', '12:00:00', '14:00:00'),
(1, 'Viernes', '18:00:00', '20:00:00'),
-- Luis Martinez availability (past semester, course_id/average/teacher = null)
(2, 'Martes', '08:00:00', '10:00:00'),
(2, 'Jueves', '08:00:00', '10:00:00'),
(2, 'Lunes', '18:00:00', '20:00:00'),
(2, 'Miercoles', '18:00:00', '20:00:00'),

-- Current semester (Spring 2025) student application schedules
-- Sofia Hernandez availability (Database Systems)
(3, 'Lunes', '12:00:00', '14:00:00'),
(3, 'Viernes', '12:00:00', '14:00:00'),
(3, 'Martes', '18:00:00', '20:00:00'),
(3, 'Jueves', '18:00:00', '20:00:00'),

-- Juan Perez availability (Software Engineering)
(4, 'Miercoles', '14:00:00', '16:00:00'),
(4, 'Viernes', '14:00:00', '16:00:00'),
(4, 'Lunes', '20:00:00', '22:00:00'),
(4, 'Miercoles', '20:00:00', '22:00:00'),

-- Laura Jimenez availability (Machine Learning)
(5, 'Martes', '06:00:00', '08:00:00'),
(5, 'Jueves', '06:00:00', '08:00:00'),
(5, 'Lunes', '14:00:00', '16:00:00'),
(5, 'Miercoles', '14:00:00', '16:00:00'),

-- Diego Ramirez availability (Algorithms)
(6, 'Lunes', '22:00:00', '24:00:00'),
(6, 'Viernes', '22:00:00', '24:00:00'),
(6, 'Martes', '12:00:00', '14:00:00'),
(6, 'Jueves', '12:00:00', '14:00:00'),

-- Camila Santos availability (Operating Systems)
(7, 'Miercoles', '20:00:00', '22:00:00'),
(7, 'Viernes', '20:00:00', '22:00:00'),
(7, 'Lunes', '08:00:00', '10:00:00'),
(7, 'Miercoles', '08:00:00', '10:00:00'),

-- Andres Flores availability (Web Development)
(8, 'Martes', '14:00:00', '16:00:00'),
(8, 'Jueves', '14:00:00', '16:00:00'),
(8, 'Lunes', '16:00:00', '18:00:00'),
(8, 'Miercoles', '16:00:00', '18:00:00'),

-- Valentina Cruz availability (Information Security)
(9, 'Lunes', '10:00:00', '12:00:00'),
(9, 'Viernes', '10:00:00', '12:00:00'),
(9, 'Martes', '20:00:00', '22:00:00'),
(9, 'Jueves', '20:00:00', '22:00:00'),

-- Student user availability (Data Analytics)
(10, 'Miercoles', '22:00:00', '24:00:00'),
(10, 'Viernes', '22:00:00', '24:00:00'),
(10, 'Lunes', '06:00:00', '08:00:00'),
(10, 'Miercoles', '06:00:00', '08:00:00'),

-- Ana Rodriguez availability (Business Intelligence - reapplying)
(11, 'Martes', '10:00:00', '12:00:00'),
(11, 'Jueves', '10:00:00', '12:00:00'),
(11, 'Lunes', '12:00:00', '14:00:00'),
(11, 'Miercoles', '12:00:00', '14:00:00'),

-- Juan Perez availability (Systems Analysis)
(12, 'Lunes', '18:00:00', '20:00:00'),
(12, 'Viernes', '18:00:00', '20:00:00'),
(12, 'Martes', '16:00:00', '18:00:00'),
(12, 'Jueves', '16:00:00', '18:00:00'),

-- Software Engineering applications schedules
-- Laura Jimenez (Agile Development)
(13, 'Miercoles', '18:00:00', '20:00:00'),
(13, 'Viernes', '18:00:00', '20:00:00'),
(13, 'Lunes', '22:00:00', '24:00:00'),
(13, 'Miercoles', '22:00:00', '24:00:00'),

-- Diego Ramirez (Software Architecture)
(14, 'Martes', '06:00:00', '08:00:00'),
(14, 'Jueves', '06:00:00', '08:00:00'),
(14, 'Lunes', '10:00:00', '12:00:00'),
(14, 'Viernes', '10:00:00', '12:00:00'),

-- Section-based applications schedules
-- Luis Martinez (CS Section)
(18, 'Lunes', '06:00:00', '08:00:00'),
(18, 'Miercoles', '06:00:00', '08:00:00'),
(18, 'Martes', '22:00:00', '24:00:00'),
(18, 'Jueves', '22:00:00', '24:00:00'),

-- Valentina Cruz (IS Section)
(19, 'Martes', '08:00:00', '10:00:00'),
(19, 'Jueves', '08:00:00', '10:00:00'),
(19, 'Lunes', '14:00:00', '16:00:00'),
(19, 'Viernes', '14:00:00', '16:00:00'),

-- Sofia Hernandez (SE Section)
(20, 'Miercoles', '10:00:00', '12:00:00'),
(20, 'Viernes', '10:00:00', '12:00:00'),
(20, 'Martes', '18:00:00', '20:00:00'),
(20, 'Jueves', '18:00:00', '20:00:00');

-- Teaching assistants --
INSERT INTO teaching_assistant (id, class_id, student_application_id, weekly_hours, weeks, total_hours) VALUES
-- Past semester (Fall 2024) - already established TAs (IDs 1-2)
(1, 1, 1, 10, 16, 160),  -- Ana Rodriguez for Database Systems (Fall 2024)
(2, 2, 2, 15, 16, 240),  -- Luis Martinez for Software Engineering (Fall 2024)

-- Current semester (Spring 2025) - new TA assignments (IDs 3-10)
(3, 7, 3, 12, 16, 192),  -- Sofia Hernandez for Database Systems Group 1
(4, 9, 4, 15, 16, 240),  -- Juan Perez for Software Engineering Group 1
(5, 12, 5, 10, 16, 160), -- Laura Jimenez for Machine Learning
(6, 14, 6, 12, 16, 192), -- Diego Ramirez for Algorithms and Data Structures
(7, 17, 9, 8, 16, 128),  -- Valentina Cruz for Information Security Group 1
(8, 19, 10, 10, 16, 160),-- Student user for Data Analytics
(9, 24, 13, 10, 16, 160),-- Laura Jimenez for Agile Development
(10, 26, 15, 8, 16, 128); -- Camila Santos for Mobile Development

-- Teaching assistant schedules (in pairs) --
INSERT INTO teaching_assistant_schedule (id, teaching_assistant_id, day, start_time, end_time) VALUES
-- Past semester schedules
-- Ana Rodriguez TA schedule (Fall 2024) (IDs 1-4)
(1, 1, 'Lunes', '06:00:00', '08:00:00'),
(2, 1, 'Miercoles', '06:00:00', '08:00:00'),
(3, 1, 'Viernes', '12:00:00', '14:00:00'),
(4, 1, 'Viernes', '18:00:00', '20:00:00'),
-- Luis Martinez TA schedule (Fall 2024) (IDs 5-8)
(5, 2, 'Martes', '08:00:00', '10:00:00'),
(6, 2, 'Jueves', '08:00:00', '10:00:00'),
(7, 2, 'Lunes', '18:00:00', '20:00:00'),
(8, 2, 'Miercoles', '18:00:00', '20:00:00'),

-- Current semester TA schedules (Spring 2025)
-- Sofia Hernandez TA schedule (Database Systems Group 1) (IDs 9-12)
(9, 3, 'Lunes', '12:00:00', '14:00:00'),
(10, 3, 'Viernes', '12:00:00', '14:00:00'),
(11, 3, 'Martes', '18:00:00', '20:00:00'),
(12, 3, 'Jueves', '18:00:00', '20:00:00'),

-- Juan Perez TA schedule (Software Engineering Group 1) (IDs 13-16)
(13, 4, 'Miercoles', '14:00:00', '16:00:00'),
(14, 4, 'Viernes', '14:00:00', '16:00:00'),
(15, 4, 'Lunes', '20:00:00', '22:00:00'),
(16, 4, 'Miercoles', '20:00:00', '22:00:00'),

-- Laura Jimenez TA schedule (Machine Learning) (IDs 17-20)
(17, 5, 'Martes', '06:00:00', '08:00:00'),
(18, 5, 'Jueves', '06:00:00', '08:00:00'),
(19, 5, 'Lunes', '14:00:00', '16:00:00'),
(20, 5, 'Miercoles', '14:00:00', '16:00:00'),

-- Diego Ramirez TA schedule (Algorithms and Data Structures) (IDs 21-24)
(21, 6, 'Lunes', '22:00:00', '24:00:00'),
(22, 6, 'Viernes', '22:00:00', '24:00:00'),
(23, 6, 'Martes', '12:00:00', '14:00:00'),
(24, 6, 'Jueves', '12:00:00', '14:00:00'),

-- Valentina Cruz TA schedule (Information Security Group 1) (IDs 25-28)
(25, 7, 'Lunes', '10:00:00', '12:00:00'),
(26, 7, 'Viernes', '10:00:00', '12:00:00'),
(27, 7, 'Martes', '20:00:00', '22:00:00'),
(28, 7, 'Jueves', '20:00:00', '22:00:00'),

-- Student user TA schedule (Data Analytics) (IDs 29-32)
(29, 8, 'Miercoles', '22:00:00', '24:00:00'),
(30, 8, 'Viernes', '22:00:00', '24:00:00'),
(31, 8, 'Lunes', '06:00:00', '08:00:00'),
(32, 8, 'Miercoles', '06:00:00', '08:00:00'),

-- Laura Jimenez TA schedule (Agile Development) (IDs 33-36)
(33, 9, 'Miercoles', '18:00:00', '20:00:00'),
(34, 9, 'Viernes', '18:00:00', '20:00:00'),
(35, 9, 'Lunes', '22:00:00', '24:00:00'),
(36, 9, 'Miercoles', '22:00:00', '24:00:00'),

-- Camila Santos TA schedule (Mobile Development) (IDs 37-40)
(37, 10, 'Miercoles', '20:00:00', '22:00:00'),
(38, 10, 'Viernes', '20:00:00', '22:00:00'),
(39, 10, 'Lunes', '08:00:00', '10:00:00'),
(40, 10, 'Miercoles', '08:00:00', '10:00:00');

-- Teacher class assignments --
INSERT INTO teacher_class (id, semester_id, teacher_id, class_id, work_hours, full_time_extra_hours, adjunct_extra_hours, decision, observation, status_id) VALUES
-- Past semester (Fall 2024) assignments (IDs 1-6)
(1, 1, 1, 1, 8, 0, 0, true, 'Database Systems assignment - Fall 2024', 8),
(2, 1, 2, 2, 10, 2, 0, true, 'Software Engineering assignment - Fall 2024', 8),
(3, 1, 2, 4, 6, 0, 0, true, 'Information Security assignment - Fall 2024', 8),
(4, 1, 3, 3, 8, 0, 4, true, 'Computer Networks assignment - Fall 2024', 8),
(5, 1, 3, 5, 6, 0, 2, true, 'Data Analytics assignment - Fall 2024', 8),
(6, 1, 1, 6, 4, 0, 0, true, 'Agile Development assignment - Fall 2024', 8),

-- Current semester (Spring 2025) assignments - comprehensive teaching load (IDs 7-29)
-- Computer Science classes
(7, 2, 1, 7, 8, 0, 0, true, 'Database Systems Group 1 - Spring 2025', 4), -- teacher user
(8, 2, 4, 8, 8, 0, 0, true, 'Database Systems Group 2 - Spring 2025', 4), -- dr.silva
(9, 2, 2, 9, 10, 2, 0, true, 'Software Engineering Group 1 - Spring 2025', 4), -- maria.garcia
(10, 2, 5, 10, 10, 2, 0, true, 'Software Engineering Group 2 - Spring 2025', 4), -- prof.torres
(11, 2, 6, 11, 8, 0, 0, true, 'Computer Networks - Spring 2025', 4), -- dr.morales
(12, 2, 4, 12, 10, 2, 0, true, 'Machine Learning - Spring 2025', 4), -- dr.silva
(13, 2, 7, 13, 6, 0, 2, true, 'Computer Graphics - Spring 2025', 4), -- prof.castro
(14, 2, 8, 14, 10, 2, 0, true, 'Algorithms and Data Structures - Spring 2025', 4), -- dr.vargas
(15, 2, 6, 15, 8, 0, 0, true, 'Operating Systems - Spring 2025', 4), -- dr.morales
(16, 2, 5, 16, 8, 0, 0, true, 'Web Development - Spring 2025', 4), -- prof.torres

-- Information Systems classes
(17, 2, 1, 17, 6, 0, 0, true, 'Information Security Group 1 - Spring 2025', 4), -- teacher user
(18, 2, 2, 18, 6, 0, 0, true, 'Information Security Group 2 - Spring 2025', 4), -- maria.garcia
(19, 2, 3, 19, 8, 0, 4, true, 'Data Analytics - Spring 2025', 4), -- carlos.lopez
(20, 2, 4, 20, 8, 0, 0, true, 'Business Intelligence - Spring 2025', 4), -- dr.silva
(21, 2, 5, 21, 8, 0, 0, true, 'Systems Analysis - Spring 2025', 4), -- prof.torres
(22, 2, 6, 22, 10, 2, 0, true, 'Enterprise Architecture - Spring 2025', 4), -- dr.morales
(23, 2, 7, 23, 6, 0, 2, true, 'IT Project Management - Spring 2025', 4), -- prof.castro

-- Software Engineering classes
(24, 2, 8, 24, 4, 0, 0, true, 'Agile Development - Spring 2025', 4), -- dr.vargas
(25, 2, 1, 25, 10, 2, 0, true, 'Software Architecture - Spring 2025', 4), -- teacher user
(26, 2, 2, 26, 8, 0, 0, true, 'Mobile Development - Spring 2025', 4), -- maria.garcia
(27, 2, 3, 27, 8, 0, 4, true, 'DevOps and CI/CD - Spring 2025', 4), -- carlos.lopez
(28, 2, 4, 28, 8, 0, 0, true, 'Software Testing - Spring 2025', 4), -- dr.silva
(29, 2, 5, 29, 8, 0, 0, true, 'Cloud Computing - Spring 2025', 4); -- prof.torres

-- Academic requests (for semester 2 - Spring 2025) --
INSERT INTO academic_request (id, user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation) VALUES
-- Computer Science program requests (IDs 1-10)
(1, 24, 1, 2, '2025-01-15', '2025-05-30', 45, '2024-12-01', 'Request for Database Systems - Spring 2025 - Group 1'),
(2, 24, 1, 2, '2025-01-15', '2025-05-30', 40, '2024-12-01', 'Request for Database Systems - Spring 2025 - Group 2'),
(3, 24, 2, 2, '2025-01-15', '2025-05-30', 50, '2024-12-02', 'Request for Software Engineering - Spring 2025 - Group 1'),
(4, 24, 2, 2, '2025-01-15', '2025-05-30', 45, '2024-12-02', 'Request for Software Engineering - Spring 2025 - Group 2'),
(5, 24, 3, 2, '2025-01-15', '2025-05-30', 55, '2024-12-03', 'Request for Computer Networks - Spring 2025'),
(6, 24, 4, 2, '2025-01-15', '2025-05-30', 40, '2024-12-04', 'Request for Machine Learning - Spring 2025'),
(7, 24, 5, 2, '2025-01-15', '2025-05-30', 35, '2024-12-05', 'Request for Computer Graphics - Spring 2025'),
(8, 24, 6, 2, '2025-01-15', '2025-05-30', 45, '2024-12-06', 'Request for Algorithms and Data Structures - Spring 2025'),
(9, 24, 7, 2, '2025-01-15', '2025-05-30', 40, '2024-12-07', 'Request for Operating Systems - Spring 2025'),
(10, 24, 8, 2, '2025-01-15', '2025-05-30', 45, '2024-12-08', 'Request for Web Development - Spring 2025'),

-- Information Systems program requests (IDs 11-17)
(11, 25, 9, 2, '2025-01-15', '2025-05-30', 35, '2024-12-09', 'Request for Information Security - Spring 2025 - Group 1'),
(12, 25, 9, 2, '2025-01-15', '2025-05-30', 30, '2024-12-09', 'Request for Information Security - Spring 2025 - Group 2'),
(13, 25, 10, 2, '2025-01-15', '2025-05-30', 40, '2024-12-10', 'Request for Data Analytics - Spring 2025'),
(14, 25, 11, 2, '2025-01-15', '2025-05-30', 35, '2024-12-11', 'Request for Business Intelligence - Spring 2025'),
(15, 25, 12, 2, '2025-01-15', '2025-05-30', 40, '2024-12-12', 'Request for Systems Analysis - Spring 2025'),
(16, 25, 13, 2, '2025-01-15', '2025-05-30', 30, '2024-12-13', 'Request for Enterprise Architecture - Spring 2025'),
(17, 25, 14, 2, '2025-01-15', '2025-05-30', 35, '2024-12-14', 'Request for IT Project Management - Spring 2025'),

-- Software Engineering program requests (IDs 18-23)
(18, 12, 15, 2, '2025-01-15', '2025-05-30', 30, '2024-12-15', 'Request for Agile Development - Spring 2025'),
(19, 12, 16, 2, '2025-01-15', '2025-05-30', 35, '2024-12-16', 'Request for Software Architecture - Spring 2025'),
(20, 12, 17, 2, '2025-01-15', '2025-05-30', 40, '2024-12-17', 'Request for Mobile Development - Spring 2025'),
(21, 12, 18, 2, '2025-01-15', '2025-05-30', 35, '2024-12-18', 'Request for DevOps and CI/CD - Spring 2025'),
(22, 12, 19, 2, '2025-01-15', '2025-05-30', 40, '2024-12-19', 'Request for Software Testing - Spring 2025'),
(23, 12, 20, 2, '2025-01-15', '2025-05-30', 35, '2024-12-20', 'Request for Cloud Computing - Spring 2025');

-- Academic requests (for past semester 1 - Fall 2024) - IDs 24-26
INSERT INTO academic_request (id, user_id, course_id, semester_id, start_date, end_date, capacity, request_date, observation) VALUES
(24, 5, 1, 1, '2024-08-15', '2024-12-15', 40, '2024-06-01', 'Request for Database Systems - Fall 2024'),
(25, 5, 2, 1, '2024-08-15', '2024-12-15', 35, '2024-06-03', 'Request for Software Engineering - Fall 2024'),
(26, 5, 3, 1, '2024-08-15', '2024-12-15', 50, '2024-06-05', 'Request for Computer Networks - Fall 2024');

-- Request schedules (in pairs) - COMPREHENSIVE FOR ALL ACADEMIC REQUESTS --
INSERT INTO request_schedule (academic_request_id, classroom_type_id, start_time, end_time, day, modality_id, disability) VALUES
-- Current semester (Spring 2025) request schedules - Computer Science Program
-- Database Systems Group 1 (ID 1)
(1, 1, '08:00:00', '10:00:00', 'Lunes', 1, false),
(1, 1, '08:00:00', '10:00:00', 'Miercoles', 1, false),
-- Database Systems Group 2 (ID 2)  
(2, 1, '08:00:00', '10:00:00', 'Martes', 1, false),
(2, 1, '08:00:00', '10:00:00', 'Jueves', 1, false),
-- Software Engineering Group 1 (ID 3)
(3, 1, '10:00:00', '12:00:00', 'Lunes', 1, false),
(3, 1, '10:00:00', '12:00:00', 'Viernes', 1, false),
-- Software Engineering Group 2 (ID 4)
(4, 1, '10:00:00', '12:00:00', 'Martes', 1, false),
(4, 1, '10:00:00', '12:00:00', 'Jueves', 1, false),
-- Computer Networks (ID 5)
(5, 1, '14:00:00', '16:00:00', 'Lunes', 1, false),
(5, 1, '14:00:00', '16:00:00', 'Miercoles', 1, false),
-- Machine Learning (ID 6)
(6, 2, '14:00:00', '16:00:00', 'Martes', 1, false),
(6, 2, '14:00:00', '16:00:00', 'Jueves', 1, false),
-- Computer Graphics (ID 7)
(7, 2, '16:00:00', '18:00:00', 'Miercoles', 1, false),
(7, 2, '16:00:00', '18:00:00', 'Viernes', 1, false),
-- Algorithms and Data Structures (ID 8)
(8, 1, '08:00:00', '10:00:00', 'Lunes', 1, false),
(8, 1, '08:00:00', '10:00:00', 'Viernes', 1, false),
-- Operating Systems (ID 9)
(9, 1, '16:00:00', '18:00:00', 'Martes', 1, false),
(9, 1, '16:00:00', '18:00:00', 'Jueves', 1, false),
-- Web Development (ID 10)
(10, 2, '08:00:00', '10:00:00', 'Miercoles', 1, false),
(10, 2, '08:00:00', '10:00:00', 'Viernes', 1, false),

-- Information Systems Program requests
-- Information Security Group 1 (ID 11)
(11, 2, '16:00:00', '18:00:00', 'Lunes', 1, false),
(11, 2, '16:00:00', '18:00:00', 'Miercoles', 1, false),
-- Information Security Group 2 (ID 12)
(12, 2, '18:00:00', '20:00:00', 'Martes', 1, false),
(12, 2, '18:00:00', '20:00:00', 'Jueves', 1, false),
-- Data Analytics (ID 13)
(13, 1, '18:00:00', '20:00:00', 'Lunes', 1, false),
(13, 1, '18:00:00', '20:00:00', 'Viernes', 1, false),
-- Business Intelligence (ID 14)
(14, 2, '12:00:00', '14:00:00', 'Martes', 1, false),
(14, 2, '12:00:00', '14:00:00', 'Jueves', 1, false),
-- Systems Analysis (ID 15)
(15, 1, '12:00:00', '14:00:00', 'Miercoles', 1, false),
(15, 1, '12:00:00', '14:00:00', 'Viernes', 1, false),
-- Enterprise Architecture (ID 16)
(16, 1, '12:00:00', '14:00:00', 'Lunes', 1, false),
(16, 1, '12:00:00', '14:00:00', 'Miercoles', 1, false),
-- IT Project Management (ID 17)
(17, 1, '20:00:00', '22:00:00', 'Martes', 1, false),
(17, 1, '20:00:00', '22:00:00', 'Jueves', 1, false),

-- Software Engineering Program requests
-- Agile Development (ID 18)
(18, 1, '20:00:00', '22:00:00', 'Lunes', 1, false),
(18, 1, '20:00:00', '22:00:00', 'Viernes', 1, false),
-- Software Architecture (ID 19)
(19, 1, '06:00:00', '08:00:00', 'Martes', 1, false),
(19, 1, '06:00:00', '08:00:00', 'Jueves', 1, false),
-- Mobile Development (ID 20)
(20, 2, '18:00:00', '20:00:00', 'Miercoles', 1, false),
(20, 2, '18:00:00', '20:00:00', 'Viernes', 1, false),
-- DevOps and CI/CD (ID 21)
(21, 2, '06:00:00', '08:00:00', 'Lunes', 1, false),
(21, 2, '06:00:00', '08:00:00', 'Miercoles', 1, false),
-- Software Testing (ID 22)
(22, 2, '20:00:00', '22:00:00', 'Martes', 1, false),
(22, 2, '20:00:00', '22:00:00', 'Jueves', 1, false),
-- Cloud Computing (ID 23)
(23, 2, '22:00:00', '24:00:00', 'Lunes', 1, false),
(23, 2, '22:00:00', '24:00:00', 'Viernes', 1, false),

-- Past semester (Fall 2024) request schedules
-- Database Systems Fall 2024 (ID 24)
(24, 1, '09:00:00', '11:00:00', 'Martes', 1, false),
(24, 1, '09:00:00', '11:00:00', 'Jueves', 1, false),
-- Software Engineering Fall 2024 (ID 25)
(25, 1, '14:00:00', '16:00:00', 'Lunes', 1, false),
(25, 1, '14:00:00', '16:00:00', 'Miercoles', 1, false),
-- Computer Networks Fall 2024 (ID 26)
(26, 1, '14:00:00', '16:00:00', 'Lunes', 1, false),
(26, 1, '14:00:00', '16:00:00', 'Viernes', 1, false);