-- Status of the users and the state of processes --
INSERT INTO status (name) VALUES
    ('Active'),
    ('Inactive'),
    ('Finished'),
    ('Pending'),
    ('Cancelled'),
    ('In Progress'),
    ('On Hold'),
    ('Confirmed'),
    ('Rejected'),
    ('Completed');

-- Roles based on user permissions --
INSERT INTO role (name) VALUES 
    ('ROLE_ADMIN'), 
    ('ROLE_USER'), 
    ('ROLE_STUDENT'), 
    ('ROLE_TEACHER');

-- Document Types valid in Colombia --
INSERT INTO document_type (name) VALUES
  ('CC'),
  ('TI'),
  ('CE'),
  ('RC'),
  ('NIT'),
  ('Passport');

-- The types of employment --
INSERT INTO employment_type (name) VALUES
    ('Full-Time'),
    ('Part-Time');

-- The modalities of the courses --
INSERT INTO modality (name) VALUES
    ('In-Person'),
    ('Online');

-- The sessions available for the courses --
INSERT INTO session (name) VALUES
    ('Morning'),
    ('Evening');

-- The types of classrooms --
INSERT INTO classroom_type (name) VALUES
    ('Lecture'),
    ('Lab');

-- Sample user with password hashed --
INSERT INTO users (username, password, name, last_name, email, status_id, role_id, document_type_id, document_number)
VALUES ('admin', '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq', 'Admin', 'User', 'admin@secchub.com', 1, 1, 1, '12345678');