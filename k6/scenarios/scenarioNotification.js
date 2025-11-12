import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// Custom metrics for Notification module
const notificationErrorRate = new Rate('notification_errors');
const emailTemplateOperationDuration = new Trend('notification_email_template_duration_ms', true);

// Mock data for email templates
const mockEmailTemplates = new SharedArray('emailTemplates', function () {
  return [
    { 
      name: 'welcome_email',
      subject: 'Welcome to SecHub',
      body: 'Hello {{name}}, welcome to our platform!',
      variables: ['name']
    },
    {
      name: 'password_reset',
      subject: 'Password Reset Request',
      body: 'Click here to reset your password: {{resetLink}}',
      variables: ['resetLink']
    },
    {
      name: 'course_enrollment',
      subject: 'Course Enrollment Confirmation',
      body: 'You have been enrolled in {{courseName}}',
      variables: ['courseName']
    },
    {
      name: 'grade_notification',
      subject: 'Grade Posted',
      body: 'Your grade for {{courseName}} is {{grade}}',
      variables: ['courseName', 'grade']
    },
    {
      name: 'schedule_update',
      subject: 'Schedule Change Notification',
      body: 'Your schedule has been updated for {{semester}}',
      variables: ['semester']
    }
  ];
});

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
// EMAIL TEMPLATE OPERATIONS
// ====================

/**
 * Email template CRUD operations
 * Tests: POST /emails/templates, GET /emails/templates, GET /emails/templates/{id}, 
 *        GET /emails/templates/name/{name}, PUT /emails/templates/{id}, DELETE /emails/templates/{id}
 */
function emailTemplateOperations(token, baseUrl) {
const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  const tags = { module: 'notification', operation: 'email_template' };
  let createdTemplateId = null;
  let createdTemplateName = null;

  group('Email Template Operations', () => {
    // 1. Create email template
    group('Create Email Template', () => {
      const templateData = mockEmailTemplates[Math.floor(Math.random() * mockEmailTemplates.length)];
      const uniqueName = `${templateData.name}_${generateUniqueId()}`;
      
      const payload = {
        name: uniqueName,
        subject: templateData.subject,
        body: templateData.body,
        variables: templateData.variables
      };

      const createResponse = http.post(
        `${baseUrl}/emails/templates`,
        JSON.stringify(payload),
        { headers, tags: { ...tags, action: 'create' } }
      );

      emailTemplateOperationDuration.add(createResponse.timings.duration);

      const createSuccess = check(createResponse, {
        'create template status is 201': (r) => r.status === 201,
        'create template returns id': (r) => {
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
        notificationErrorRate.add(0, tags);
        const responseBody = JSON.parse(createResponse.body);
        createdTemplateId = responseBody.id;
        createdTemplateName = responseBody.name;
      } else {
        notificationErrorRate.add(1, tags);
        console.error(`❌ Failed to create template: ${createResponse.status} - ${createResponse.body}`);
      }

      sleep(0.2);
    });

    // 2. Get all email templates
    if (createdTemplateId) {
      group('Get All Email Templates', () => {
        const getAllResponse = http.get(
          `${baseUrl}/emails/templates`,
          { headers, tags: { ...tags, action: 'getAll' } }
        );

        emailTemplateOperationDuration.add(getAllResponse.timings.duration);

        const getAllSuccess = check(getAllResponse, {
          'getAll templates status is 200': (r) => r.status === 200,
          'getAll templates returns array': (r) => {
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
          notificationErrorRate.add(0, tags);
        } else {
          notificationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });

      // 3. Get email template by ID
      group('Get Email Template by ID', () => {
        const getByIdResponse = http.get(
          `${baseUrl}/emails/templates/${createdTemplateId}`,
          { headers, tags: { ...tags, action: 'getById' } }
        );

        emailTemplateOperationDuration.add(getByIdResponse.timings.duration);

        const getByIdSuccess = check(getByIdResponse, {
          'getById template status is 200': (r) => r.status === 200,
          'getById template returns correct id': (r) => {
            try {
              const body = JSON.parse(r.body);
              return body.id === createdTemplateId;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        if (getByIdSuccess) {
          notificationErrorRate.add(0, tags);
        } else {
          notificationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });

      // 4. Get email template by name
      group('Get Email Template by Name', () => {
        const getByNameResponse = http.get(
          `${baseUrl}/emails/templates/name/${createdTemplateName}`,
          { headers, tags: { ...tags, action: 'getByName' } }
        );

        emailTemplateOperationDuration.add(getByNameResponse.timings.duration);

        const getByNameSuccess = check(getByNameResponse, {
          'getByName template status is 200': (r) => r.status === 200,
          'getByName template returns correct name': (r) => {
            try {
              const body = JSON.parse(r.body);
              return body.name === createdTemplateName;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        if (getByNameSuccess) {
          notificationErrorRate.add(0, tags);
        } else {
          notificationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });

      // 5. Update email template
      group('Update Email Template', () => {
        const updatePayload = {
          name: createdTemplateName,
          subject: 'Updated Subject - Load Test',
          body: 'Updated body content for template {{variable}}',
          variables: ['variable']
        };

        const updateResponse = http.put(
          `${baseUrl}/emails/templates/${createdTemplateId}`,
          JSON.stringify(updatePayload),
          { headers, tags: { ...tags, action: 'update' } }
        );

        emailTemplateOperationDuration.add(updateResponse.timings.duration);

        const updateSuccess = check(updateResponse, {
          'update template status is 200': (r) => r.status === 200,
          'update template returns updated data': (r) => {
            try {
              const body = JSON.parse(r.body);
              return body.subject === 'Updated Subject - Load Test';
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        if (updateSuccess) {
          notificationErrorRate.add(0, tags);
        } else {
          notificationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });

      // 6. Delete email template
      group('Delete Email Template', () => {
        const deleteResponse = http.del(
          `${baseUrl}/emails/templates/${createdTemplateId}`,
          null,
          { headers, tags: { ...tags, action: 'delete' } }
        );

        emailTemplateOperationDuration.add(deleteResponse.timings.duration);

        const deleteSuccess = check(deleteResponse, {
          'delete template status is 200': (r) => r.status === 200,
        });

        if (deleteSuccess) {
          notificationErrorRate.add(0, tags);
        } else {
          notificationErrorRate.add(1, tags);
        }

        sleep(0.2);
      });
    }
  });
}

/**
 * Main notification scenario function
 * Executes email template operations as admin actions (create, read, update, delete)
 */
export default function (token, baseUrl, data) {
  // Execute email template CRUD operations
  emailTemplateOperations(token, baseUrl);
}
