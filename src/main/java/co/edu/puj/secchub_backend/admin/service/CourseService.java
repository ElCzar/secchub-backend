package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleCourseContract;
import co.edu.puj.secchub_backend.admin.dto.CourseRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.CourseResponseDTO;
import co.edu.puj.secchub_backend.admin.exception.CourseNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Course;
import co.edu.puj.secchub_backend.admin.repository.CourseRepository;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseService implements AdminModuleCourseContract {
    private final ModelMapper modelMapper;
    private final CourseRepository courseRepository;
    private final SectionService sectionService;

    /**
     * Creates a new course.
     * @param courseRequestDTO dto with course data
     * @return Created course
     */
    public Mono<CourseResponseDTO> createCourse(CourseRequestDTO courseRequestDTO) {
        return courseRepository.existsByName(courseRequestDTO.getName())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new IllegalArgumentException("Course with name already exists: " + courseRequestDTO.getName()));
                    }
                    return sectionService.findSectionById(courseRequestDTO.getSectionId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Section not found with id: " + courseRequestDTO.getSectionId())))
                            .flatMap(section -> {
                                Course course = modelMapper.map(courseRequestDTO, Course.class);
                                return courseRepository.save(course)
                                        .map(savedCourse -> modelMapper.map(savedCourse, CourseResponseDTO.class));
                            });
                });
    }    
    
    /**
     * Lists all existing courses.
     * @return Flux of courses
     */
    public Flux<CourseResponseDTO> findAllCourses() {
        return courseRepository.findAll()
                .map(course -> modelMapper.map(course, CourseResponseDTO.class));
    }

    /**
     * Get a course by its ID.
     * @param courseId Course ID
     * @return Course with the given ID
     */
    public Mono<CourseResponseDTO> findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .switchIfEmpty(Mono.error(new CourseNotFoundException("Course not found with id: " + courseId)))
                .map(course -> modelMapper.map(course, CourseResponseDTO.class));
    }

    /**
     * Updates a course by its ID.
     * @param courseId Course ID
     * @param courseRequestDTO with updated data
     * @return Updated course
     */
    public Mono<CourseResponseDTO> updateCourse(Long courseId, CourseRequestDTO courseRequestDTO) {
        return courseRepository.findById(courseId)
                .switchIfEmpty(Mono.error(new CourseNotFoundException("Course for update not found with id: " + courseId)))
                .flatMap(existingCourse -> {
                    modelMapper.map(courseRequestDTO, existingCourse);
                    return courseRepository.save(existingCourse)
                            .map(updatedCourse -> modelMapper.map(updatedCourse, CourseResponseDTO.class));
                });
    }

    /**
     * Partially updates a course by its ID.
     * @param id Course ID
     * @param updates Map of fields to update
     * @return Updated course
     */
    public Mono<CourseResponseDTO> patchCourse(Long id, Map<String, Object> updates) {
        return courseRepository.findById(id)
                .switchIfEmpty(Mono.error(new CourseNotFoundException("Course for patch not found with id: " + id)))
                .flatMap(existingCourse -> {
                    modelMapper.map(updates, existingCourse);
                    return courseRepository.save(existingCourse)
                            .map(updatedCourse -> modelMapper.map(updatedCourse, CourseResponseDTO.class));
                });
    }

    /**
     * Deletes a course by its ID.
     * @param courseId Course ID
     * @return Mono signaling completion
     */
    public Mono<Void> deleteCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .switchIfEmpty(Mono.error(new CourseNotFoundException("Course for deletion not found with id: " + courseId)))
                .flatMap(courseRepository::delete);
    }

    /**
     * Implementation of AdminModuleCourseContract.
     * Gets the course name by its ID.
     */
    @Override
    public Mono<String> getCourseName(Long courseId) {
        return courseRepository.findById(courseId)
                .map(Course::getName)
                .defaultIfEmpty("N/A");
    }

    /**
     * Implementation of AdminModuleCourseContract.
     * Gets the section ID associated with a course.
     */
    @Override
    public Mono<Long> getCourseSectionId(Long courseId) {
        return courseRepository.findById(courseId)
                .map(Course::getSectionId)
                .map(Long::valueOf)
                .switchIfEmpty(Mono.error(new CourseNotFoundException("Course not found for section ID retrieval: " + courseId)));
    }
}
