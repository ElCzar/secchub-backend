import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// Custom metrics for Planning module
const planningErrorRate = new Rate('planning_errors');
const classroomOperationDuration = new Trend('planning_classroom_duration_ms', true);
const planningOperationDuration = new Trend('planning_planning_duration_ms', true);
const teachingAssistantOperationDuration = new Trend('planning_teaching_assistant_duration_ms', true);

// Mock data for classrooms
const mockClassrooms = new SharedArray('classrooms', function () {
  return [
    { classroomTypeId: 1, campus: 'Main Campus', location: 'Building A', room: 'A101', capacity: 30 },
    { classroomTypeId: 2, campus: 'Main Campus', location: 'Building B', room: 'B202', capacity: 25 },
    { classroomTypeId: 3, campus: 'Main Campus', location: 'Building C', room: 'C303', capacity: 100 },
    { classroomTypeId: 1, campus: 'North Campus', location: 'Building D', room: 'D404', capacity: 35 },
    { classroomTypeId: 2, campus: 'North Campus', location: 'Building E', room: 'E505', capacity: 20 },
  ];
});

// Mock data for class schedules
const mockSchedules = new SharedArray('schedules', function () {
  return [
    { classroomId: 1, startTime: '08:00:00', endTime: '10:00:00', dayOfWeek: 'Lunes' },
    { classroomId: 2, startTime: '10:00:00', endTime: '12:00:00', dayOfWeek: 'Martes' },
    { classroomId: 3, startTime: '14:00:00', endTime: '16:00:00', dayOfWeek: 'Miércoles' },
    { classroomId: 4, startTime: '08:00:00', endTime: '10:00:00', dayOfWeek: 'Jueves' },
    { classroomId: 5, startTime: '16:00:00', endTime: '18:00:00', dayOfWeek: 'Viernes' },
  ];
});

// Available student application IDs that are NOT already assigned as TAs
// Based on init-mock-data.sql: IDs 1-19 exist, but 1,2,3,4,5,6,9,10,13,15 are already TAs
const availableStudentApplicationIds = new SharedArray('availableStudentAppIds', function () {
  return [7, 8, 11, 12, 14, 16, 17, 18, 19];
});

/**
 * Utility function for weighted random selection
 */
