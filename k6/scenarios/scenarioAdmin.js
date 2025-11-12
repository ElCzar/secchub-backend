import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// Custom metrics for Admin module
const adminErrorRate = new Rate('admin_errors');
const courseOperationDuration = new Trend('admin_course_duration_ms', true);
const teacherOperationDuration = new Trend('admin_teacher_duration_ms', true);
const sectionOperationDuration = new Trend('admin_section_duration_ms', true);
const semesterOperationDuration = new Trend('admin_semester_duration_ms', true);
const registerOperationDuration = new Trend('admin_register_duration_ms', true);

// Mock data for creating resources
const mockCourses = new SharedArray('courses', function () {
  return [
    { name: 'Advanced Algorithms', credits: 3, sectionId: 1 },
    { name: 'Database Systems', credits: 4, sectionId: 2 },
    { name: 'Software Engineering', credits: 3, sectionId: 3 },
    { name: 'Computer Networks', credits: 3, sectionId: 1 },
    { name: 'Artificial Intelligence', credits: 4, sectionId: 2 },
    { name: 'Operating Systems', credits: 4, sectionId: 1 },
    { name: 'Web Development', credits: 3, sectionId: 3 },
    { name: 'Mobile Computing', credits: 3, sectionId: 2 },
  ];
});

/**
 * Utility function for weighted random selection within Admin module
 */
function weightedRandom(items) {
  const totalWeight = items.reduce((sum, item) => sum + item.weight, 0);
  let random = Math.random() * totalWeight;
  
  for (const item of items) {
    random -= item.weight;
    if (random <= 0) return item;
  }
  return items[0];
}

/**
 * Generate unique identifier for test data
 */
