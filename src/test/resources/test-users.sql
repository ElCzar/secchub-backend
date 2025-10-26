INSERT INTO users (username, password, faculty, name, last_name, email, status_id, last_access, role_id, document_type_id, document_number) VALUES
('adminuser', '$2a$10$encoded_password', 'Engineering', 'Admin', 'User', 'testAdmin@example.com', 1, NOW(), 1, 1, '1234567890'),
('regularuser', '$2a$10$encoded_password', 'Engineering', 'Regular', 'User', 'testUser@example.com', 1, NOW(), 2, 1, '0987654321'),
('student', '$2a$10$encoded_password', 'Engineering', 'Student', 'User', 'testStudent@example.com', 1, NOW(), 3, 1, '1122334455'),
('teacher', '$2a$10$encoded_password', 'Science', 'Teacher', 'User', 'testTeacher@example.com', 1, NOW(), 4, 1, '5544332211'),
('programuser', '$2a$10$encoded_password', 'Arts', 'Program', 'User', 'testProgram@example.com', 1, NOW(), 5, 1, '9988776655');