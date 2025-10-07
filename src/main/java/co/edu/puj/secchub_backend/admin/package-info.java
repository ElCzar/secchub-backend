/**
 * Admin module that provides administrative functionality for academic management.
 * 
 * This module handles:
 * - Course management (creation, updates, queries)
 * - Section management and organization
 * - Administrative operations and oversight
 * 
 * Dependencies:
 * - Security module: For user authentication and authorization
 * - Parametric module: For status and role lookups
 * 
 * This module exposes APIs for course and section management to other modules
 * through its service layer and DTOs.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Admin Module",
    allowedDependencies = {"security", "parametric"}
)
package co.edu.puj.secchub_backend.admin;