function generateUniqueId() {
  return `${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

// ====================
// COURSE OPERATIONS
// ====================

function courseOperations(token, baseUrl, data) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Admin - Course CRUD Cycle', () => {
    let courseId;
    
    // CREATE Course
    const courseData = mockCourses[Math.floor(Math.random() * mockCourses.length)];
    const uniqueCourse = {
      ...courseData,
      name: `${courseData.name} ${generateUniqueId()}`,
    };
    
    let response = http.post(
      `${baseUrl}/courses`,
      JSON.stringify(uniqueCourse),
      { headers, tags: { operation: 'course_create' } }
    );
    
    courseOperationDuration.add(response.timings.duration);
    
    const createSuccess = check(response, {
      'course created (201)': (r) => r.status === 201,
      'course has id': (r) => r.json('id') !== undefined,
      'course has name': (r) => r.json('name') !== undefined,
    });
    
    adminErrorRate.add(!createSuccess);
    
    if (createSuccess) {
      courseId = response.json('id');
      data.createdResourceIds.courses.push(courseId);
    }
    
    sleep(0.1);
    
    // GET All Courses
    response = http.get(
      `${baseUrl}/courses`,
      { headers, tags: { operation: 'course_get_all' } }
    );
    
    courseOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get all courses (200)': (r) => r.status === 200,
      'courses list is array': (r) => Array.isArray(r.json()),
    });
    
    sleep(0.1);
    
    if (courseId) {
      // GET Course by ID
      response = http.get(
        `${baseUrl}/courses/${courseId}`,
        { headers, tags: { operation: 'course_get_by_id' } }
      );
      
      courseOperationDuration.add(response.timings.duration);
      
      check(response, {
        'get course by id (200)': (r) => r.status === 200,
        'course id matches': (r) => r.json('id') === courseId,
      });
      
      sleep(0.1);
      
      // PATCH Course
      const patchData = { credits: 4 };
      response = http.patch(
        `${baseUrl}/courses/${courseId}`,
        JSON.stringify(patchData),
        { headers, tags: { operation: 'course_patch' } }
      );
      
      courseOperationDuration.add(response.timings.duration);
      
      check(response, {
        'course patched (200)': (r) => r.status === 200,
        'credits updated': (r) => r.json('credits') === 4,
      });
      
      sleep(0.1);
      
      // GET Course again to verify PATCH
      response = http.get(
        `${baseUrl}/courses/${courseId}`,
        { headers, tags: { operation: 'course_get_after_patch' } }
      );
      
      courseOperationDuration.add(response.timings.duration);
      
      check(response, {
        'get patched course (200)': (r) => r.status === 200,
        'patch persisted': (r) => r.json('credits') === 4,
      });
      
      sleep(0.1);
      
      // DELETE Course
      response = http.del(
        `${baseUrl}/courses/${courseId}`,
        null,
        { headers, tags: { operation: 'course_delete' } }
      );
      
      courseOperationDuration.add(response.timings.duration);
      
      check(response, {
        'course deleted (204)': (r) => r.status === 204,
      });
    }
  });
}

// ====================
// TEACHER OPERATIONS
// ====================

function teacherOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Admin - Teacher Operations Cycle', () => {
    // GET All Teachers
    let response = http.get(
      `${baseUrl}/teachers`,
      { headers, tags: { operation: 'teacher_get_all' } }
    );
    
    teacherOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get all teachers (200)': (r) => r.status === 200,
      'teachers list is array': (r) => Array.isArray(r.json()),
    });
    
    sleep(0.1);
    
    // GET Teacher by ID (using existing teacher from mock data)
    const teacherId = Math.floor(Math.random() * 5) + 1; // IDs 1-5
    response = http.get(
      `${baseUrl}/teachers/${teacherId}`,
      { headers, tags: { operation: 'teacher_get_by_id' } }
    );
    
    teacherOperationDuration.add(response.timings.duration);
    
    const teacherFound = check(response, {
      'get teacher by id (200 or 404)': (r) => r.status === 200 || r.status === 404,
    });
    
    sleep(0.3);
    
    if (teacherFound && response.status === 200) {
      const currentTeacher = response.json();
      
      // PATCH Teacher (using PUT endpoint with updated maxHours)
      const updateData = {
        employmentTypeId: currentTeacher.employmentTypeId,
        maxHours: Math.floor(Math.random() * 20) + 35, // 35-54 hours
      };
      
      response = http.put(
        `${baseUrl}/teachers/${teacherId}`,
        JSON.stringify(updateData),
        { headers, tags: { operation: 'teacher_update' } }
      );
      
      teacherOperationDuration.add(response.timings.duration);
      
      check(response, {
        'teacher updated (200)': (r) => r.status === 200,
        'max hours updated': (r) => r.json('maxHours') === updateData.maxHours,
      });
      
      sleep(0.3);
      
      // GET Teacher again to verify update
      response = http.get(
        `${baseUrl}/teachers/${teacherId}`,
        { headers, tags: { operation: 'teacher_get_after_update' } }
      );
      
      teacherOperationDuration.add(response.timings.duration);
      
      check(response, {
        'get updated teacher (200)': (r) => r.status === 200,
        'update persisted': (r) => r.json('maxHours') === updateData.maxHours,
      });
    }
    
    sleep(0.1);
    
    // GET Teachers by Employment Type
    const employmentTypeId = Math.floor(Math.random() * 2) + 1; // 1 or 2
    response = http.get(
      `${baseUrl}/teachers/employment-type/${employmentTypeId}`,
      { headers, tags: { operation: 'teacher_get_by_employment' } }
    );
    
    teacherOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get teachers by employment (200)': (r) => r.status === 200,
      'filtered teachers is array': (r) => Array.isArray(r.json()),
    });
  });
}

// ====================
// SECTION OPERATIONS
// ====================

function sectionOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Admin - Section Operations Cycle', () => {
    // GET All Sections
    let response = http.get(
      `${baseUrl}/sections`,
      { headers, tags: { operation: 'section_get_all' } }
    );
    
    sectionOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get all sections (200)': (r) => r.status === 200,
      'sections list is array': (r) => Array.isArray(r.json()),
    });
    
    sleep(0.1);
    
    // GET Section by ID
    const sectionId = Math.floor(Math.random() * 3) + 1; // IDs 1-3
    response = http.get(
      `${baseUrl}/sections/${sectionId}`,
      { headers, tags: { operation: 'section_get_by_id' } }
    );
    
    sectionOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get section by id (200 or 404)': (r) => r.status === 200 || r.status === 404,
    });
    
    sleep(0.1);
    
    // GET Planning Status Stats
    response = http.get(
      `${baseUrl}/sections/planning-status-stats`,
      { headers, tags: { operation: 'section_get_stats' } }
    );
    
    sectionOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get planning stats (200)': (r) => r.status === 200,
      'stats have openCount': (r) => r.json('openCount') !== undefined,
      'stats have closedCount': (r) => r.json('closedCount') !== undefined,
    });
    
    sleep(0.1);
    
    // GET Sections Summary
    response = http.get(
      `${baseUrl}/sections/summary`,
      { headers, tags: { operation: 'section_get_summary' } }
    );
    
    sectionOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get sections summary (200)': (r) => r.status === 200,
      'summary list is array': (r) => Array.isArray(r.json()),
    });
  });
}

// ====================
// SEMESTER OPERATIONS
// ====================

function semesterOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Admin - Semester Operations Cycle', () => {
    // GET Current Semester
    let response = http.get(
      `${baseUrl}/semesters/current`,
      { headers, tags: { operation: 'semester_get_current' } }
    );
    
    semesterOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get current semester (200)': (r) => r.status === 200,
      'semester has id': (r) => r.json('id') !== undefined,
      'semester has year': (r) => r.json('year') !== undefined,
      'semester has period': (r) => r.json('period') !== undefined,
    });
    
    sleep(0.1);
    
    // GET All Semesters
    response = http.get(
      `${baseUrl}/semesters/all`,
      { headers, tags: { operation: 'semester_get_all' } }
    );
    
    semesterOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get all semesters (200)': (r) => r.status === 200,
      'semesters list is array': (r) => Array.isArray(r.json()),
    });
    
    sleep(0.1);
    
    // GET Semester by Year and Period
    response = http.get(
      `${baseUrl}/semesters?year=2025&period=10`,
      { headers, tags: { operation: 'semester_get_by_params' } }
    );
    
    semesterOperationDuration.add(response.timings.duration);
    
    check(response, {
      'get semester by params (200 or 404)': (r) => r.status === 200 || r.status === 404,
    });
  });
}

// ====================
// REGISTER OPERATIONS (Less Common - 5% weight)
// ====================

function registerOperations(token, baseUrl, data) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Admin - Register Operations', () => {
    const uniqueId = generateUniqueId();
    
    // Register Student
    const studentData = {
      username: `student_${uniqueId}`,
      password: 'password123',
      name: 'Test',
      lastName: 'Student',
      email: `student_${uniqueId}@secchub.com`,
      documentTypeId: 1,
      documentNumber: `DOC${uniqueId}`,
    };
    
    let response = http.post(
      `${baseUrl}/admin/register/student`,
      JSON.stringify(studentData),
      { headers, tags: { operation: 'register_student' } }
    );

    registerOperationDuration.add(response.timings.duration);
    
    const registerStudentSuccess = check(response, {
      'student registered (201)': (r) => r.status === 201,
      'student id returned': (r) => r.body !== '',
    });
    
    adminErrorRate.add(!registerStudentSuccess);
    
    if (registerStudentSuccess) {
      const studentId = Number.parseInt(response.body);
      data.createdResourceIds.students.push(studentId);
    }
    
    sleep(0.5);
    
    // Register Teacher
    const teacherData = {
      user: {
        username: `teacher_${uniqueId}`,
        password: 'password123',
        name: 'Test',
        lastName: 'Teacher',
        email: `teacher_${uniqueId}@secchub.com`,
        documentTypeId: 1,
        documentNumber: `TDOC${uniqueId}`,
      },
      employmentTypeId: Math.floor(Math.random() * 2) + 1,
      maxHours: 40,
    };
    
    response = http.post(
      `${baseUrl}/admin/register/teacher`,
      JSON.stringify(teacherData),
      { headers, tags: { operation: 'register_teacher' } }
    );
    
    registerOperationDuration.add(response.timings.duration);
    
    const registerTeacherSuccess = check(response, {
      'teacher registered (201)': (r) => r.status === 201,
      'teacher has id': (r) => r.json('id') !== undefined,
    });
    
    adminErrorRate.add(!registerTeacherSuccess);
    
    if (registerTeacherSuccess) {
      const teacherId = response.json('id');
      data.createdResourceIds.teachers.push(teacherId);
    }
  });
}

// ====================
// MAIN ADMIN SCENARIO FUNCTION
// ====================

/**
 * Main entry point for Admin module scenario
 * Distributes operations based on weights
 * @param {string} token - JWT authentication token
 * @param {string} baseUrl - Base URL of the API
 * @param {Object} data - Shared test data
 */
export default function (token, baseUrl, data) {
  // Weighted operation selection within Admin module
  // Course: 35%, Teacher: 25%, Section: 20%, Semester: 19%, Register: 1%
  const operation = weightedRandom([
    { weight: 35, name: 'course', fn: courseOperations },
    { weight: 25, name: 'teacher', fn: teacherOperations },
    { weight: 20, name: 'section', fn: sectionOperations },
    { weight: 19, name: 'semester', fn: semesterOperations },
    { weight: 1, name: 'register', fn: registerOperations },
  ]);
  
  // Execute selected operation
  operation.fn(token, baseUrl, data);
}
