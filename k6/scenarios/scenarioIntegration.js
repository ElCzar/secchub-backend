import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// Custom metrics for Integration module
const integrationErrorRate = new Rate('integration_errors');
const academicRequestOperationDuration = new Trend('integration_academic_request_duration_ms', true);
const studentApplicationOperationDuration = new Trend('integration_student_application_duration_ms', true);
const teacherClassOperationDuration = new Trend('integration_teacher_class_duration_ms', true);

// Mock data for academic requests
const mockAcademicRequests = new SharedArray('academicRequests', function () {
  return [
    { courseId: 1, semesterId: 2, sections: [1, 2, 3], capacity: 40 },
    { courseId: 2, semesterId: 2, sections: [1, 2], capacity: 35 },
    { courseId: 3, semesterId: 2, sections: [1], capacity: 50 },
    { courseId: 4, semesterId: 2, sections: [1, 2], capacity: 35 },
    { courseId: 5, semesterId: 2, sections: [1], capacity: 30 },
  ];
});

// Mock data for student applications
const mockStudentApplications = new SharedArray('studentApplications', function () {
  return [
    { classId: 7, observation: 'Interested in Database Systems' },
    { classId: 9, observation: 'Software Engineering focus' },
    { classId: 11, observation: 'Networking specialization' },
    { classId: 12, observation: 'ML enthusiast' },
    { classId: 15, observation: 'OS fundamentals' },
  ];
});

// Role-specific users for authentication
const roleUsers = {
  admin: { email: 'admin@secchub.com', password: 'password' },
  teacher: { email: 'teacher@secchub.com', password: 'password' },
  student: { email: 'student@secchub.com', password: 'password' },
  program: { email: 'program@secchub.com', password: 'password' },
};

/**
 * Authenticate user and get token
 */
function authenticateUser(baseUrl, email, password) {
  const loginPayload = {
    email: email,
    password: password,
  };

  const loginResponse = http.post(
    `${baseUrl}/auth/login`,
    JSON.stringify(loginPayload),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { operation: 'integration_auth' }
    }
  );

  if (loginResponse.status === 200) {
    try {
      const body = JSON.parse(loginResponse.body);
      return body.accessToken;
    } catch (e) {
      console.error(`Failed to parse login response for ${email}: ${e.message}`);
      return null;
    }
  }
  console.error(`Failed to authenticate ${email}: ${loginResponse.status}`);
  return null;
}

/**
 * Utility function for weighted random selection
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
// ACADEMIC REQUEST OPERATIONS
// ====================

/**
 * Academic Request CRUD operations
 * Uses PROGRAM role for creation, ADMIN/USER for other operations
 */
