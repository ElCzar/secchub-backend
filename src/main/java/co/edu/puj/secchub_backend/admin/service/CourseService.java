package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.CourseDTO;
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
public class CourseService {
    private final ModelMapper modelMapper;
    private final CourseRepository courseRepository;
    private final SectionService sectionService;

    /**
     * Creates a new course.
     * @param courseDTO dto with course data
     * @return Created course
     */
    public Mono<CourseDTO> createCourse(CourseDTO courseDTO) {
        return Mono.fromCallable(() -> {
            if (courseDTO.getSectionId() != null) {
                sectionService.findSectionById(courseDTO.getSectionId()).block();
            }
            
            Course course = modelMapper.map(courseDTO, Course.class);
            Course saved = courseRepository.save(course);
            return modelMapper.map(saved, CourseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }    
    
    /**
     * Lists all existing courses.
     * @return List of courses
     */
    public List<CourseDTO> findAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    /**
     * Get a course by its ID.
     * @param courseId Course ID
     * @return Course with the given ID
     */
    public Mono<CourseDTO> findCourseById(Long courseId) {
        return Mono.fromCallable(() -> {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found for consult: " + courseId));
            return modelMapper.map(course, CourseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates a course by its ID.
     * @param courseId Course ID
     * @param courseDTO with updated data
     * @return Updated course
     */
    public Mono<CourseDTO> updateCourse(Long courseId, CourseDTO courseDTO) {
        return Mono.fromCallable(() -> {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found for update: " + courseId));

            // Validate that the section exists if sectionId is being updated
            if (courseDTO.getSectionId() != null) {
                sectionService.findSectionById(courseDTO.getSectionId()).block();
            }

            modelMapper.getConfiguration().setPropertyCondition(context ->
                    context.getSource() != null);
            modelMapper.map(courseDTO, course);

            return modelMapper.map(courseRepository.save(course), CourseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<CourseDTO> patchCourse(Long id, Map<String, Object> updates) {
        return Mono.fromCallable(() -> {
            Course course = courseRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found with id=" + id));

            modelMapper.map(updates, course);

            return modelMapper.map(courseRepository.save(course), CourseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
