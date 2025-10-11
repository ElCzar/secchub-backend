package co.edu.puj.secchub_backend.planning.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.planning.dto.ClassCreateRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleResponseDTO;
import co.edu.puj.secchub_backend.planning.exception.ClassNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.ClassScheduleNotFoundException;
import co.edu.puj.secchub_backend.planning.model.Class;
import co.edu.puj.secchub_backend.planning.model.ClassSchedule;
import co.edu.puj.secchub_backend.planning.repository.ClassRepository;
import co.edu.puj.secchub_backend.planning.repository.ClassScheduleRepository;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling planning-related operations.
 * This class manages the core business logic for the planning module.
 */
@Service
@RequiredArgsConstructor
public class PlanningService {
    private final ModelMapper modelMapper;
    private final ClassRepository classRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final AdminModuleSemesterContract semesterService;

    /**
     * Creates a new class.
     * @param classCreateRequestDTO DTO with class information
     * @return Created class DTO
     */
    @Transactional
    public Mono<ClassResponseDTO> createClass(ClassCreateRequestDTO classCreateRequestDTO) {
        return Mono.fromCallable(() -> {
            Long currentSemesterId = semesterService.getCurrentSemesterId();
            
            Class classEntity = modelMapper.map(classCreateRequestDTO, Class.class);
            classEntity.setSemesterId(currentSemesterId);
            
            Class savedClass = classRepository.save(classEntity);
            return mapToResponseDTO(savedClass);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets all classes for the current semester.
     * @return List of classes for the current semester
     */
    public List<ClassResponseDTO> findCurrentSemesterClasses() {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return classRepository.findBySemesterId(currentSemesterId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets all classes.
     * @return List of all classes
     */
    public List<ClassResponseDTO> findAllClasses() {
        return classRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets a class by ID.
     * @param classId Class ID
     * @return Class found
     */
    public Mono<ClassResponseDTO> findClassById(Long classId) {
        return Mono.fromCallable(() -> classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException("Class not found for retrieval with id: " + classId)))
                .map(this::mapToResponseDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates a class.
     * @param classId Class ID
     * @param classCreateRequestDTO DTO with updated data
     * @return Updated class
     */
    public Mono<ClassResponseDTO> updateClass(Long classId, ClassCreateRequestDTO classCreateRequestDTO) {
        return Mono.fromCallable(() -> {
            Class classEntity = classRepository.findById(classId)
                    .orElseThrow(() -> new ClassNotFoundException("Class not found for update with id: " + classId));

            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(classCreateRequestDTO, classEntity);
            
            Class savedClass = classRepository.save(classEntity);
            return mapToResponseDTO(savedClass);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a class by ID.
     * @param classId Class ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteClass(Long classId) {
        return Mono.fromCallable(() -> {
            if (!classRepository.existsById(classId)) {
                throw new ClassNotFoundException("Class not found for deletion with id: " + classId);
            }
            classRepository.deleteById(classId);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Gets classes by course ID.
     * @param courseId Course ID
     * @return List of classes for the specified course
     */
    public List<ClassResponseDTO> findClassesByCourse(Long courseId) {
        return classRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets classes by section.
     * @param section Section number
     * @return List of classes for the specified section
     */
    public List<ClassResponseDTO> findClassesBySection(Long section) {
        return classRepository.findBySection(section).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets classes by semester and course for the current semester.
     * @param courseId Course ID
     * @return List of classes for the current semester and specified course
     */
    public List<ClassResponseDTO> findCurrentSemesterClassesByCourse(Long courseId) {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return classRepository.findBySemesterIdAndCourseId(currentSemesterId, courseId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Adds a schedule to a class.
     * @param classId Class ID
     * @param classScheduleRequestDTO DTO with schedule data
     * @return Created schedule DTO
     */
    public Mono<ClassScheduleResponseDTO> addClassSchedule(Long classId, ClassScheduleRequestDTO classScheduleRequestDTO) {
        return Mono.fromCallable(() -> {
            classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException("Class not found for retrieval with id: " + classId));

            ClassSchedule schedule = modelMapper.map(classScheduleRequestDTO, ClassSchedule.class);
            schedule.setClassId(classId);

            ClassSchedule savedSchedule = classScheduleRepository.save(schedule);
            return modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets schedules associated with a class.
     * @param classId Class ID
     * @return List of schedules
     */
    public List<ClassScheduleResponseDTO> findClassSchedulesByClassId(Long classId) {
        classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException("Class not found for retrieval with id: " + classId));

        List<ClassSchedule> classSchedules = classScheduleRepository.findByClassId(classId);
        return classSchedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Gets a class schedule by ID.
     * @param scheduleId Schedule ID
     * @return Class schedule found
     */
    public Mono<ClassScheduleResponseDTO> findClassScheduleById(Long scheduleId) {
        return Mono.fromCallable(() -> classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ClassScheduleNotFoundException("Class schedule not found for retrieval with id: " + scheduleId)))
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates a specific class schedule.
     * @param scheduleId Schedule ID
     * @param classScheduleRequestDTO DTO with updated data
     * @return Updated schedule
     */
    public Mono<ClassScheduleResponseDTO> updateClassSchedule(Long scheduleId, ClassScheduleRequestDTO classScheduleRequestDTO) {
        return Mono.fromCallable(() -> {
            ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new ClassScheduleNotFoundException("Class schedule not found for update with id: " + scheduleId));

            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(classScheduleRequestDTO, schedule);

            ClassSchedule savedSchedule = classScheduleRepository.save(schedule);
            return modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a class schedule by ID.
     * @param scheduleId Schedule ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteClassSchedule(Long scheduleId) {
        return Mono.fromCallable(() -> {
            if (!classScheduleRepository.existsById(scheduleId)) {
                throw new ClassScheduleNotFoundException("Class schedule not found for deletion with id: " + scheduleId);
            }
            classScheduleRepository.deleteById(scheduleId);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Partially updates a class schedule.
     * @param scheduleId Schedule ID
     * @param updates Map with fields to update
     * @return Updated schedule
     */
    public Mono<ClassScheduleResponseDTO> patchClassSchedule(Long scheduleId, Map<String, Object> updates) {
        return Mono.fromCallable(() -> {
            ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new ClassScheduleNotFoundException("Class schedule not found for retrieval with id: " + scheduleId));

            ClassScheduleResponseDTO updateDTO = new ClassScheduleResponseDTO();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "startTime" -> {
                        if (value instanceof String stringValue) {
                            updateDTO.setStartTime(LocalTime.parse(stringValue));
                        } else if (value instanceof LocalTime localTimeValue) {
                            updateDTO.setStartTime(localTimeValue);
                        }
                    }
                    case "endTime" -> {
                        if (value instanceof String stringValue) {
                            updateDTO.setEndTime(LocalTime.parse(stringValue));
                        } else if (value instanceof LocalTime localTimeValue) {
                            updateDTO.setEndTime(localTimeValue);
                        }
                    }
                    case "day" -> updateDTO.setDay((String) value);
                    case "classroomId" -> updateDTO.setClassroomId((Long) value);
                    case "modalityId" -> updateDTO.setModalityId((Long) value);
                    case "disability" -> updateDTO.setDisability((Boolean) value);
                    default -> {}
                }
            });

            modelMapper.map(updateDTO, schedule);
            ClassSchedule savedSchedule = classScheduleRepository.save(schedule);
            return modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets schedules by classroom ID.
     * @param classroomId Classroom ID
     * @return List of schedules for the specified classroom
     */
    public List<ClassScheduleResponseDTO> findClassSchedulesByClassroom(Long classroomId) {
        List<ClassSchedule> schedules = classScheduleRepository.findByClassroomId(classroomId);
        return schedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Gets schedules by day.
     * @param day Day of the week
     * @return List of schedules for the specified day
     */
    public List<ClassScheduleResponseDTO> findClassSchedulesByDay(String day) {
        List<ClassSchedule> schedules = classScheduleRepository.findByDay(day);
        return schedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Gets schedules with disability accommodations.
     * @param disability True to find schedules with disability accommodations
     * @return List of schedules with disability considerations
     */
    public List<ClassScheduleResponseDTO> findClassSchedulesByDisability(Boolean disability) {
        List<ClassSchedule> schedules = classScheduleRepository.findByDisability(disability);
        return schedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
    }
    
    private ClassResponseDTO mapToResponseDTO(Class classEntity) {
        ClassResponseDTO responseDTO = modelMapper.map(classEntity, ClassResponseDTO.class);

        List<ClassSchedule> schedules = classScheduleRepository.findByClassId(classEntity.getId());
        List<ClassScheduleResponseDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
        
        responseDTO.setSchedules(scheduleDTOs);
        return responseDTO;
    }
}
