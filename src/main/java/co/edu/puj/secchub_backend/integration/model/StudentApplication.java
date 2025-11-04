package co.edu.puj.secchub_backend.integration.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity mapped to 'student_application'.
 * Represents a student's application to be a teaching assistant (academic or administrative).
 */
@Table("student_application")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentApplication {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("course_id")
    private Long courseId;

    @Column("section_id")
    private Long sectionId;

    @Column("semester_id")
    private Long semesterId;

    @Column("program")
    private String program;

    @Column("student_semester")
    private Integer studentSemester;

    @Column("academic_average")
    private Double academicAverage;

    @Column("phone_number")
    private String phoneNumber;

    @Column("alternate_phone_number")
    private String alternatePhoneNumber;

    @Column("address")
    private String address;

    @Column("personal_email")
    private String personalEmail;

    @Column("was_teaching_assistant")
    private Boolean wasTeachingAssistant;

    @Column("course_average")
    private Double courseAverage;

    @Column("course_teacher")
    private String courseTeacher;

    @Column("application_date")
    private LocalDate applicationDate;

    @Column("status_id")
    private Long statusId;
}
