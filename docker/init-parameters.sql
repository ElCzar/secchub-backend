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
    (14, 'Uploaded');

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
    (6, 'Pasaporte');

-- The types of employment --
INSERT INTO employment_type (id, name) VALUES
    (1, 'Tiempo Completo'),
    (2, 'Medio Tiempo');

-- The modalities of the courses --
INSERT INTO modality (id, name) VALUES
    (1, 'Presencial'),
    (2, 'Online');

-- The types of classrooms --
INSERT INTO classroom_type (id, name) VALUES
    (1, 'Aula'),
    (2, 'Laboratorio'),
    (3, 'Aula Movil'),
    (4, 'Auditorio');