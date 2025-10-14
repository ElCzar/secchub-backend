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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

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
        return Mono.fromCallable(() -> {
            if (courseRequestDTO.getSectionId() != null) {
                sectionService.findSectionById(courseRequestDTO.getSectionId()).block();
            }
            
            Course course = modelMapper.map(courseRequestDTO, Course.class);
            Course saved = courseRepository.save(course);
            return modelMapper.map(saved, CourseResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }    
    
    /**
     * Lists all existing courses.
     * @return List of courses
     */
    public List<CourseResponseDTO> findAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(course -> modelMapper.map(course, CourseResponseDTO.class))
                .toList();
    }

    /**
     * Get a course by its ID.
     * @param courseId Course ID
     * @return Course with the given ID
     */
    public Mono<CourseResponseDTO> findCourseById(Long courseId) {
        return Mono.fromCallable(() -> {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found for consult: " + courseId));
            return modelMapper.map(course, CourseResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates a course by its ID.
     * @param courseId Course ID
     * @param courseRequestDTO with updated data
     * @return Updated course
     */
    public Mono<CourseResponseDTO> updateCourse(Long courseId, CourseRequestDTO courseRequestDTO) {
        return Mono.fromCallable(() -> {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found for update: " + courseId));

            // Validate that the section exists if sectionId is being updated
            if (courseRequestDTO.getSectionId() != null) {
                sectionService.findSectionById(courseRequestDTO.getSectionId()).block();
            }

            modelMapper.getConfiguration().setPropertyCondition(context ->
                    context.getSource() != null);
            modelMapper.map(courseRequestDTO, course);

            return modelMapper.map(courseRepository.save(course), CourseResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<CourseResponseDTO> patchCourse(Long id, Map<String, Object> updates) {
        return Mono.fromCallable(() -> {
            Course course = courseRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found with id=" + id));

            modelMapper.map(updates, course);

            return modelMapper.map(courseRepository.save(course), CourseResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Implementation of AdminModuleCourseContract.
     * Gets the course name by its ID.
     */
    @Override
    public String getCourseName(Long courseId) {
        return courseRepository.findById(courseId)
                .map(Course::getName)
                .orElse("Curso sin nombre");
    }
}
