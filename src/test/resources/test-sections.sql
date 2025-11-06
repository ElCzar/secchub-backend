-- ============================================
-- Test Sections Data - Insert sample sections
-- ============================================
-- Purpose: Provides test sections for integration testing of the admin system
--
-- Common Test Data:
-- - User ID: References test users from test-users.sql
-- - Planning Closed: FALSE for most sections (open for planning)
-- ============================================

INSERT INTO section (
    id,                 -- Primary key (auto-incremented)
    user_id,            -- FK to users table (section coordinator)
    name,               -- Section name
    planning_closed     -- Whether planning is closed for this section
) VALUES
-- ==================
-- 1. Engineering Section
-- ==================
-- Purpose: Main section for engineering courses and testing
-- Coordinator: testAdmin@example.com (user_id will be 1 after test-users.sql)
-- Note: This is section_id = 1, used by test-courses.sql
(
    1,                  -- Section ID
    2,                  -- Admin user as coordinator
    'Engineering',
    FALSE               -- Planning open
),

-- ==================
-- 2. Science Section
-- ==================
-- Purpose: Science department section
-- Coordinator: testTeacher@example.com (user_id will be 4 after test-users.sql)
(
    2,                  -- Section ID
    4,                  -- Teacher user as coordinator
    'Science',
    FALSE               -- Planning open
),

-- ==================
-- 3. Arts Section
-- ==================
-- Purpose: Arts and humanities section
-- Coordinator: testProgram@example.com (user_id will be 5 after test-users.sql)
(
    3,                  -- Section ID
    5,                  -- Program user as coordinator
    'Arts',
    FALSE               -- Planning open
);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Section with id=1 (Engineering) is used by test-courses.sql
-- 2. User IDs reference test users created by test-users.sql
-- 3. Most sections have planning_closed = FALSE for active testing
-- 4. Mathematics section has planning_closed = TRUE for testing restrictions
-- 5. This data should be loaded AFTER test-users.sql
-- 6. Load order: test-users.sql → test-sections.sql → test-courses.sql
-- ============================================
