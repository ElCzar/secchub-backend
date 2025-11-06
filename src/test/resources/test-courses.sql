-- ============================================
-- Test Courses Data - Insert sample courses
-- ============================================
-- Purpose: Provides test courses for integration testing of the admin course system
--
-- Common Test Data:
-- - Section ID: 1 (Engineering section)
-- - Credits: Various credit values (2-4 credits typical)
-- - Status ID: 1 (Active)
-- - Is Valid: TRUE for most courses
-- ============================================

INSERT INTO course (
    id,                 -- Primary key (auto-incremented)
    section_id,         -- FK to section table
    name,               -- Course name
    credits,            -- Number of academic credits
    description,        -- Course description
    is_valid,           -- Whether course is valid/approved
    recommendation,     -- Prerequisites or recommendations
    status_id           -- Course status (FK to status table)
) VALUES
-- ==================
-- 1. Core Engineering Courses
-- ==================
-- Purpose: Fundamental engineering subjects for testing basic CRUD
(
    1,                  -- Course ID
    1,                  -- Engineering section
    'Data Structures',
    4,
    'Introduction to fundamental data structures including arrays, linked lists, trees, and graphs.',
    TRUE,
    'Prerequisites: Programming Fundamentals',
    1                   -- Active status
),
(
    2,                  -- Course ID
    1,                  -- Engineering section
    'Algorithms',
    4,
    'Study of algorithm design techniques including divide and conquer, dynamic programming, and greedy algorithms.',
    TRUE,
    'Prerequisites: Data Structures, Discrete Mathematics',
    1                   -- Active status
),
(
    3,                  -- Course ID
    1,                  -- Engineering section
    'Database Systems',
    3,
    'Relational database design, SQL, transactions, and database management systems.',
    TRUE,
    'Prerequisites: Data Structures',
    1                   -- Active status
),
(
    4,                  -- Course ID
    1,                  -- Engineering section
    'Operating Systems',
    4,
    'Study of operating system concepts including process management, memory management, and file systems.',
    TRUE,
    'Prerequisites: Data Structures, Computer Architecture',
    1                   -- Active status
),

-- ==================
-- 2. Advanced Courses
-- ==================
-- Purpose: Higher-level courses for testing filtering and updates
(
    5,                  -- Course ID
    1,                  -- Engineering section
    'Software Engineering',
    3,
    'Software development lifecycle, design patterns, and project management methodologies.',
    TRUE,
    'Prerequisites: Database Systems, Object-Oriented Programming',
    1                   -- Active status
),
(
    6,                  -- Course ID
    1,                  -- Engineering section
    'Web Development',
    3,
    'Modern web technologies including HTML, CSS, JavaScript, and frameworks like React and Spring Boot.',
    TRUE,
    'Prerequisites: Database Systems',
    1                   -- Active status
),
(
    7,                  -- Course ID
    1,                  -- Engineering section
    'Machine Learning',
    4,
    'Introduction to machine learning algorithms including supervised and unsupervised learning.',
    TRUE,
    'Prerequisites: Linear Algebra, Statistics, Algorithms',
    1                   -- Active status
),

-- ==================
-- 3. Elective Courses
-- ==================
-- Purpose: Optional courses with varying credit values for testing edge cases
(
    8,                  -- Course ID
    1,                  -- Engineering section
    'Mobile Development',
    3,
    'Development of mobile applications for iOS and Android platforms.',
    TRUE,
    'Recommended: Web Development',
    1                   -- Active status
),
(
    9,                  -- Course ID
    1,                  -- Engineering section
    'Cloud Computing',
    3,
    'Cloud computing concepts, AWS, Azure, containerization with Docker and Kubernetes.',
    TRUE,
    'Recommended: Operating Systems, Web Development',
    1                   -- Active status
),
(
    10,                 -- Course ID
    1,                  -- Engineering section
    'Cybersecurity',
    3,
    'Network security, cryptography, ethical hacking, and security best practices.',
    TRUE,
    'Prerequisites: Computer Networks',
    1                   -- Active status
);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. All courses belong to section_id = 1 (Engineering) for simplicity
-- 2. Credits range from 2 to 4, typical for university courses
-- 3. Most courses have is_valid = TRUE (approved), some FALSE for testing
-- 4. Recommendations provide context but are not enforced constraints
-- 5. Status ID = 1 (Active) for all test courses
-- 6. Course names are realistic and diverse for meaningful tests
-- 7. This data should be loaded AFTER parametric data and test-users.sql
-- ============================================
