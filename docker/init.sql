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
    (10, 'Completed');

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

-- Sample user with password hashed --
INSERT INTO users (username, password, name, last_name, email, status_id, role_id, document_type_id, document_number)
VALUES 
('admin', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Admin', 'User', 'admin@secchub.com', 1, 1, 1, '12345678'),
('user', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Regular', 'User', 'user@secchub.com', 1, 2, 1, '87654321'),
('student', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Student', 'User', 'student@secchub.com', 1, 3, 1, '11111111'),
('teacher', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Teacher', 'User', 'teacher@secchub.com', 1, 4, 1, '22222222'),
('program', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Program', 'User', 'program@secchub.com', 1, 5, 1, '33333333');