function academicRequestOperations(adminToken, baseUrl) {
  // Get program token for creation
  const programToken = authenticateUser(baseUrl, roleUsers.program.email, roleUsers.program.password);
  
  const adminHeaders = {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json',
  };

  const tags = { module: 'integration', operation: 'academic_request' };
  let createdRequestIds = [];

  group('Academic Request Operations', () => {
    // 1. Create academic request batch (requires PROGRAM role)
    if (programToken) {
      group('Create Academic Request Batch', () => {
        const requestData = mockAcademicRequests[Math.floor(Math.random() * mockAcademicRequests.length)];
        
        const payload = {
          requests: [
            {
              courseId: requestData.courseId,
              semesterId: requestData.semesterId,
              sections: requestData.sections,
              capacity: requestData.capacity,
              schedules: [
                { day: 'Lunes', startTime: '08:00:00', endTime: '10:00:00', modalityId: 1 },
                { day: 'Miercoles', startTime: '08:00:00', endTime: '10:00:00', modalityId: 1 },
              ]
            }
          ]
        };

        const programHeaders = {
          'Authorization': `Bearer ${programToken}`,
          'Content-Type': 'application/json',
        };

        const createResponse = http.post(
          `${baseUrl}/academic-requests`,
          JSON.stringify(payload),
          { headers: programHeaders, tags: { ...tags, action: 'create' } }
        );

        academicRequestOperationDuration.add(createResponse.timings.duration);

        const createSuccess = check(createResponse, {
          'create request status is 201': (r) => r.status === 201,
          'create request returns array': (r) => {
            try {
              const body = JSON.parse(r.body);
              return Array.isArray(body);
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        if (createSuccess) {
          integrationErrorRate.add(0, tags);
          const responseBody = JSON.parse(createResponse.body);
          if (responseBody.length > 0) {
            createdRequestIds = responseBody.map(req => req.id);
          }
        } else {
          integrationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });
    }

    // 2. Get all academic requests (ADMIN/USER)
    group('Get All Academic Requests', () => {
      const getAllResponse = http.get(
        `${baseUrl}/academic-requests`,
        { headers: adminHeaders, tags: { ...tags, action: 'getAll' } }
      );

      academicRequestOperationDuration.add(getAllResponse.timings.duration);

      const getAllSuccess = check(getAllResponse, {
        'getAll requests status is 200': (r) => r.status === 200,
        'getAll requests returns array': (r) => {
          try {
            const body = JSON.parse(r.body);
            return Array.isArray(body);
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      if (getAllSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 3. Get current semester academic requests (ADMIN/USER)
    group('Get Current Semester Requests', () => {
      const getCurrentSemesterResponse = http.get(
        `${baseUrl}/academic-requests/current-semester`,
        { headers: adminHeaders, tags: { ...tags, action: 'getCurrentSemester' } }
      );

      academicRequestOperationDuration.add(getCurrentSemesterResponse.timings.duration);

      const getCurrentSemesterSuccess = check(getCurrentSemesterResponse, {
        'getCurrentSemester status is 200': (r) => r.status === 200,
      });

      if (getCurrentSemesterSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 4. Get requests by semester (ADMIN/USER/PROGRAM)
    group('Get Requests by Semester', () => {
      const getBySemesterResponse = http.get(
        `${baseUrl}/academic-requests/by-semester?semesterId=2`,
        { headers: adminHeaders, tags: { ...tags, action: 'getBySemester' } }
      );

      academicRequestOperationDuration.add(getBySemesterResponse.timings.duration);

      const getBySemesterSuccess = check(getBySemesterResponse, {
        'getBySemester status is 200': (r) => r.status === 200,
      });

      if (getBySemesterSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 5. Get request by ID
    if (createdRequestIds.length > 0) {
      const requestId = createdRequestIds[0];
      
      group('Get Request by ID', () => {
        const getByIdResponse = http.get(
          `${baseUrl}/academic-requests/${requestId}`,
          { headers: adminHeaders, tags: { ...tags, action: 'getById' } }
        );

        academicRequestOperationDuration.add(getByIdResponse.timings.duration);

        const getByIdSuccess = check(getByIdResponse, {
          'getById request status is 200': (r) => r.status === 200,
        });

        if (getByIdSuccess) {
          integrationErrorRate.add(0, tags);
        } else {
          integrationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });

      // 6. Update request (ADMIN/USER)
      group('Update Academic Request', () => {
        const updatePayload = {
          capacity: 45,
          observation: 'Updated capacity - Load Test'
        };

        const updateResponse = http.put(
          `${baseUrl}/academic-requests/${requestId}`,
          JSON.stringify(updatePayload),
          { headers: adminHeaders, tags: { ...tags, action: 'update' } }
        );

        academicRequestOperationDuration.add(updateResponse.timings.duration);

        const updateSuccess = check(updateResponse, {
          'update request status is 200': (r) => r.status === 200,
        });

        if (updateSuccess) {
          integrationErrorRate.add(0, tags);
        } else {
          integrationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });

      // 7. Get request schedules (ADMIN/USER)
      group('Get Request Schedules', () => {
        const getSchedulesResponse = http.get(
          `${baseUrl}/academic-requests/${requestId}/schedules`,
          { headers: adminHeaders, tags: { ...tags, action: 'getSchedules' } }
        );

        academicRequestOperationDuration.add(getSchedulesResponse.timings.duration);

        const getSchedulesSuccess = check(getSchedulesResponse, {
          'getSchedules status is 200': (r) => r.status === 200,
        });

        if (getSchedulesSuccess) {
          integrationErrorRate.add(0, tags);
        } else {
          integrationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });

      // 8. Delete request (ADMIN/USER)
      group('Delete Academic Request', () => {
        const deleteResponse = http.del(
          `${baseUrl}/academic-requests/${requestId}`,
          null,
          { headers: adminHeaders, tags: { ...tags, action: 'delete' } }
        );

        academicRequestOperationDuration.add(deleteResponse.timings.duration);

        const deleteSuccess = check(deleteResponse, {
          'delete request status is 204': (r) => r.status === 204,
        });

        if (deleteSuccess) {
          integrationErrorRate.add(0, tags);
        } else {
          integrationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });
    }
  });
}

// ====================
// STUDENT APPLICATION OPERATIONS
// ====================

/**
 * Student Application CRUD operations
 * Uses STUDENT role for creation, ADMIN/USER for queries and approvals
 */
function studentApplicationOperations(adminToken, baseUrl) {
  // Get student token for creation
  const studentToken = authenticateUser(baseUrl, roleUsers.student.email, roleUsers.student.password);
  
  const adminHeaders = {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json',
  };

  const tags = { module: 'integration', operation: 'student_application' };
  let createdApplicationId = null;

  group('Student Application Operations', () => {
    // 1. Create student application (requires STUDENT role - may return 200, 201 or 400)
    if (studentToken) {
      group('Create Student Application', () => {
        const appData = mockStudentApplications[Math.floor(Math.random() * mockStudentApplications.length)];
        
        const payload = {
          classId: appData.classId,
          observation: appData.observation
        };

        const studentHeaders = {
          'Authorization': `Bearer ${studentToken}`,
          'Content-Type': 'application/json',
        };

        const createResponse = http.post(
          `${baseUrl}/student-applications`,
          JSON.stringify(payload),
          { headers: studentHeaders, tags: { ...tags, action: 'create' } }
        );

        studentApplicationOperationDuration.add(createResponse.timings.duration);

        const createSuccess = check(createResponse, {
          'create application status is 200, 201 or 400': (r) => r.status === 200 || r.status === 201 || r.status === 400,
        });

        if (createSuccess) {
          integrationErrorRate.add(0, tags);
          if (createResponse.status === 201) {
            try {
              const responseBody = JSON.parse(createResponse.body);
              createdApplicationId = responseBody.id;
            } catch (e) {
              console.error('Failed to parse created application response ' + e.message);
            }
          }
        } else {
          integrationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });
    }

    // 2. Get all student applications (ADMIN/USER)
    group('Get All Student Applications', () => {
      const getAllResponse = http.get(
        `${baseUrl}/student-applications`,
        { headers: adminHeaders, tags: { ...tags, action: 'getAll' } }
      );

      studentApplicationOperationDuration.add(getAllResponse.timings.duration);

      const getAllSuccess = check(getAllResponse, {
        'getAll applications status is 200': (r) => r.status === 200,
        'getAll applications returns array': (r) => {
          try {
            const body = JSON.parse(r.body);
            return Array.isArray(body);
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      if (getAllSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 3. Get current semester applications (ADMIN/USER)
    group('Get Current Semester Applications', () => {
      const getCurrentSemesterResponse = http.get(
        `${baseUrl}/student-applications/current-semester`,
        { headers: adminHeaders, tags: { ...tags, action: 'getCurrentSemester' } }
      );

      studentApplicationOperationDuration.add(getCurrentSemesterResponse.timings.duration);

      const getCurrentSemesterSuccess = check(getCurrentSemesterResponse, {
        'getCurrentSemester applications status is 200': (r) => r.status === 200,
      });

      if (getCurrentSemesterSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 4. Get applications by status (ADMIN/USER)
    group('Get Applications by Status', () => {
      const statusId = Math.floor(Math.random() * 3) + 1; // Status 1-3
      
      const getByStatusResponse = http.get(
        `${baseUrl}/student-applications/status/${statusId}`,
        { headers: adminHeaders, tags: { ...tags, action: 'getByStatus' } }
      );

      studentApplicationOperationDuration.add(getByStatusResponse.timings.duration);

      const getByStatusSuccess = check(getByStatusResponse, {
        'getByStatus applications status is 200': (r) => r.status === 200,
      });

      if (getByStatusSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 5. Get applications by section (ADMIN/USER)
    group('Get Applications by Section', () => {
      const sectionId = Math.floor(Math.random() * 3) + 1; // Section 1-3
      
      const getBySectionResponse = http.get(
        `${baseUrl}/student-applications/section/${sectionId}`,
        { headers: adminHeaders, tags: { ...tags, action: 'getBySection' } }
      );

      studentApplicationOperationDuration.add(getBySectionResponse.timings.duration);

      const getBySectionSuccess = check(getBySectionResponse, {
        'getBySection applications status is 200': (r) => r.status === 200,
      });

      if (getBySectionSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 6. Get application by ID
    if (createdApplicationId) {
      group('Get Application by ID', () => {
        const getByIdResponse = http.get(
          `${baseUrl}/student-applications/${createdApplicationId}`,
          { headers: adminHeaders, tags: { ...tags, action: 'getById' } }
        );

        studentApplicationOperationDuration.add(getByIdResponse.timings.duration);

        const getByIdSuccess = check(getByIdResponse, {
          'getById application status is 200': (r) => r.status === 200,
        });

        if (getByIdSuccess) {
          integrationErrorRate.add(0, tags);
        } else {
          integrationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });

      // 7. Approve or reject application (ADMIN/USER)
      const approveOrReject = Math.random() > 0.5;
      
      if (approveOrReject) {
        group('Approve Application', () => {
          const approveResponse = http.put(
            `${baseUrl}/student-applications/${createdApplicationId}/approve`,
            null,
            { headers: adminHeaders, tags: { ...tags, action: 'approve' } }
          );

          studentApplicationOperationDuration.add(approveResponse.timings.duration);

          const approveSuccess = check(approveResponse, {
            'approve application status is 200': (r) => r.status === 200,
          });

          if (approveSuccess) {
            integrationErrorRate.add(0, tags);
          } else {
            integrationErrorRate.add(1, tags);
          }

          sleep(0.2);
        });
      } else {
        group('Reject Application', () => {
          const rejectResponse = http.put(
            `${baseUrl}/student-applications/${createdApplicationId}/reject`,
            null,
            { headers: adminHeaders, tags: { ...tags, action: 'reject' } }
          );

          studentApplicationOperationDuration.add(rejectResponse.timings.duration);

          const rejectSuccess = check(rejectResponse, {
            'reject application status is 200': (r) => r.status === 200,
          });

          if (rejectSuccess) {
            integrationErrorRate.add(0, tags);
          } else {
            integrationErrorRate.add(1, tags);
          }

          sleep(0.2);
        });
      }
    }
  });
}

// ====================
// TEACHER CLASS OPERATIONS
// ====================

/**
 * Teacher Class CRUD operations
 * Uses ADMIN/USER for creation, TEACHER for accept/reject operations
 */
function teacherClassOperations(adminToken, baseUrl) {
  // Get teacher token for accept/reject
  const teacherToken = authenticateUser(baseUrl, roleUsers.teacher.email, roleUsers.teacher.password);
  
  const adminHeaders = {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json',
  };

  const tags = { module: 'integration', operation: 'teacher_class' };
  let createdTeacherClassId = null;
  const teacherId = Math.floor(Math.random() * 8) + 1; // Teacher IDs 1-8
  const classId = Math.floor(Math.random() * 23) + 7; // Class IDs 7-29

  group('Teacher Class Operations', () => {
    // 1. Create teacher-class assignment (ADMIN/USER)
    group('Create Teacher Class', () => {
      const payload = {
        teacherId: teacherId,
        classId: classId,
        startDate: '2025-01-15',
        endDate: '2025-05-30'
      };

      const createResponse = http.post(
        `${baseUrl}/teachers/classes`,
        JSON.stringify(payload),
        { headers: adminHeaders, tags: { ...tags, action: 'create' } }
      );

      teacherClassOperationDuration.add(createResponse.timings.duration);

      const createSuccess = check(createResponse, {
        'create teacher class status is 200': (r) => r.status === 200,
        'create teacher class returns id': (r) => {
          try {
            const body = JSON.parse(r.body);
            return body.id !== undefined;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      if (createSuccess) {
        integrationErrorRate.add(0, tags);
        try {
          const responseBody = JSON.parse(createResponse.body);
          createdTeacherClassId = responseBody.id;
        } catch (e) {
          console.error('Failed to parse created teacher class response ' + e.message);
        }
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 2. Get current semester teacher classes (ADMIN/USER/TEACHER)
    group('Get Current Semester Teacher Classes', () => {
      const getCurrentSemesterResponse = http.get(
        `${baseUrl}/teachers/classes/current-semester`,
        { headers: adminHeaders, tags: { ...tags, action: 'getCurrentSemester' } }
      );

      teacherClassOperationDuration.add(getCurrentSemesterResponse.timings.duration);

      const getCurrentSemesterSuccess = check(getCurrentSemesterResponse, {
        'getCurrentSemester teacher classes status is 200': (r) => r.status === 200,
      });

      if (getCurrentSemesterSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 3. Get all classes for a teacher (ADMIN/USER/TEACHER)
    group('Get All Teacher Classes', () => {
      const getAllResponse = http.get(
        `${baseUrl}/teachers/${teacherId}/classes`,
        { headers: adminHeaders, tags: { ...tags, action: 'getAllByTeacher' } }
      );

      teacherClassOperationDuration.add(getAllResponse.timings.duration);

      const getAllSuccess = check(getAllResponse, {
        'getAllByTeacher status is 200': (r) => r.status === 200,
      });

      if (getAllSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 4. Get pending decision classes (ADMIN/TEACHER)
    group('Get Pending Decision Classes', () => {
      const getPendingResponse = http.get(
        `${baseUrl}/teachers/classes/pending-decision`,
        { headers: adminHeaders, tags: { ...tags, action: 'getPending' } }
      );

      teacherClassOperationDuration.add(getPendingResponse.timings.duration);

      const getPendingSuccess = check(getPendingResponse, {
        'getPending classes status is 200': (r) => r.status === 200,
      });

      if (getPendingSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 5. Get teacher classes by status (ADMIN/USER/TEACHER)
    group('Get Teacher Classes by Status', () => {
      const statusId = Math.floor(Math.random() * 3) + 1; // Status 1-3
      
      const getByStatusResponse = http.get(
        `${baseUrl}/teachers/${teacherId}/classes/status/${statusId}`,
        { headers: adminHeaders, tags: { ...tags, action: 'getByStatus' } }
      );

      teacherClassOperationDuration.add(getByStatusResponse.timings.duration);

      const getByStatusSuccess = check(getByStatusResponse, {
        'getByStatus teacher classes status is 200': (r) => r.status === 200,
      });

      if (getByStatusSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 6. Get teacher class by class ID (ADMIN/USER/TEACHER)
    group('Get Teacher Class by Class ID', () => {
      const getByClassIdResponse = http.get(
        `${baseUrl}/teachers/classes/class/${classId}`,
        { headers: adminHeaders, tags: { ...tags, action: 'getByClassId' } }
      );

      teacherClassOperationDuration.add(getByClassIdResponse.timings.duration);

      const getByClassIdSuccess = check(getByClassIdResponse, {
        'getByClassId status is 200': (r) => r.status === 200,
      });

      if (getByClassIdSuccess) {
        integrationErrorRate.add(0, tags);
      } else {
        integrationErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 7. Accept or reject teacher class (requires TEACHER role)
    if (createdTeacherClassId && teacherToken) {
      const teacherHeaders = {
        'Authorization': `Bearer ${teacherToken}`,
        'Content-Type': 'application/json',
      };

      const acceptOrReject = Math.random() > 0.5;
      
      if (acceptOrReject) {
        group('Accept Teacher Class', () => {
          const acceptPayload = {
            observation: 'Accepted - Load Test'
          };

          const acceptResponse = http.patch(
            `${baseUrl}/teachers/classes/${createdTeacherClassId}/accept`,
            JSON.stringify(acceptPayload),
            { headers: teacherHeaders, tags: { ...tags, action: 'accept' } }
          );

          teacherClassOperationDuration.add(acceptResponse.timings.duration);

          const acceptSuccess = check(acceptResponse, {
            'accept teacher class status is not 500': (r) => r.status !== 500,
          });

          if (acceptSuccess) {
            integrationErrorRate.add(0, tags);
          } else {
            integrationErrorRate.add(1, tags);
          }

          sleep(0.2);
        });
      } else {
        group('Reject Teacher Class', () => {
          const rejectPayload = {
            observation: 'Rejected - Load Test'
          };

          const rejectResponse = http.patch(
            `${baseUrl}/teachers/classes/${createdTeacherClassId}/reject`,
            JSON.stringify(rejectPayload),
            { headers: teacherHeaders, tags: { ...tags, action: 'reject' } }
          );

          teacherClassOperationDuration.add(rejectResponse.timings.duration);

          const rejectSuccess = check(rejectResponse, {
            'reject teacher class status is not 500': (r) => r.status !== 500,
          });

          if (rejectSuccess) {
            integrationErrorRate.add(0, tags);
          } else {
            integrationErrorRate.add(1, tags);
          }

          sleep(0.2);
        });
      }

      // 8. Delete teacher-class assignment (ADMIN/USER)
      group('Delete Teacher Class', () => {
        const deleteResponse = http.del(
          `${baseUrl}/teachers/classes/teacher/${teacherId}/class/${classId}`,
          null,
          { headers: adminHeaders, tags: { ...tags, action: 'delete' } }
        );

        teacherClassOperationDuration.add(deleteResponse.timings.duration);

        const deleteSuccess = check(deleteResponse, {
          'delete teacher class status is 204': (r) => r.status === 204,
        });

        if (deleteSuccess) {
          integrationErrorRate.add(0, tags);
        } else {
          integrationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });
    }
  });
}

// ====================
// MAIN INTEGRATION SCENARIO FUNCTION
// ====================

/**
 * Main entry point for Integration module scenario
 * Distributes operations equally across three controllers
 * @param {string} token - JWT authentication token (admin)
 * @param {string} baseUrl - Base URL of the API
 * @param {Object} data - Shared test data
 */
export default function (token, baseUrl, data) {
  // Weighted operation selection within Integration module
  // Academic Request: 34%, Student Application: 33%, Teacher Class: 33%
  const operation = weightedRandom([
    { weight: 34, name: 'academicRequest', fn: academicRequestOperations },
    { weight: 33, name: 'studentApplication', fn: studentApplicationOperations },
    { weight: 33, name: 'teacherClass', fn: teacherClassOperations },
  ]);
  
  // Execute selected operation with admin token
  operation.fn(token, baseUrl);
}
