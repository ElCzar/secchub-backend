-- ============================================
-- Test Semesters Data - Insert sample semesters
-- ============================================
-- Purpose: Provides test semesters for integration testing of academic periods
--
-- Common Test Data:
-- - Period: 1 (First semester) or 2 (Second semester)
-- - Year: Various years for testing different periods
-- - Is Current: Only one semester should be current
-- - Dates: Realistic start and end dates
-- ============================================

INSERT INTO semester (
    id,                 -- Primary key (auto-incremented)
    period,             -- Semester period (1 = First, 2 = Second)
    year,               -- Academic year
    is_current,         -- Whether this is the current active semester
    start_date,         -- Semester start date
    end_date            -- Semester end date
) VALUES
-- ==================
-- 1. Current Semester
-- ==================
-- Purpose: Active semester for current operations and testing
-- Note: This should be the only semester with is_current = TRUE
(
    2,                  -- id
    2,                  -- Second semester
    2025,               -- Current year
    TRUE,               -- Current active semester
    '2025-08-01',       -- Start: August 1, 2025
    '2025-12-15'        -- End: December 15, 2025
),

-- ==================
-- 2. Previous Semester
-- ==================
-- Purpose: Most recent past semester for historical data testing
(
    1,                  -- id
    1,                  -- First semester
    2025,               -- Current year
    FALSE,              -- Not current
    '2025-01-15',       -- Start: January 15, 2025
    '2025-06-30'        -- End: June 30, 2025
);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Only ONE semester should have is_current = TRUE at any time
-- 2. Semester ID = 1 is the current active semester (2025-2)
-- 3. Dates follow typical academic calendar (Jan-Jun, Aug-Dec)
-- 4. Period 1 = First semester (Spring), Period 2 = Second semester (Fall)
-- 5. Used by student_application, academic_request, teacher_class, and class tables
-- 6. This data can be loaded independently but should come before dependent tables
-- 7. No foreign key dependencies - can be loaded early in test setup
-- ============================================
