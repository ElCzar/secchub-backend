/**
 * Integration module that handles academic workflows and student-teacher interactions.
 * 
 * This module handles:
 * - Academic request processing and management
 * - Student application lifecycle (creation, approval, rejection)
 * - Teacher class assignments and scheduling
 * - Cross-system integration and workflow coordination
 * 
 * Dependencies:
 * - Security module: For user authentication, user lookup, and authorization
 * 
 * This module serves as the core business logic layer for any request to the system,
 * allowing coordination between different actors (students, teachers, administrators).
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Integration Module", 
    allowedDependencies = "security"
)
package co.edu.puj.secchub_backend.integration;