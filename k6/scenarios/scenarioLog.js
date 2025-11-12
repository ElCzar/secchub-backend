import http from 'k6/http';
import { check, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics for Log module
const logErrorRate = new Rate('log_errors');
const auditLogQueryDuration = new Trend('log_audit_query_duration_ms', true);
const auditLogByEmailDuration = new Trend('log_audit_by_email_duration_ms', true);
const auditLogByActionDuration = new Trend('log_audit_by_action_duration_ms', true);
const auditLogByDateRangeDuration = new Trend('log_audit_by_date_range_duration_ms', true);
const auditLogByMethodDuration = new Trend('log_audit_by_method_duration_ms', true);
const auditLogCombinedDuration = new Trend('log_audit_combined_duration_ms', true);

/**
 * Utility function for weighted random selection within Log module
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
 * Helper to consume NDJSON stream response
 * @param {Response} response - HTTP response
 * @returns {number} Number of log entries
 */
function consumeNDJSONStream(response) {
  if (response.status !== 200) return 0;
  
  const lines = response.body.split('\n').filter(line => line.trim().length > 0);
  return lines.length;
}

// ====================
// ALL AUDIT LOGS
// ====================

function getAllAuditLogs(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Accept': 'application/x-ndjson',
  };
  
  group('Log - Get All Audit Logs', () => {
    const response = http.get(
      `${baseUrl}/audit-logs`,
      { headers, tags: { operation: 'audit_log_get_all' } }
    );
    
    auditLogQueryDuration.add(response.timings.duration);
    
    const logCount = consumeNDJSONStream(response);
    
    const getAllSuccess = check(response, {
      'get all audit logs (200)': (r) => r.status === 200,
      'response is NDJSON': (r) => r.headers['Content-Type']?.includes('application/x-ndjson') || r.headers['content-type']?.includes('application/x-ndjson'),
      'logs returned': () => logCount > 0,
    });
    
    logErrorRate.add(!getAllSuccess);
  });
}

// ====================
// AUDIT LOGS BY EMAIL
// ====================

function getAuditLogsByEmail(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Accept': 'application/x-ndjson',
  };
  
  // Test with known admin email
  const testEmails = [
    'admin@secchub.com',
    'teacher1@secchub.com',
    'student1@secchub.com',
  ];
  
  const email = testEmails[Math.floor(Math.random() * testEmails.length)];
  
  group('Log - Get Audit Logs by Email', () => {
    const response = http.get(
      `${baseUrl}/audit-logs/email/${encodeURIComponent(email)}`,
      { headers, tags: { operation: 'audit_log_by_email' } }
    );
    
    auditLogByEmailDuration.add(response.timings.duration);

    const getByEmailSuccess = check(response, {
      'get audit logs by email (200)': (r) => r.status === 200,
      'response is NDJSON': (r) => r.headers['Content-Type']?.includes('application/x-ndjson') || r.headers['content-type']?.includes('application/x-ndjson'),
    });
    
    logErrorRate.add(!getByEmailSuccess);
  });
}

// ====================
// AUDIT LOGS BY ACTION
// ====================

function getAuditLogsByAction(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Accept': 'application/x-ndjson',
  };
  
  // Common action types
  const actions = ['CREATE', 'UPDATE', 'DELETE', 'READ'];
  const action = actions[Math.floor(Math.random() * actions.length)];
  
  group('Log - Get Audit Logs by Action', () => {
    const response = http.get(
      `${baseUrl}/audit-logs/action/${action}`,
      { headers, tags: { operation: 'audit_log_by_action' } }
    );
    
    auditLogByActionDuration.add(response.timings.duration);

    const getByActionSuccess = check(response, {
      'get audit logs by action (200)': (r) => r.status === 200,
      'response is NDJSON': (r) => r.headers['Content-Type']?.includes('application/x-ndjson') || r.headers['content-type']?.includes('application/x-ndjson'),
    });
    
    logErrorRate.add(!getByActionSuccess);
  });
}

// ====================
// AUDIT LOGS BY DATE RANGE
// ====================

