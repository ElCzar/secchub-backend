-- ============================================
-- Test Users Data - Insert sample users
-- ============================================
-- Purpose: Provides test users for integration testing of the academic system
--
-- Common Test Data:
-- - Password: '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq' (BCrypt encoded - same for all test users)
-- - Status ID: 1 (Active)
-- - Document Type ID: 1 (Default document type)
-- - Last Access: Current timestamp
-- ============================================

INSERT INTO users (
    username,           -- Login username
    password,           -- BCrypt encoded password
    faculty,            -- Academic faculty/department
    name,               -- First name
    last_name,          -- Last name
    email,              -- Unique email address
    status_id,          -- User status (FK to status table)
    last_access,        -- Last login timestamp
    role_id,            -- User role (FK to role table)
    document_type_id,   -- Document type (FK to document_type table)
    document_number     -- Identification number
) VALUES
-- ==================
-- 1. Administrator
-- ==================
-- Email: testAdmin@example.com
(
    'adminuser',
    '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq',
    'Engineering',
    'Admin',
    'User',
    'testAdmin@example.com',
    1,      -- Active status
    NOW(),
    1,      -- Admin role
    1,      -- Default document type
    '1234567890'
),

-- ==================
-- 2. Section Standard User
-- ==================
-- Email: testUser@example.com
(
    'regularuser',
    '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq',
    'Engineering',
    'Regular',
    'User',
    'testUser@example.com',
    1,      -- Active status
    NOW(),
    2,      -- Regular user role
    1,      -- Default document type
    '0987654321'
),

-- ==================
-- 3. Student
-- ==================
-- Email: testStudent@example.com
(
    'student',
    '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq',
    'Engineering',
    'Student',
    'User',
    'testStudent@example.com',
    1,      -- Active status
    NOW(),
    3,      -- Student role
    1,      -- Default document type
    '1122334455'
),

-- ==================
-- 4. Teacher
-- ==================
-- Email: testTeacher@example.com
(
    'teacher',
    '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq',
    'Science',
    'Teacher',
    'User',
    'testTeacher@example.com',
    1,      -- Active status
    NOW(),
    4,      -- Teacher role
    1,      -- Default document type
    '5544332211'
),

-- ==================
-- 5. Program
-- ==================
-- Email: testProgram@example.com
(
    'programuser',
    '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq',
    'Arts',
    'Program',
    'User',
    'testProgram@example.com',
    1,      -- Active status
    NOW(),
    5,      -- Program coordinator role
    1,      -- Default document type
    '9988776655'
);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. All passwords are the same for testing: '$2a$10$8y88Ox9NYdBZ/4y.SUr.suOAF3qT0g/zfGQMWLwMRRoUk8p/YjhTq' which is BCrypt for 'password'
-- 2. Each user has a unique email for email-based authentication
-- 3. Document numbers are fictional and unique
-- 4. This data should be loaded AFTER the parametric data (roles, statuses, document types)
-- ============================================