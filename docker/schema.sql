-- Target: MySQL 8.4.x (LTS)
-- =========================
-- Parametric lookup tables
-- =========================

CREATE TABLE `status` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `document_type` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `employment_type` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `modality` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `classroom_type` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============
-- Core tables
-- ============

CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(150) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `faculty` VARCHAR(150) NULL,
  `name` VARCHAR(150) NOT NULL,
  `last_name` VARCHAR(150) NOT NULL,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `status_id` BIGINT UNSIGNED NULL,
  `last_access` DATETIME NULL,
  `role_id` BIGINT UNSIGNED NULL,
  `document_type_id` BIGINT UNSIGNED NULL,
  `document_number` VARCHAR(50) NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_users_status`
    FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_users_role`
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_users_document_type`
    FOREIGN KEY (`document_type_id`) REFERENCES `document_type` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `teacher` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NULL,
  `employment_type_id` BIGINT UNSIGNED NULL,
  `max_hours` INT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_teacher_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_teacher_employment_type`
    FOREIGN KEY (`employment_type_id`) REFERENCES `employment_type` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `section` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NULL,
  `name` VARCHAR(150) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_section_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `course` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `section_id` BIGINT UNSIGNED NULL,
  `name` VARCHAR(200) NOT NULL,
  `credits` INT NULL,
  `description` TEXT NULL,
  `requirement` VARCHAR(200) NULL,
  `is_valid` BOOLEAN NULL,
  `recommendation` TEXT NULL,
  `status_id` BIGINT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_course_section`
    FOREIGN KEY (`section_id`) REFERENCES `section` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_course_status`
    FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `semester` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `period` INT NULL,
  `year` INT NULL,
  `is_current` BOOLEAN NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `classroom` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `classroom_type_id` BIGINT UNSIGNED NULL,
  `campus` VARCHAR(150) NULL,
  `location` VARCHAR(200) NULL,
  `room` VARCHAR(100) NULL,
  `capacity` INT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_classroom_classroom_type`
    FOREIGN KEY (`classroom_type_id`) REFERENCES `classroom_type` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `student_application` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NULL,
  `course_id` BIGINT UNSIGNED NULL,
  `section_id` BIGINT UNSIGNED NULL,
  `semester_id` BIGINT UNSIGNED NULL,
  `program` VARCHAR(150) NULL,
  `student_semester` INT NULL,
  `academic_average` DECIMAL(4,2) NULL,
  `phone_number` VARCHAR(50) NULL,
  `alternate_phone_number` VARCHAR(50) NULL,
  `address` VARCHAR(255) NULL,
  `personal_email` VARCHAR(255) NULL,
  `was_teaching_assistant` BOOLEAN NULL,
  `course_average` DECIMAL(4,2) NULL,
  `course_teacher` VARCHAR(150) NULL,
  `application_date` DATE NULL,
  `status_id` BIGINT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_student_application_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_student_application_course`
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_student_application_section`
    FOREIGN KEY (`section_id`) REFERENCES `section` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_student_application_semester`
    FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_student_application_status`
    FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `academic_request` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NULL,
  `course_id` BIGINT UNSIGNED NULL,
  `semester_id` BIGINT UNSIGNED NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  `capacity` INT NULL,
  `request_date` DATE NULL,
  `observation` TEXT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_academic_request_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_academic_request_course`
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_academic_request_semester`
    FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `request_schedule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `academic_request_id` BIGINT UNSIGNED NULL,
  `classroom_type_id` BIGINT UNSIGNED NULL,
  `start_time` TIME NULL,
  `end_time` TIME NULL,
  `day` VARCHAR(20) NULL,
  `modality_id` BIGINT UNSIGNED NULL,
  `disability` BOOLEAN NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_request_schedule_academic_request`
    FOREIGN KEY (`academic_request_id`) REFERENCES `academic_request` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_request_schedule_classroom_type`
    FOREIGN KEY (`classroom_type_id`) REFERENCES `classroom_type` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_request_schedule_modality`
    FOREIGN KEY (`modality_id`) REFERENCES `modality` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `class` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `section` BIGINT UNSIGNED NULL,
  `course_id` BIGINT UNSIGNED NULL,
  `semester_id` BIGINT UNSIGNED NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  `observation` TEXT NULL,
  `capacity` INT NULL,
  `status_id` BIGINT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_class_course`
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_class_semester`
    FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_class_status`
    FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `class_schedule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NULL,
  `classroom_id` BIGINT UNSIGNED NULL,
  `day` VARCHAR(20) NULL,
  `start_time` TIME NULL,
  `end_time` TIME NULL,
  `modality_id` BIGINT UNSIGNED NULL,
  `disability` BOOLEAN NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_class_schedule_class`
    FOREIGN KEY (`class_id`) REFERENCES `class` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_class_schedule_classroom`
    FOREIGN KEY (`classroom_id`) REFERENCES `classroom` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_class_schedule_modality`
    FOREIGN KEY (`modality_id`) REFERENCES `modality` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `teaching_assistant` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT UNSIGNED NULL,
  `student_application_id` BIGINT UNSIGNED NULL,
  `weekly_hours` INT NULL,
  `weeks` INT NULL,
  `total_hours` INT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_teaching_assistant_class`
    FOREIGN KEY (`class_id`) REFERENCES `class` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_teaching_assistant_student_application`
    FOREIGN KEY (`student_application_id`) REFERENCES `student_application` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `teaching_assistant_schedule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `teaching_assistant_id` BIGINT UNSIGNED NULL,
  `day` VARCHAR(20) NULL,
  `start_time` TIME NULL,
  `end_time` TIME NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_teaching_assistant_schedule_teaching_assistant`
    FOREIGN KEY (`teaching_assistant_id`) REFERENCES `teaching_assistant` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `teacher_class` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `semester_id` BIGINT UNSIGNED NULL,
  `teacher_id` BIGINT UNSIGNED NULL,
  `class_id` BIGINT UNSIGNED NULL,
  `work_hours` INT NULL,
  `full_time_extra_hours` INT NULL,
  `adjunct_extra_hours` INT NULL,
  `decision` BOOLEAN NULL,
  `observation` TEXT NULL,
  `status_id` BIGINT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_teacher_class_semester`
    FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_teacher_class_teacher`
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_teacher_class_class`
    FOREIGN KEY (`class_id`) REFERENCES `class` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_teacher_class_status`
    FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `student_application_schedule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `student_application_id` BIGINT UNSIGNED NULL,
  `day` VARCHAR(20) NULL,
  `start_time` TIME NULL,
  `end_time` TIME NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_student_application_schedule_student_application`
    FOREIGN KEY (`student_application_id`) REFERENCES `student_application` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- Other auxiliary tables
-- =========================
CREATE TABLE `email_template` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NOT NULL,
  `subject` VARCHAR(255) NOT NULL,
  `body` TEXT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
