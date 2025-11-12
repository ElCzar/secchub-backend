import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// Custom metrics for Security module
const securityErrorRate = new Rate('security_errors');
const authenticationOperationDuration = new Trend('security_authentication_duration_ms', true);
const userOperationDuration = new Trend('security_user_duration_ms', true);

// Mock data for authentication operations
const mockUsers = new SharedArray('users', function () {
  return [
    { username: 'admin', password: 'password', email: 'admin@secchub.com' },
    { username: 'user', password: 'password', email: 'user@secchub.com' },
    { username: 'student', password: 'password', email: 'student@secchub.com' },
    { username: 'teacher', password: 'password', email: 'teacher@secchub.com' },
    { username: 'program', password: 'password', email: 'program@secchub.com' },
    { username: 'maria.garcia', password: 'password', email: 'maria.garcia@secchub.com' },
    { username: 'carlos.lopez', password: 'password', email: 'carlos.lopez@secchub.com' },
    { username: 'ana.rodriguez', password: 'password', email: 'ana.rodriguez@secchub.com' },
    { username: 'luis.martinez', password: 'password', email: 'luis.martinez@secchub.com' },
    { username: 'sofia.hernandez', password: 'password', email: 'sofia.hernandez@secchub.com' },
    { username: 'user-is', password: 'password', email: 'user-is@secchub.com' },
    { username: 'user-si', password: 'password', email: 'user-si@secchub.com' },
    { username: 'dr.silva', password: 'password', email: 'dr.silva@secchub.com' },
    { username: 'prof.torres', password: 'password', email: 'prof.torres@secchub.com' },
    { username: 'dr.morales', password: 'password', email: 'dr.morales@secchub.com' },
    { username: 'prof.castro', password: 'password', email: 'prof.castro@secchub.com' },
    { username: 'dr.vargas', password: 'password', email: 'dr.vargas@secchub.com' },
    { username: 'juan.perez', password: 'password', email: 'juan.perez@secchub.com' },
    { username: 'laura.jimenez', password: 'password', email: 'laura.jimenez@secchub.com' },
    { username: 'diego.ramirez', password: 'password', email: 'diego.ramirez@secchub.com' },
    { username: 'camila.santos', password: 'password', email: 'camila.santos@secchub.com' },
    { username: 'andres.flores', password: 'password', email: 'andres.flores@secchub.com' },
    { username: 'valentina.cruz', password: 'password', email: 'valentina.cruz@secchub.com' },
    { username: 'coord.cs', password: 'password', email: 'coord.cs@secchub.com' },
    { username: 'coord.is', password: 'password', email: 'coord.is@secchub.com' },
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

// ====================
// AUTHENTICATION OPERATIONS
// ====================

/**
 * Authentication operations: Login and Token Refresh
 * Tests: POST /auth/login, POST /auth/refresh
 */
function authenticationOperations(token, baseUrl) {
  const headers = {
    'Content-Type': 'application/json',
  };

  const tags = { module: 'security', operation: 'authentication' };
  let refreshToken = null;

  group('Authentication Operations', () => {
    // 1. Login operation
    group('User Login', () => {
      const userData = mockUsers[Math.floor(Math.random() * mockUsers.length)];
      
      const loginPayload = {
        email: userData.email,
        password: userData.password,
      };

      const loginResponse = http.post(
        `${baseUrl}/auth/login`,
        JSON.stringify(loginPayload),
        { headers, tags: { ...tags, action: 'login' } }
      );

      authenticationOperationDuration.add(loginResponse.timings.duration);

      const loginSuccess = check(loginResponse, {
        'login status is 200': (r) => r.status === 200,
        'login returns access token': (r) => {
          try {
            const body = JSON.parse(r.body);
            return body.accessToken !== undefined;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
        'login returns refresh token': (r) => {
          try {
            const body = JSON.parse(r.body);
            return body.refreshToken !== undefined;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      if (loginSuccess) {
        securityErrorRate.add(0, tags);
        const responseBody = JSON.parse(loginResponse.body);
        refreshToken = responseBody.refreshToken;
      } else {
        securityErrorRate.add(1, tags);
        console.error(`❌ Failed to login user ${userData.username}: ${loginResponse.status} - ${loginResponse.body}`);
      }

      sleep(0.2);
    });

    // 2. Token refresh operation
    if (refreshToken) {
      group('Token Refresh', () => {
        const refreshPayload = {
          refreshToken: refreshToken,
        };

        const refreshResponse = http.post(
          `${baseUrl}/auth/refresh`,
          JSON.stringify(refreshPayload),
          { headers, tags: { ...tags, action: 'refresh' } }
        );

        authenticationOperationDuration.add(refreshResponse.timings.duration);

        const refreshSuccess = check(refreshResponse, {
          'refresh status is 200': (r) => r.status === 200,
          'refresh returns new access token': (r) => {
            try {
              const body = JSON.parse(r.body);
              return body.accessToken !== undefined;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
          'refresh returns new refresh token': (r) => {
            try {
              const body = JSON.parse(r.body);
              return body.refreshToken !== undefined;
            } catch (e) {
              console.error(e);
              return false;
            }
          },
        });

        if (refreshSuccess) {
          securityErrorRate.add(0, tags);
        } else {
          securityErrorRate.add(1, tags);
        }

        sleep(0.2);
      });
    }
  });
}

// ====================
// USER OPERATIONS
// ====================

/**
 * User operations: Get user information, search by email/id, get all users
 * Tests: GET /user, GET /user/all, GET /user/email, GET /user/id/{userId}
 */
function userOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  const tags = { module: 'security', operation: 'user' };

  group('User Operations', () => {
    // 1. Get current user information
    group('Get Current User Info', () => {
      const getCurrentUserResponse = http.get(
        `${baseUrl}/user`,
        { headers, tags: { ...tags, action: 'getCurrentUser' } }
      );

      userOperationDuration.add(getCurrentUserResponse.timings.duration);

      const getCurrentUserSuccess = check(getCurrentUserResponse, {
        'getCurrentUser status is 200': (r) => r.status === 200,
        'getCurrentUser returns user data': (r) => {
          try {
            const body = JSON.parse(r.body);
            return body.username !== undefined && body.email !== undefined;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      if (getCurrentUserSuccess) {
        securityErrorRate.add(0, tags);
      } else {
        securityErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 2. Get all users (admin operation)
    group('Get All Users', () => {
      const getAllUsersResponse = http.get(
        `${baseUrl}/user/all`,
        { headers, tags: { ...tags, action: 'getAllUsers' } }
      );

      userOperationDuration.add(getAllUsersResponse.timings.duration);

      const getAllUsersSuccess = check(getAllUsersResponse, {
        'getAllUsers status is 200': (r) => r.status === 200,
        'getAllUsers returns array': (r) => {
          try {
            const body = JSON.parse(r.body);
            return Array.isArray(body);
          } catch (e) {
            console.error(e);
            return false;
          }
        },
        'getAllUsers has multiple users': (r) => {
          try {
            const body = JSON.parse(r.body);
            return body.length > 0;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      if (getAllUsersSuccess) {
        securityErrorRate.add(0, tags);
      } else {
        securityErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 3. Get user by email
    group('Get User by Email', () => {
      const testUser = mockUsers[Math.floor(Math.random() * mockUsers.length)];
      
      const getUserByEmailResponse = http.get(
        `${baseUrl}/user/email?email=${testUser.email}`,
        { headers, tags: { ...tags, action: 'getUserByEmail' } }
      );

      userOperationDuration.add(getUserByEmailResponse.timings.duration);

      const getUserByEmailSuccess = check(getUserByEmailResponse, {
        'getUserByEmail status is 200': (r) => r.status === 200,
        'getUserByEmail returns correct email': (r) => {
          try {
            const body = JSON.parse(r.body);
            return body.email === testUser.email;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      if (getUserByEmailSuccess) {
        securityErrorRate.add(0, tags);
      } else {
        securityErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 4. Get user by ID
    group('Get User by ID', () => {
      // User IDs range from 1 to 25 based on mock data
      const userId = Math.floor(Math.random() * 25) + 1;
      
      const getUserByIdResponse = http.get(
        `${baseUrl}/user/id/${userId}`,
        { headers, tags: { ...tags, action: 'getUserById' } }
      );

      userOperationDuration.add(getUserByIdResponse.timings.duration);

      const getUserByIdSuccess = check(getUserByIdResponse, {
        'getUserById status is 200 or 404': (r) => r.status === 200 || r.status === 404,
        'getUserById returns user if 200': (r) => {
          if (r.status === 200) {
            try {
              const body = JSON.parse(r.body);
              return body.id === userId;
            } catch (e) {
              console.error(e);
              return false;
            }
          }
          return true; // 404 is acceptable
        },
      });

      if (getUserByIdSuccess) {
        securityErrorRate.add(0, tags);
      } else {
        securityErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 5. Additional get user by email with different user
    group('Get User by Email (Random)', () => {
      const randomUser = mockUsers[Math.floor(Math.random() * mockUsers.length)];
      
      const response = http.get(
        `${baseUrl}/user/email?email=${randomUser.email}`,
        { headers, tags: { ...tags, action: 'getUserByEmailRandom' } }
      );

      userOperationDuration.add(response.timings.duration);

      const success = check(response, {
        'getUserByEmail(random) status is 200': (r) => r.status === 200,
        'getUserByEmail(random) has username': (r) => {
          try {
            const body = JSON.parse(r.body);
            return body.username !== undefined;
          } catch (e) {
            console.error(e);
            return false;
          }
        },
      });

      if (success) {
        securityErrorRate.add(0, tags);
      } else {
        securityErrorRate.add(1, tags);
      }

      sleep(0.2);
    });

    // 6. Get user by ID with different range
    group('Get User by ID (High Range)', () => {
      // Test with higher user IDs
      const userId = Math.floor(Math.random() * 10) + 16; // IDs 16-25
      
      const response = http.get(
        `${baseUrl}/user/id/${userId}`,
        { headers, tags: { ...tags, action: 'getUserByIdHigh' } }
      );

      userOperationDuration.add(response.timings.duration);

      const success = check(response, {
        'getUserById(high) status is 200 or 404': (r) => r.status === 200 || r.status === 404,
      });

      if (success) {
        securityErrorRate.add(0, tags);
      } else {
        securityErrorRate.add(1, tags);
      }

      sleep(0.2);
    });
  });
}

// ====================
// MAIN SECURITY SCENARIO FUNCTION
// ====================

/**
 * Main entry point for Security module scenario
 * Distributes operations based on weights
 * User operations have more weight than authentication
 * @param {string} token - JWT authentication token
 * @param {string} baseUrl - Base URL of the API
 * @param {Object} data - Shared test data
 */
export default function (token, baseUrl, data) {
  // Weighted operation selection within Security module
  // User operations: 70%, Authentication operations: 30%
  const operation = weightedRandom([
    { weight: 70, name: 'user', fn: userOperations },
    { weight: 30, name: 'authentication', fn: authenticationOperations },
  ]);
  
  // Execute selected operation
  operation.fn(token, baseUrl);
}
