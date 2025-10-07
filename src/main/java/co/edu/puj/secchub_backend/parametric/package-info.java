/**
 * Parametric module that provides lookup values and reference data for the entire application.
 * 
 * This module handles:
 * - Status values (ACTIVE, PENDING, APPROVED, REJECTED, etc.)
 * - Role definitions (ADMIN, USER, TEACHER, STUDENT, etc.)
 * - Document types (CC, CE, PASSPORT, etc.)
 * - Employment types (FULL_TIME, PART_TIME, ADJUNCT, etc.)
 * - Modality types (PRESENTIAL, VIRTUAL, HYBRID, etc.)
 * - Session types and classroom types
 * 
 * This is a foundational module that:
 * - Has no dependencies on other business modules
 * - Provides cached lookup services for performance
 * - Exposes contracts for other modules to consume
 * - Maintains referential integrity for all parametric values
 * 
 * All parametric values are cached for optimal performance since they
 * are frequently accessed but rarely change.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Parametric Module",
    allowedDependencies = {}
)
package co.edu.puj.secchub_backend.parametric;