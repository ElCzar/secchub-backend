package co.edu.puj.secchub_backend.admin.contract;

/**
 * Contract interface for course-related operations in the admin module.
 * Defines methods for obtaining course information.
 */
public interface AdminModuleCourseContract {
    /**
     * Obtains the name of a course by its ID.
     * @param courseId the ID of the course
     * @return the name of the course, or "Curso sin nombre" if not found
     */
    String getCourseName(Long courseId);
}