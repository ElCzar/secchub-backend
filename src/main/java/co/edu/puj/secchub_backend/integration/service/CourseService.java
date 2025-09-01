package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.dto.CourseDTO;
import co.edu.puj.secchub_backend.integration.model.Course;
import co.edu.puj.secchub_backend.integration.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing courses (HU04).
 * Provides CRUD operations for asignaturas.
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseDTO create(CourseDTO dto) {
        Course course = Course.builder()
                .name(dto.getName())
                .credits(dto.getCredits())
                .description(dto.getDescription())
                .isValid(dto.getIsValid())
                .sectionId(dto.getSectionId())
                .build();
        Course saved = courseRepository.save(course);
        return toDTO(saved);
    }

    public List<CourseDTO> findAll() {
        return courseRepository.findAll().stream().map(this::toDTO).toList();
    }

    public CourseDTO findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id=" + id));
        return toDTO(course);
    }

    public CourseDTO update(Long id, CourseDTO dto) {
    Course course = courseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Course not found with id=" + id));

    if (dto.getName() != null) course.setName(dto.getName());
    if (dto.getCredits() != null) course.setCredits(dto.getCredits());
    if (dto.getDescription() != null) course.setDescription(dto.getDescription());
    if (dto.getIsValid() != null) course.setIsValid(dto.getIsValid());
    if (dto.getSectionId() != null) course.setSectionId(dto.getSectionId());

    return toDTO(courseRepository.save(course));
}

    @Transactional
    public CourseDTO patch(Long id, Map<String, Object> updates) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id=" + id));

        if (updates.containsKey("name")) {
            course.setName((String) updates.get("name"));
        }
        if (updates.containsKey("credits")) {
            course.setCredits((Integer) updates.get("credits"));
        }
        if (updates.containsKey("description")) {
            course.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("isValid")) {
            course.setIsValid((Boolean) updates.get("isValid"));
        }
        if (updates.containsKey("sectionId")) {
            // Jackson puede mapear números como Integer o LinkedHashMap, mejor hacer cast largo
            Number sectionId = (Number) updates.get("sectionId");
            course.setSectionId(sectionId.longValue());
        }

        return toDTO(courseRepository.save(course));
    }


    private CourseDTO toDTO(Course c) {
        return CourseDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .credits(c.getCredits())
                .description(c.getDescription())
                .isValid(c.getIsValid())
                .sectionId(c.getSectionId())
                .build();
    }
}
