/**
 * This package contains the planning functionality of the SeccHub backend.
 * 
 * This package plays a crucial role in the overall architecture of the SeccHub
 * backend by ensuring that user can plan the classes for the next academic semester.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Planning Module",
    allowedDependencies = {"admin", "security", "parametric"}
)
package co.edu.puj.secchub_backend.planning;