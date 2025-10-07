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
 * - Admin module: For course and section information (implicit through shared entities)
 * - Parametric module: For status and role lookups
 * 
 * This module serves as the core business logic layer for academic operations,
 * coordinating between different actors (students, teachers, administrators) and
 * managing complex academic workflows.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Integration Module", 
    allowedDependencies = {"security", "admin", "parametric"}
)
package co.edu.puj.secchub_backend.integration;