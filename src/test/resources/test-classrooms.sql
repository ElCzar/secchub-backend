-- ============================================
-- Test Classrooms Data - Insert sample classrooms
-- ============================================
-- Purpose: Provides test classrooms for integration testing of the planning system
--
-- Common Test Data:
-- - Campus: Various campus locations for testing filters
-- - Location: Building and floor information
-- - Room: Unique room identifiers
-- - Capacity: Student capacity ranging from small (20) to large (200)
-- ============================================

INSERT INTO classroom (
    classroom_type_id,  -- FK to classroom_type table
    campus,             -- Campus name/location
    location,           -- Building and floor details
    room,               -- Room number/identifier
    capacity            -- Maximum student capacity
) VALUES
-- ==================
-- 1. Aulas (Regular Classrooms)
-- ==================
-- Purpose: Standard lecture rooms for regular classes
-- Use in tests: Basic CRUD operations, list all classrooms
(
    1,                  -- Aula type
    'Campus Central',
    'Edificio A - Piso 1',
    'A-101',
    30
),
(
    1,                  -- Aula type
    'Campus Central',
    'Edificio A - Piso 2',
    'A-201',
    40
),
(
    1,                  -- Aula type
    'Campus Norte',
    'Edificio B - Piso 1',
    'B-105',
    35
),
(
    1,                  -- Aula type
    'Campus Sur',
    'Edificio C - Piso 3',
    'C-301',
    45
),

-- ==================
-- 2. Laboratorios
-- ==================
-- Purpose: Science and computer labs with specialized equipment
-- Use in tests: Filter by type, capacity constraints
(
    2,                  -- Laboratorio type
    'Campus Central',
    'Edificio de Ciencias - Piso 2',
    'LAB-201',
    25
),
(
    2,                  -- Laboratorio type
    'Campus Sur',
    'Edificio de Computación - Piso 1',
    'COMP-101',
    30
),
(
    2,                  -- Laboratorio type
    'Campus Sur',
    'Edificio de Computación - Piso 2',
    'COMP-202',
    28
),
(
    2,                  -- Laboratorio type
    'Campus Oeste',
    'Edificio de Investigación - Piso 3',
    'INV-305',
    20
),

-- ==================
-- 3. Aulas Móviles
-- ==================
-- Purpose: Flexible classrooms that can be adapted for different purposes
-- Use in tests: Special room type handling, update operations
(
    3,                  -- Aula Movil type
    'Campus Central',
    'Módulo Temporal - Zona A',
    'MOV-A1',
    25
),
(
    3,                  -- Aula Movil type
    'Campus Norte',
    'Módulo Temporal - Zona B',
    'MOV-B2',
    22
),
(
    3,                  -- Aula Movil type
    'Campus Este',
    'Edificio Polivalente - Piso 1',
    'POLI-101',
    30
),

-- ==================
-- 4. Auditorios
-- ==================
-- Purpose: Large capacity venues for conferences and major lectures
-- Use in tests: Large capacity queries, special venue handling
(
    4,                  -- Auditorio type
    'Campus Central',
    'Centro de Conferencias',
    'AUD-PRINCIPAL',
    200
),
(
    4,                  -- Auditorio type
    'Campus Norte',
    'Sala de Eventos',
    'AUD-NORTE',
    150
),
(
    4,                  -- Auditorio type
    'Campus Sur',
    'Teatro Universitario',
    'AUD-SUR',
    180
),

-- ==================
-- 5. Additional Test Data
-- ==================
-- Purpose: Various scenarios for comprehensive testing
-- Use in tests: Edge cases, delete operations, campus filters
(
    1,                  -- Aula type
    'Campus Oeste',
    'Edificio D - Piso 1',
    'D-110',
    38
),
(
    2,                  -- Laboratorio type
    'Campus Central',
    'Edificio de Ingeniería - Piso 2',
    'ING-LAB1',
    24
);

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Each classroom has a unique room identifier
-- 2. Capacities are realistic for different room types
-- 3. Multiple classrooms per type allow for filtering tests
-- 4. Various campuses enable location-based queries
-- 5. Room numbers follow Spanish naming conventions
-- 6. Classroom types match the actual system: Aula, Laboratorio, Aula Movil, Auditorio
-- ============================================