function getAuditLogsByDateRange(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Accept': 'application/x-ndjson',
  };
  
  // Query last 24 hours
  const now = new Date();
  const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
  
  const startDate = yesterday.toISOString().split('.')[0]; // Remove milliseconds
  const endDate = now.toISOString().split('.')[0];
  
  group('Log - Get Audit Logs by Date Range', () => {
    const response = http.get(
      `${baseUrl}/audit-logs/date-range?start=${encodeURIComponent(startDate)}&end=${encodeURIComponent(endDate)}`,
      { headers, tags: { operation: 'audit_log_by_date_range' } }
    );
    
    auditLogByDateRangeDuration.add(response.timings.duration);

    const getByDateRangeSuccess = check(response, {
      'get audit logs by date range (200)': (r) => r.status === 200,
      'response is NDJSON': (r) => r.headers['Content-Type']?.includes('application/x-ndjson') || r.headers['content-type']?.includes('application/x-ndjson'),
    });
    
    logErrorRate.add(!getByDateRangeSuccess);
  });
}

// ====================
// AUDIT LOGS BY METHOD NAME
// ====================

function getAuditLogsByMethodName(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Accept': 'application/x-ndjson',
  };
  
  // Common method names from the application
  const methodNames = [
    'createCourse',
    'updateCourse',
    'deleteCourse',
    'getAllTeachers',
    'updateTeacher',
    'registerStudent',
  ];
  
  const methodName = methodNames[Math.floor(Math.random() * methodNames.length)];
  
  group('Log - Get Audit Logs by Method Name', () => {
    const response = http.get(
      `${baseUrl}/audit-logs/method/${encodeURIComponent(methodName)}`,
      { headers, tags: { operation: 'audit_log_by_method' } }
    );
    
    auditLogByMethodDuration.add(response.timings.duration);
    
    const getByMethodSuccess = check(response, {
      'get audit logs by method (200)': (r) => r.status === 200,
      'response is NDJSON': (r) => r.headers['Content-Type']?.includes('application/x-ndjson') || r.headers['content-type']?.includes('application/x-ndjson'),
    });
    
    logErrorRate.add(!getByMethodSuccess);
  });
}

// ====================
// AUDIT LOGS BY EMAIL AND ACTION (Combined)
// ====================

function getAuditLogsByEmailAndAction(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Accept': 'application/x-ndjson',
  };
  
  const email = 'admin@secchub.com';
  const actions = ['CREATE', 'UPDATE', 'DELETE'];
  const action = actions[Math.floor(Math.random() * actions.length)];
  
  group('Log - Get Audit Logs by Email and Action', () => {
    const response = http.get(
      `${baseUrl}/audit-logs/email/${encodeURIComponent(email)}/action/${action}`,
      { headers, tags: { operation: 'audit_log_by_email_and_action' } }
    );
    
    auditLogCombinedDuration.add(response.timings.duration);

    const getCombinedSuccess = check(response, {
      'get audit logs by email and action (200)': (r) => r.status === 200,
      'response is NDJSON': (r) => r.headers['Content-Type']?.includes('application/x-ndjson') || r.headers['content-type']?.includes('application/x-ndjson'),
    });
    
    logErrorRate.add(!getCombinedSuccess);
  });
}

// ====================
// MAIN LOG SCENARIO FUNCTION
// ====================

/**
 * Main entry point for Log module scenario
 * Distributes operations based on weights
 * @param {string} token - JWT authentication token
 * @param {string} baseUrl - Base URL of the API
 * @param {Object} data - Shared test data
 */
export default function (token, baseUrl, data) {
  // Weighted operation selection within Log module
  // AllLogs: 25%, ByEmail: 20%, ByAction: 20%, ByDateRange: 15%, ByMethod: 10%, Combined: 10%
  const operation = weightedRandom([
    { weight: 25, name: 'allLogs', fn: getAllAuditLogs },
    { weight: 20, name: 'byEmail', fn: getAuditLogsByEmail },
    { weight: 20, name: 'byAction', fn: getAuditLogsByAction },
    { weight: 15, name: 'byDateRange', fn: getAuditLogsByDateRange },
    { weight: 10, name: 'byMethod', fn: getAuditLogsByMethodName },
    { weight: 10, name: 'combined', fn: getAuditLogsByEmailAndAction },
  ]);
  
  // Execute selected operation
  operation.fn(token, baseUrl, data);
}
