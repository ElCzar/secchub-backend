package co.edu.puj.secchub_backend.planning.controller;

import co.edu.puj.secchub_backend.planning.dto.*;
import co.edu.puj.secchub_backend.planning.service.TeachingAssistantService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

/**
 * REST controller for managing Teaching Assistant assignments.
 * Provides endpoints to create, query, update and delete teaching assistant assignments and their schedules.
 */
@RestController
@RequestMapping("/teaching-assistants")
@RequiredArgsConstructor
public class TeachingAssistantController {

        private final TeachingAssistantService teachingAssistantService;

        /**
         * Creates a new teaching assistant assignment.
         * @param teachingAssistantRequestDTO DTO with assignment data
         * @return TeachingAssistantResponseDTO with the created assignment
         */
        @PostMapping
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<TeachingAssistantResponseDTO>> createTeachingAssistant(@RequestBody TeachingAssistantRequestDTO teachingAssistantRequestDTO) {
        return teachingAssistantService.createTeachingAssistant(teachingAssistantRequestDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
        }

        /**
         * Updates an existing teaching assistant assignment.
         * @param id Teaching assistant ID
         * @param teachingAssistantRequestDTO DTO with updated data
         * @return TeachingAssistantResponseDTO with the updated assignment
         */
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<TeachingAssistantResponseDTO>> updateTeachingAssistant(
                @PathVariable Long id,
                @RequestBody TeachingAssistantRequestDTO teachingAssistantRequestDTO) {
        return teachingAssistantService.updateTeachingAssistant(id, teachingAssistantRequestDTO)
                .map(ResponseEntity::ok);
        }

        /**
         * Deletes a teaching assistant assignment.
         * @param id Teaching assistant ID
         * @return Response with ok status
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<Void>> deleteTeachingAssistant(@PathVariable Long id) {
        return teachingAssistantService.deleteTeachingAssistant(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
        }

        /**
         * Gets a teaching assistant assignment by its ID.
         * @param id Teaching assistant ID
         * @return TeachingAssistantResponseDTO with the found assignment
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<TeachingAssistantResponseDTO>> getTeachingAssistantById(@PathVariable Long id) {
        return teachingAssistantService.findTeachingAssistantById(id)
                .map(ResponseEntity::ok);
        }

        /**
         * Gets teaching assistant assignments by student application ID.
         * @param studentApplicationId Student application ID
         * @return List of TeachingAssistantResponseDTO for the student application
         */
        @GetMapping("/student-application/{studentApplicationId}")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<List<TeachingAssistantResponseDTO>>> getTeachingAssistantsByStudentApplication(
                @PathVariable Long studentApplicationId) {
        return teachingAssistantService.findByStudentApplicationId(studentApplicationId)
                .map(ResponseEntity::ok);
        }

        /**
         * Gets all teaching assistant assignments for the current semester.
         * @return List of TeachingAssistantResponseDTO for the current semester
         */
        @GetMapping("/current-semester")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<List<TeachingAssistantResponseDTO>>> getCurrentSemesterTeachingAssistants() {
        return teachingAssistantService.listCurrentSemesterTeachingAssistants()
                .map(ResponseEntity::ok);
        }

        /**
         * Gets all teaching assistant assignments.
         * @return List of TeachingAssistantResponseDTO
         */
        @GetMapping
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<List<TeachingAssistantResponseDTO>>> getAllTeachingAssistants() {
        return teachingAssistantService.listAllTeachingAssistants()
                .map(ResponseEntity::ok);
        }

        /**
         * Creates a new schedule for a teaching assistant.
         * @param teachingAssistantId Teaching assistant ID
         * @param scheduleRequestDTO DTO with schedule data
         * @return TeachingAssistantScheduleResponseDTO with the created schedule
         */
        @PostMapping("/{teachingAssistantId}/schedules")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<TeachingAssistantScheduleResponseDTO>> createSchedule(
                @PathVariable Long teachingAssistantId,
                @RequestBody TeachingAssistantScheduleRequestDTO scheduleRequestDTO) {
        return teachingAssistantService.createSchedule(teachingAssistantId, scheduleRequestDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
        }

        /**
         * Updates an existing teaching assistant schedule.
         * @param scheduleId Schedule ID
         * @param scheduleRequestDTO DTO with updated schedule data
         * @return TeachingAssistantScheduleResponseDTO with the updated schedule
         */
        @PutMapping("/schedules/{scheduleId}")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<TeachingAssistantScheduleResponseDTO>> updateSchedule(
                @PathVariable Long scheduleId,
                @RequestBody TeachingAssistantScheduleRequestDTO scheduleRequestDTO) {
        return teachingAssistantService.updateSchedule(scheduleId, scheduleRequestDTO)
                .map(ResponseEntity::ok);
        }

        /**
         * Deletes a teaching assistant schedule.
         * @param scheduleId Schedule ID
         * @return Response with ok status
         */
        @DeleteMapping("/schedules/{scheduleId}")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<Void>> deleteSchedule(@PathVariable Long scheduleId) {
        return teachingAssistantService.deleteSchedule(scheduleId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
        }

        /**
         * Generates payroll for teaching assistants (placeholder endpoint).
         * @return Response with ok status
         */
        @PostMapping("/payroll")
        @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
        public Mono<ResponseEntity<Void>> generatePayroll() {
        return teachingAssistantService.generatePayroll()
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
        }
}