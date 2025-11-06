-- ============================================
-- Test Email Templates Data - Insert sample email templates
-- ============================================
-- Purpose: Provides test email templates for integration testing
--
-- Common Test Data:
-- - Name: Unique identifier for the template
-- - Subject: Email subject line with optional placeholders
-- - Body: Email body content with optional placeholders like {name}, {email}, etc.
-- ============================================

-- ==================
-- Drop existing data to avoid conflicts
-- ==================
DELETE FROM email_template;

-- ==================
-- 1. Welcome Email Template
-- ==================
INSERT INTO email_template (id, name, subject, body)
VALUES (1, 'welcome_email', 'Welcome to SecHub', 
'Hello {name},

Welcome to SecHub! We are excited to have you on board.

Your account has been successfully created with the following details:
Email: {email}

Please log in to your account to get started.

Best regards,
The SecHub Team
secchubnoreply@gmail.com');

-- ==================
-- 2. Password Reset Template
-- ==================
INSERT INTO email_template (id, name, subject, body)
VALUES (2, 'password_reset', 'SecHub - Password Reset Request', 
'Hello {name},

We received a request to reset your password for your SecHub account.

If you made this request, please click the link below to reset your password:
{reset_link}

If you did not request a password reset, please ignore this email or contact support if you have concerns.

This link will expire in 24 hours.

Best regards,
The SecHub Team
secchubnoreply@gmail.com');

-- ==================
-- 3. Class Assignment Notification Template
-- ==================
INSERT INTO email_template (id, name, subject, body)
VALUES (3, 'class_assignment', 'New Class Assignment - {course_name}', 
'Hello {teacher_name},

You have been assigned to teach the following class:

Course: {course_name}
Section: {section}
Semester: {semester}
Schedule: {schedule}

Please review the assignment and confirm your availability through the SecHub portal.

If you have any questions, please contact the academic coordinator.

Best regards,
The SecHub Team
secchubnoreply@gmail.com');

-- ============================================
-- Usage Notes:
-- ============================================
-- 1. Template IDs are pre-defined to avoid conflicts in parameterized tests
-- 2. Template names are unique identifiers used to retrieve templates
-- 3. Placeholders in curly braces {} can be replaced with actual values:
--    - {name}: User's name
--    - {email}: User's email address
--    - {reset_link}: Password reset link
--    - {teacher_name}: Teacher's name
--    - {course_name}: Course name
--    - {section}: Section name
--    - {semester}: Semester name
--    - {schedule}: Class schedule
-- 4. All templates use secchubnoreply@gmail.com as the sender
-- 5. Load order: test-email-templates.sql (no dependencies)
-- ============================================
