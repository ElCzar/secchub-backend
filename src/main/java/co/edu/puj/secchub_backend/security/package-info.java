/**
 * Security module that provides authentication, authorization, and user management services.
 * 
 * This module exposes the following APIs to other modules:
 * - {@link co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract} for user-related operations
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Security Module",
    allowedDependencies = {}
)
package co.edu.puj.secchub_backend.security;