/**
 * Admin module that provides administrative functionality for system management.
 * 
 * This module handles:
 * - Course management (creation, updates, queries)
 * - Section management and organization
 * - Administrative operations and oversight
 * 
 * Dependencies:
 * - Security module: For user authentication and authorization
 *
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Admin Module",
    allowedDependencies = "security"
)
package co.edu.puj.secchub_backend.admin;