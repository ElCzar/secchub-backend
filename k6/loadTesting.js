import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';
import * as AdminScenario from './scenarios/scenarioAdmin.js';
import * as IntegrationScenario from './scenarios/scenarioIntegration.js';
import * as LogScenario from './scenarios/scenarioLog.js';
import * as NotificationScenario from './scenarios/scenarioNotification.js';
import * as ParametricScenario from './scenarios/scenarioParametric.js';
import * as PlanningScenario from './scenarios/scenarioPlanning.js';
import * as SecurityScenario from './scenarios/scenarioSecurity.js';

// Custom metrics
const errorRate = new Rate('errors');
const operationCounter = new Counter('operations_by_module');

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Load test configuration
export const options = {
  stages: [
    { duration: '10s', target: 10 },  // Ramp up to 10 users
    { duration: '20s', target: 30 },  // Ramp up to 30 users
    { duration: '30s', target: 50 },  // Ramp up to 50 users (max)
    { duration: '20s', target: 50 },  // Maintain 50 users for 5 minutes
    { duration: '5s', target: 0 },   // Ramp down
  ],
  thresholds: {
    'errors': ['rate<0.05'],                           // Error rate < 5%
    'http_req_duration': ['p(95)<1000', 'p(99)<2000'], // 95% < 1s, 99% < 2s
    'http_req_failed': ['rate<0.05'],                  // Failed requests < 5%
  },
};

/**
 * Utility function for weighted random selection
 * @param {Array} items - Array of {weight, name, fn} objects
 * @returns {Object} Selected item
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
 * Authenticate admin user
 * @returns {string|null} JWT token or null if authentication fails
 */
function authenticate() {
  const payload = JSON.stringify({
    email: 'admin@secchub.com',
    password: 'password',
  });
  
  const response = http.post(`${BASE_URL}/auth/login`, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'authenticate' },
  });
  
  const success = check(response, {
    'authentication successful': (r) => r.status === 200,
    'token received': (r) => r.json('accessToken') !== undefined,
  });
  
  if (!success) {
    console.error(`❌ Authentication failed: ${response.status} - ${response.body}`);
    return null;
  }
  
  return response.json('accessToken');
}

/**
 * Setup function - runs once before test
 * Authenticates admin user and prepares test data
 */
export function setup() {
  console.log('🚀 Starting SecHub Backend Load Test');
  console.log(`📍 Base URL: ${BASE_URL}`);
  console.log('👤 Authenticating admin user...');
  
  const token = authenticate();
  
  if (!token) {
    throw new Error('Failed to authenticate admin user');
  }
  
  console.log('✅ Admin authentication successful');
  console.log('⏱️  Test duration: 10 minutes (1m ramp-up + 5m sustained + 1m ramp-down)');
  console.log('👥 Max Virtual Users: 50');
  console.log('📊 Module weights: Admin(14%), Security(10%), Parametric(5%), Notification(5%), Log(1%), Integration(30%), Planning(30%)');
  
  return { 
    token,
    baseUrl: BASE_URL,
    createdResourceIds: {
      courses: [],
      teachers: [],
      students: [],
      admins: [],
      programs: [],
      sections: [],
    }
  };
}

/**
 * Main test function - runs for each VU iteration
 * @param {Object} data - Shared data from setup()
 */
export default function (data) {
  const { token, baseUrl } = data;
  
  if (!token) {
    console.error('❌ No authentication token available');
    return;
  }

  // Weighted random scenario selection
  // Admin: 14%, Security: 10%, Parametric: 5%, Notification: 5%, Log: 1%, Integration: 30%, Planning: 30%
  const scenario = weightedRandom([
    { weight: 14, name: 'admin', fn: AdminScenario.default },
    { weight: 10, name: 'security', fn: SecurityScenario.default },
    { weight: 5, name: 'parametric', fn: ParametricScenario.default },
    { weight: 5, name: 'notification', fn: NotificationScenario.default },
    { weight: 1, name: 'log', fn: LogScenario.default },
    { weight: 30, name: 'integration', fn: IntegrationScenario.default },
    { weight: 30, name: 'planning', fn: PlanningScenario.default },
  ]);
  
  // Execute selected scenario
  if (scenario.weight > 0) {
    scenario.fn(token, baseUrl, data);
    operationCounter.add(1, { module: scenario.name });
  }
  
  // Think time (realistic user behavior)
  sleep(Math.random() * 2 + 1); // Random 1-3 seconds
}

/**
 * Teardown function - runs once after test
 * @param {Object} data - Shared data from setup()
 */
export function teardown(data) {
  console.log('\n🏁 Load Test Complete');
  console.log('📊 Summary:');
  console.log(`   - Created Courses: ${data.createdResourceIds.courses.length}`);
  console.log(`   - Created Teachers: ${data.createdResourceIds.teachers.length}`);
  console.log(`   - Created Students: ${data.createdResourceIds.students.length}`);
  console.log(`   - Created Admins: ${data.createdResourceIds.admins.length}`);
  console.log(`   - Created Programs: ${data.createdResourceIds.programs.length}`);
  console.log(`   - Created Sections: ${data.createdResourceIds.sections.length}`);
  console.log('\n✨ Test execution finished successfully');
}