function weightedRandom(items) {
  const totalWeight = items.reduce((sum, item) => sum + item.weight, 0);
  let random = Math.random() * totalWeight;
  
  for (const item of items) {
    random -= item.weight;
    if (random <= 0) {
      return item;
    }
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
// CLASSROOM OPERATIONS (10%)
// ====================

/**
 * Classroom CRUD operations
 * Create, Read, Update, Delete classrooms
 */
function classroomOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  let createdClassroomId = null;

  group('Planning - Classroom Operations', () => {
    // 1. Create Classroom
    group('Create Classroom', () => {
      const classroomData = mockClassrooms[Math.floor(Math.random() * mockClassrooms.length)];
      const uniqueClassroom = {
        ...classroomData,
        room: `${classroomData.room}-${generateUniqueId()}`,
      };

      const createResponse = http.post(
        `${baseUrl}/classrooms`,
        JSON.stringify(uniqueClassroom),
        { headers, tags: { operation: 'classroom_create' } }
      );

      classroomOperationDuration.add(createResponse.timings.duration);

      const createSuccess = check(createResponse, {
        'classroom created (201)': (r) => r.status === 201,
        'classroom has id': (r) => {
          try {
            return r.json('id') !== undefined;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      planningErrorRate.add(!createSuccess);

      if (createSuccess) {
        try {
          createdClassroomId = createResponse.json('id');
        } catch (e) {
          console.error(e);
        }
      }

      sleep(0.2);
    });

    // 2. Get All Classrooms
    group('Get All Classrooms', () => {
      const getAllResponse = http.get(
        `${baseUrl}/classrooms`,
        { headers, tags: { operation: 'classroom_get_all' } }
      );

      classroomOperationDuration.add(getAllResponse.timings.duration);

      const getAllSuccess = check(getAllResponse, {
        'get all classrooms (200)': (r) => r.status === 200,
        'classrooms list is array': (r) => {
          try {
            return Array.isArray(r.json());
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      planningErrorRate.add(!getAllSuccess);

      sleep(0.2);
    });

    // 3. Get Classroom by ID
    if (createdClassroomId) {
      group('Get Classroom by ID', () => {
        const getByIdResponse = http.get(
          `${baseUrl}/classrooms/${createdClassroomId}`,
          { headers, tags: { operation: 'classroom_get_by_id' } }
        );

        classroomOperationDuration.add(getByIdResponse.timings.duration);

        const getByIdSuccess = check(getByIdResponse, {
          'get classroom by id (200)': (r) => r.status === 200,
          'classroom has correct id': (r) => {
            try {
              return r.json('id') === createdClassroomId;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!getByIdSuccess);

        sleep(0.2);
      });

      // 4. Update Classroom
      group('Update Classroom', () => {
        const updatedData = {
          classroomTypeId: 1,
          campus: 'Updated Campus',
          location: 'Updated Building',
          room: `Updated-${generateUniqueId()}`,
          capacity: 45,
        };

        const updateResponse = http.put(
          `${baseUrl}/classrooms/${createdClassroomId}`,
          JSON.stringify(updatedData),
          { headers, tags: { operation: 'classroom_update' } }
        );

        classroomOperationDuration.add(updateResponse.timings.duration);

        const updateSuccess = check(updateResponse, {
          'classroom updated (200)': (r) => r.status === 200,
          'classroom room updated': (r) => {
            try {
              return r.json('room') === updatedData.room;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!updateSuccess);

        sleep(0.2);
      });

      // 5. Get Classrooms by Type
      group('Get Classrooms by Type', () => {
        const typeId = Math.floor(Math.random() * 3) + 1; // Type IDs 1-3
        const getByTypeResponse = http.get(
          `${baseUrl}/classrooms/type/${typeId}`,
          { headers, tags: { operation: 'classroom_get_by_type' } }
        );

        classroomOperationDuration.add(getByTypeResponse.timings.duration);

        const getByTypeSuccess = check(getByTypeResponse, {
          'get classrooms by type (200)': (r) => r.status === 200,
          'filtered classrooms is array': (r) => {
            try {
              return Array.isArray(r.json());
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!getByTypeSuccess);

        sleep(0.2);
      });

      // 6. Delete Classroom
      group('Delete Classroom', () => {
        const deleteResponse = http.del(
          `${baseUrl}/classrooms/${createdClassroomId}`,
          null,
          { headers, tags: { operation: 'classroom_delete' } }
        );

        classroomOperationDuration.add(deleteResponse.timings.duration);

        const deleteSuccess = check(deleteResponse, {
          'classroom deleted (204)': (r) => r.status === 204,
        });

        planningErrorRate.add(!deleteSuccess);

        sleep(0.2);
      });
    }
  });
}

// ====================
// PLANNING OPERATIONS (80%)
// ====================

/**
 * Planning CRUD operations for classes and schedules
 * Create, Read, Update, Delete classes and their schedules
 */
function planningOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  let createdClassId = null;
  let createdScheduleId = null;

  group('Planning - Class and Schedule Operations', () => {
    // 1. Create Class
    group('Create Class', () => {
      const courseId = Math.floor(Math.random() * 20) + 1; // Course IDs 1-20
      const semesterId = 2; // Current semester
      const teacherId = Math.floor(Math.random() * 8) + 1; // Teacher IDs 1-8
      const sectionId = Math.floor(Math.random() * 3) + 1; // Section IDs 1-3

      const classData = {
        courseId: courseId,
        semesterId: semesterId,
        section: sectionId,
        capacity: Math.floor(Math.random() * 20) + 30, // 30-50
        teacherId: teacherId,
      };

      const createResponse = http.post(
        `${baseUrl}/planning/classes`,
        JSON.stringify(classData),
        { headers, tags: { operation: 'planning_create_class' } }
      );

      planningOperationDuration.add(createResponse.timings.duration);

      const createSuccess = check(createResponse, {
        'class created (201)': (r) => r.status === 201,
        'class has id': (r) => {
          try {
            return r.json('id') !== undefined;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      planningErrorRate.add(!createSuccess);

      if (createSuccess) {
        try {
          createdClassId = createResponse.json('id');
        } catch (e) {
          console.error(e);
        }
      }

      sleep(0.2);
    });

    // 2. Get Current Semester Classes
    group('Get Current Semester Classes', () => {
      const getCurrentResponse = http.get(
        `${baseUrl}/planning/classes/current-semester`,
        { headers, tags: { operation: 'planning_get_current_semester' } }
      );

      planningOperationDuration.add(getCurrentResponse.timings.duration);

      const getCurrentSuccess = check(getCurrentResponse, {
        'get current semester classes (200)': (r) => r.status === 200,
        'classes list is array': (r) => {
          try {
            return Array.isArray(r.json());
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      planningErrorRate.add(!getCurrentSuccess);

      sleep(0.2);
    });

    // 3. Get All Classes
    group('Get All Classes', () => {
      const getAllResponse = http.get(
        `${baseUrl}/planning/classes`,
        { headers, tags: { operation: 'planning_get_all_classes' } }
      );

      planningOperationDuration.add(getAllResponse.timings.duration);

      const getAllSuccess = check(getAllResponse, {
        'get all classes (200)': (r) => r.status === 200,
        'all classes is array': (r) => {
          try {
            return Array.isArray(r.json());
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      planningErrorRate.add(!getAllSuccess);

      sleep(0.2);
    });

    // 4. Get Class by ID
    if (createdClassId) {
      group('Get Class by ID', () => {
        const getByIdResponse = http.get(
          `${baseUrl}/planning/classes/${createdClassId}`,
          { headers, tags: { operation: 'planning_get_class_by_id' } }
        );

        planningOperationDuration.add(getByIdResponse.timings.duration);

        const getByIdSuccess = check(getByIdResponse, {
          'get class by id (200)': (r) => r.status === 200,
          'class has correct id': (r) => {
            try {
              return r.json('id') === createdClassId;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!getByIdSuccess);

        sleep(0.2);
      });

      // 5. Update Class
      group('Update Class', () => {
        const updatedData = {
          courseId: Math.floor(Math.random() * 20) + 1,
          semesterId: 2,
          section: Math.floor(Math.random() * 3) + 1,
          capacity: Math.floor(Math.random() * 20) + 40,
          teacherId: Math.floor(Math.random() * 8) + 1,
        };

        const updateResponse = http.put(
          `${baseUrl}/planning/classes/${createdClassId}`,
          JSON.stringify(updatedData),
          { headers, tags: { operation: 'planning_update_class' } }
        );

        planningOperationDuration.add(updateResponse.timings.duration);

        const updateSuccess = check(updateResponse, {
          'class updated (200)': (r) => r.status === 200,
          'class capacity updated': (r) => {
            try {
              return r.json('capacity') === updatedData.capacity;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!updateSuccess);

        sleep(0.2);
      });

      // 6. Get Classes by Course
      group('Get Classes by Course', () => {
        const courseId = Math.floor(Math.random() * 20) + 1;
        const getByCourseResponse = http.get(
          `${baseUrl}/planning/classes/course/${courseId}`,
          { headers, tags: { operation: 'planning_get_classes_by_course' } }
        );

        planningOperationDuration.add(getByCourseResponse.timings.duration);

        const getByCourseSuccess = check(getByCourseResponse, {
          'get classes by course (200)': (r) => r.status === 200,
          'course classes is array': (r) => {
            try {
              return Array.isArray(r.json());
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!getByCourseSuccess);

        sleep(0.2);
      });

      // 7. Get Classes by Section
      group('Get Classes by Section', () => {
        const sectionId = Math.floor(Math.random() * 3) + 1;
        const getBySectionResponse = http.get(
          `${baseUrl}/planning/classes/section/${sectionId}`,
          { headers, tags: { operation: 'planning_get_classes_by_section' } }
        );

        planningOperationDuration.add(getBySectionResponse.timings.duration);

        const getBySectionSuccess = check(getBySectionResponse, {
          'get classes by section (200)': (r) => r.status === 200,
          'section classes is array': (r) => {
            try {
              return Array.isArray(r.json());
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!getBySectionSuccess);

        sleep(0.2);
      });

      // 8. Add Class Schedule
      group('Add Class Schedule', () => {
        const scheduleData = mockSchedules[Math.floor(Math.random() * mockSchedules.length)];
        const schedulePayload = {
          ...scheduleData,
          classroomId: Math.floor(Math.random() * 5) + 1, // Classroom IDs 1-5
        };

        const addScheduleResponse = http.post(
          `${baseUrl}/planning/classes/${createdClassId}/schedules`,
          JSON.stringify(schedulePayload),
          { headers, tags: { operation: 'planning_add_schedule' } }
        );

        planningOperationDuration.add(addScheduleResponse.timings.duration);

        const addScheduleSuccess = check(addScheduleResponse, {
          'schedule added (201)': (r) => r.status === 201,
          'schedule has id': (r) => {
            try {
              return r.json('id') !== undefined;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!addScheduleSuccess);

        if (addScheduleSuccess) {
          try {
            createdScheduleId = addScheduleResponse.json('id');
          } catch (e) {
            console.error(e);
          }
        }

        sleep(0.2);
      });

      // 9. Get Class Schedules
      group('Get Class Schedules', () => {
        const getSchedulesResponse = http.get(
          `${baseUrl}/planning/classes/${createdClassId}/schedules`,
          { headers, tags: { operation: 'planning_get_class_schedules' } }
        );

        planningOperationDuration.add(getSchedulesResponse.timings.duration);

        const getSchedulesSuccess = check(getSchedulesResponse, {
          'get class schedules (200)': (r) => r.status === 200,
          'schedules is array': (r) => {
            try {
              return Array.isArray(r.json());
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!getSchedulesSuccess);

        sleep(0.2);
      });

      // 10. Update Schedule (if created)
      if (createdScheduleId) {
        group('Update Class Schedule', () => {
          const updatedScheduleData = {
            classroomId: Math.floor(Math.random() * 5) + 1,
            startTime: '10:00:00',
            endTime: '12:00:00',
            dayOfWeek: 'Miércoles',
          };

          const updateScheduleResponse = http.put(
            `${baseUrl}/planning/schedules/${createdScheduleId}`,
            JSON.stringify(updatedScheduleData),
            { headers, tags: { operation: 'planning_update_schedule' } }
          );

          planningOperationDuration.add(updateScheduleResponse.timings.duration);

          const updateScheduleSuccess = check(updateScheduleResponse, {
            'schedule updated (200)': (r) => r.status === 200,
            'schedule time updated': (r) => {
              try {
                return r.json('startTime') === updatedScheduleData.startTime;
              } catch (e) {
                console.error(e);
                return false;
              }
            },
          });

          planningErrorRate.add(!updateScheduleSuccess);

          sleep(0.2);
        });

        // 11. Get Schedule by ID
        group('Get Schedule by ID', () => {
          const getScheduleByIdResponse = http.get(
            `${baseUrl}/planning/schedules/${createdScheduleId}`,
            { headers, tags: { operation: 'planning_get_schedule_by_id' } }
          );

          planningOperationDuration.add(getScheduleByIdResponse.timings.duration);

          const getScheduleByIdSuccess = check(getScheduleByIdResponse, {
            'get schedule by id (200)': (r) => r.status === 200,
            'schedule has correct id': (r) => {
              try {
                return r.json('id') === createdScheduleId;
              } catch (e) {
                console.error(e);
                return false;
              }
            },
          });

          planningErrorRate.add(!getScheduleByIdSuccess);

          sleep(0.2);
        });

        // 12. Delete Schedule
        group('Delete Class Schedule', () => {
          const deleteScheduleResponse = http.del(
            `${baseUrl}/planning/schedules/${createdScheduleId}`,
            null,
            { headers, tags: { operation: 'planning_delete_schedule' } }
          );

          planningOperationDuration.add(deleteScheduleResponse.timings.duration);

          const deleteScheduleSuccess = check(deleteScheduleResponse, {
            'schedule deleted (204)': (r) => r.status === 204,
          });

          planningErrorRate.add(!deleteScheduleSuccess);

          sleep(0.2);
        });
      }

      // 13. Delete Class
      group('Delete Class', () => {
        const deleteResponse = http.del(
          `${baseUrl}/planning/classes/${createdClassId}`,
          null,
          { headers, tags: { operation: 'planning_delete_class' } }
        );

        planningOperationDuration.add(deleteResponse.timings.duration);

        const deleteSuccess = check(deleteResponse, {
          'class deleted (204)': (r) => r.status === 204,
        });

        planningErrorRate.add(!deleteSuccess);

        sleep(0.2);
      });
    }
  });
}

// ====================
// TEACHING ASSISTANT OPERATIONS (10%)
// ====================

/**
 * Teaching Assistant CRUD operations
 * Create, Read, Update, Delete teaching assistant assignments and schedules
 */
function teachingAssistantOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  let createdTAId = null;
  let createdTAScheduleId = null;
  let createdTAStudentApplicationId = null;
  let createdTAClassId = null;

  group('Planning - Teaching Assistant Operations', () => {
    // 1. Create Teaching Assistant
    group('Create Teaching Assistant', () => {
      // Use available student application IDs that are not already assigned as TAs
      // Note: Each student application can only have ONE TA assignment (uniqueness constraint)
      // In concurrent load testing, some create attempts may fail if the ID is already used
      // This is expected behavior and tests the constraint validation
      const studentApplicationId = availableStudentApplicationIds[
        Math.floor(Math.random() * availableStudentApplicationIds.length)
      ];
      const classId = Math.floor(Math.random() * 23) + 7; // Class IDs 7-29

      const taData = {
        classId: classId,
        studentApplicationId: studentApplicationId,
        weeklyHours: Math.floor(Math.random() * 10) + 10, // 10-20 hours
        weeks: Math.floor(Math.random() * 8) + 10, // 10-18 weeks
        totalHours: null, // Will be calculated by backend
        schedules: [
          {
            day: 'Lunes',
            startTime: '08:00:00',
            endTime: '10:00:00',
          },
          {
            day: 'Miércoles',
            startTime: '14:00:00',
            endTime: '16:00:00',
          }
        ],
      };

      const createResponse = http.post(
        `${baseUrl}/teaching-assistants`,
        JSON.stringify(taData),
        { headers, tags: { operation: 'ta_create' } }
      );

      teachingAssistantOperationDuration.add(createResponse.timings.duration);

      // Accept both 201 (created) and 400 (duplicate) as valid responses
      // 400 is expected when student application already has a TA assignment
      const createSuccess = check(createResponse, {
        'teaching assistant created or duplicate (201/400)': (r) => r.status === 201 || r.status === 400,
        'teaching assistant has id if created': (r) => {
          if (r.status === 201) {
            try {
              return r.json('id') !== undefined;
            } catch (e) {
              console.error(e);
              return false;
            }
          }
          return true; // Skip check for 400 responses
        },
      });

      planningErrorRate.add(!createSuccess);

      // Only store TA data if it was actually created (201), not if duplicate (400)
      if (createSuccess && createResponse.status === 201) {
        try {
          createdTAId = createResponse.json('id');
          createdTAStudentApplicationId = studentApplicationId;
          createdTAClassId = classId;
        } catch (e) {
          console.error(e);
        }
      }

      sleep(0.2);
    });

    // 2. Get All Teaching Assistants
    group('Get All Teaching Assistants', () => {
      const getAllResponse = http.get(
        `${baseUrl}/teaching-assistants`,
        { headers, tags: { operation: 'ta_get_all' } }
      );

      teachingAssistantOperationDuration.add(getAllResponse.timings.duration);

      const getAllSuccess = check(getAllResponse, {
        'get all teaching assistants (200)': (r) => r.status === 200,
        'teaching assistants list is array': (r) => {
          try {
            return Array.isArray(r.json());
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      planningErrorRate.add(!getAllSuccess);

      sleep(0.2);
    });

    // 3. Get Teaching Assistant by ID
    if (createdTAId) {
      group('Get Teaching Assistant by ID', () => {
        const getByIdResponse = http.get(
          `${baseUrl}/teaching-assistants/${createdTAId}`,
          { headers, tags: { operation: 'ta_get_by_id' } }
        );

        teachingAssistantOperationDuration.add(getByIdResponse.timings.duration);

        const getByIdSuccess = check(getByIdResponse, {
          'get teaching assistant by id (200)': (r) => r.status === 200,
          'teaching assistant has correct id': (r) => {
            try {
              return r.json('id') === createdTAId;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!getByIdSuccess);

        sleep(0.2);
      });

      // 4. Update Teaching Assistant
      group('Update Teaching Assistant', () => {
        // Update the TA assignment similar to integration test
        // Keep the same studentApplicationId and classId to avoid conflicts
        const updatedData = {
          classId: createdTAClassId, // Keep same class
          studentApplicationId: createdTAStudentApplicationId, // Keep same student application
          weeklyHours: Math.floor(Math.random() * 10) + 15, // 15-25 hours (increased from creation)
          weeks: Math.floor(Math.random() * 8) + 12, // 12-20 weeks
          totalHours: null, // Will be calculated by backend
          schedules: [
            {
              day: 'Martes',
              startTime: '10:00:00',
              endTime: '12:00:00',
            },
            {
              day: 'Jueves',
              startTime: '16:00:00',
              endTime: '18:00:00',
            }
          ],
        };

        const updateResponse = http.put(
          `${baseUrl}/teaching-assistants/${createdTAId}`,
          JSON.stringify(updatedData),
          { headers, tags: { operation: 'ta_update' } }
        );

        teachingAssistantOperationDuration.add(updateResponse.timings.duration);

        // Log error details if update fails
        if (updateResponse.status !== 200) {
          console.error(`TA Update failed: Status ${updateResponse.status}, ID: ${createdTAId}, ClassId: ${createdTAClassId}, StudentAppId: ${createdTAStudentApplicationId}, Body: ${updateResponse.body}`);
        }

        const updateSuccess = check(updateResponse, {
          'teaching assistant updated (200)': (r) => r.status === 200,
          'teaching assistant has correct id': (r) => {
            try {
              return r.json('id') === createdTAId;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
          'teaching assistant hours updated': (r) => {
            try {
              const json = r.json();
              return json.weeklyHours >= 15;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!updateSuccess);

        sleep(0.2);
      });

      // 5. Get TA by Student Application
      group('Get TA by Student Application', () => {
        // Try to get a TA for a student application that might have one
        // Use IDs 1-19 (all valid student applications in mock data)
        const applicationId = Math.floor(Math.random() * 19) + 1;
        const getByApplicationResponse = http.get(
          `${baseUrl}/teaching-assistants/student-application/${applicationId}`,
          { headers, tags: { operation: 'ta_get_by_application' } }
        );

        teachingAssistantOperationDuration.add(getByApplicationResponse.timings.duration);

        const getByApplicationSuccess = check(getByApplicationResponse, {
          'get ta by application (200 or 404)': (r) => r.status === 200 || r.status === 404,
        });

        planningErrorRate.add(!getByApplicationSuccess);

        sleep(0.2);
      });

      // 6. Create TA Schedule
      group('Create TA Schedule', () => {
        const scheduleData = {
          day: 'Lunes',
          startTime: '08:00:00',
          endTime: '10:00:00',
        };

        const createScheduleResponse = http.post(
          `${baseUrl}/teaching-assistants/${createdTAId}/schedules`,
          JSON.stringify(scheduleData),
          { headers, tags: { operation: 'ta_create_schedule' } }
        );

        teachingAssistantOperationDuration.add(createScheduleResponse.timings.duration);

        const createScheduleSuccess = check(createScheduleResponse, {
          'ta schedule created (201)': (r) => r.status === 201,
          'ta schedule has id': (r) => {
            try {
              return r.json('id') !== undefined;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!createScheduleSuccess);

        if (createScheduleSuccess) {
          try {
            createdTAScheduleId = createScheduleResponse.json('id');
          } catch (e) {
            console.error(e);
          }
        }

        sleep(0.2);
      });

      // 7. Update TA Schedule (if created)
      if (createdTAScheduleId) {
        group('Update TA Schedule', () => {
          const updatedSchedule = {
            day: 'Miércoles',
            startTime: '14:00:00',
            endTime: '16:00:00',
          };

          const updateScheduleResponse = http.put(
            `${baseUrl}/teaching-assistants/schedules/${createdTAScheduleId}`,
            JSON.stringify(updatedSchedule),
            { headers, tags: { operation: 'ta_update_schedule' } }
          );

          teachingAssistantOperationDuration.add(updateScheduleResponse.timings.duration);

          const updateScheduleSuccess = check(updateScheduleResponse, {
            'ta schedule updated (200)': (r) => r.status === 200,
          });

          planningErrorRate.add(!updateScheduleSuccess);

          sleep(0.2);
        });

        // 8. Delete TA Schedule
        group('Delete TA Schedule', () => {
          const deleteScheduleResponse = http.del(
            `${baseUrl}/teaching-assistants/schedules/${createdTAScheduleId}`,
            null,
            { headers, tags: { operation: 'ta_delete_schedule' } }
          );

          teachingAssistantOperationDuration.add(deleteScheduleResponse.timings.duration);

          const deleteScheduleSuccess = check(deleteScheduleResponse, {
            'ta schedule deleted (200)': (r) => r.status === 200,
          });

          planningErrorRate.add(!deleteScheduleSuccess);

          sleep(0.2);
        });
      }

      // 9. Get TA Schedule Conflicts
      group('Get TA Schedule Conflicts', () => {
        const getConflictsResponse = http.get(
          `${baseUrl}/teaching-assistants/conflicts`,
          { headers, tags: { operation: 'ta_get_conflicts' } }
        );

        teachingAssistantOperationDuration.add(getConflictsResponse.timings.duration);

        const getConflictsSuccess = check(getConflictsResponse, {
          'get ta conflicts (200)': (r) => r.status === 200,
          'conflicts is array': (r) => {
            try {
              return Array.isArray(r.json());
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        planningErrorRate.add(!getConflictsSuccess);

        sleep(0.2);
      });

      // 10. Delete Teaching Assistant
      group('Delete Teaching Assistant', () => {
        const deleteResponse = http.del(
          `${baseUrl}/teaching-assistants/${createdTAId}`,
          null,
          { headers, tags: { operation: 'ta_delete' } }
        );

        teachingAssistantOperationDuration.add(deleteResponse.timings.duration);

        const deleteSuccess = check(deleteResponse, {
          'teaching assistant deleted (200)': (r) => r.status === 200,
        });

        planningErrorRate.add(!deleteSuccess);

        // Reset the ID after deletion to prevent reuse in next iteration
        if (deleteSuccess) {
          createdTAId = null;
          createdTAScheduleId = null;
          createdTAStudentApplicationId = null;
          createdTAClassId = null;
        }

        sleep(0.2);
      });
    }
  });
}

// ====================
// MAIN PLANNING SCENARIO FUNCTION
// ====================

/**
 * Main entry point for Planning module scenario
 * Distributes operations based on weights
 * @param {string} token - JWT authentication token (admin)
 * @param {string} baseUrl - Base URL of the API
 * @param {Object} data - Shared test data
 */
export default function (token, baseUrl, data) {
  // Weighted operation selection within Planning module
  // Planning: 80%, Classroom: 10%, Teaching Assistant: 10%
  const operation = weightedRandom([
    { weight: 80, name: 'planning', fn: planningOperations },
    { weight: 10, name: 'classroom', fn: classroomOperations },
    { weight: 10, name: 'teachingAssistant', fn: teachingAssistantOperations },
  ]);
  
  // Execute selected operation with admin token
  operation.fn(token, baseUrl);
}
