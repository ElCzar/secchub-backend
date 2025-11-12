import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics for Parametric module
const parametricErrorRate = new Rate('parametric_errors');
const statusOperationDuration = new Trend('parametric_status_duration_ms', true);
const roleOperationDuration = new Trend('parametric_role_duration_ms', true);
const documentTypeOperationDuration = new Trend('parametric_document_type_duration_ms', true);
const employmentTypeOperationDuration = new Trend('parametric_employment_type_duration_ms', true);
const modalityOperationDuration = new Trend('parametric_modality_duration_ms', true);
const classroomTypeOperationDuration = new Trend('parametric_classroom_type_duration_ms', true);

/**
 * Utility function for weighted random selection within Parametric module
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
// STATUS OPERATIONS
// ====================

function statusOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Parametric - Status Operations', () => {
    // GET All Statuses
    let response = http.get(
      `${baseUrl}/parametric/statuses`,
      { headers, tags: { operation: 'status_get_all' } }
    );
    
    statusOperationDuration.add(response.timings.duration);
    
    const getAllSuccess = check(response, {
      'get all statuses (200)': (r) => r.status === 200,
      'statuses list is array': (r) => Array.isArray(r.json()),
      'statuses have id': (r) => r.json().length > 0 && r.json()[0].id !== undefined,
      'statuses have name': (r) => r.json().length > 0 && r.json()[0].name !== undefined,
    });
    
    parametricErrorRate.add(!getAllSuccess);
    
    sleep(0.2);
    
    // GET Status by ID
    const statusId = Math.floor(Math.random() * 5) + 1; // IDs 1-5
    response = http.get(
      `${baseUrl}/parametric/statuses/${statusId}`,
      { headers, tags: { operation: 'status_get_by_id' } }
    );
    
    statusOperationDuration.add(response.timings.duration);
    
    const getByIdSuccess = check(response, {
      'get status by id (200)': (r) => r.status === 200,
      'status name is string': (r) => typeof r.body === 'string' && r.body.length > 0,
    });
    
    parametricErrorRate.add(!getByIdSuccess);
  });
}

// ====================
// ROLE OPERATIONS
// ====================

function roleOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Parametric - Role Operations', () => {
    // GET All Roles
    let response = http.get(
      `${baseUrl}/parametric/roles`,
      { headers, tags: { operation: 'role_get_all' } }
    );
    
    roleOperationDuration.add(response.timings.duration);
    
    const getAllSuccess = check(response, {
      'get all roles (200)': (r) => r.status === 200,
      'roles list is array': (r) => Array.isArray(r.json()),
      'roles have id': (r) => r.json().length > 0 && r.json()[0].id !== undefined,
      'roles have name': (r) => r.json().length > 0 && r.json()[0].name !== undefined,
    });
    
    parametricErrorRate.add(!getAllSuccess);
    
    sleep(0.2);
    
    // GET Role by ID
    const roleId = Math.floor(Math.random() * 4) + 1; // IDs 1-4
    response = http.get(
      `${baseUrl}/parametric/roles/${roleId}`,
      { headers, tags: { operation: 'role_get_by_id' } }
    );
    
    roleOperationDuration.add(response.timings.duration);
    
    const getByIdSuccess = check(response, {
      'get role by id (200)': (r) => r.status === 200,
      'role name is string': (r) => typeof r.body === 'string' && r.body.length > 0,
    });
    
    parametricErrorRate.add(!getByIdSuccess);
  });
}

// ====================
// DOCUMENT TYPE OPERATIONS
// ====================

function documentTypeOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Parametric - Document Type Operations', () => {
    // GET All Document Types
    let response = http.get(
      `${baseUrl}/parametric/document-types`,
      { headers, tags: { operation: 'document_type_get_all' } }
    );
    
    documentTypeOperationDuration.add(response.timings.duration);
    
    const getAllSuccess = check(response, {
      'get all document types (200)': (r) => r.status === 200,
      'document types list is array': (r) => Array.isArray(r.json()),
      'document types have id': (r) => r.json().length > 0 && r.json()[0].id !== undefined,
      'document types have name': (r) => r.json().length > 0 && r.json()[0].name !== undefined,
    });
    
    parametricErrorRate.add(!getAllSuccess);
    
    sleep(0.2);
    
    // GET Document Type by ID
    const docTypeId = Math.floor(Math.random() * 3) + 1; // IDs 1-3
    response = http.get(
      `${baseUrl}/parametric/document-types/${docTypeId}`,
      { headers, tags: { operation: 'document_type_get_by_id' } }
    );
    
    documentTypeOperationDuration.add(response.timings.duration);
    
    const getByIdSuccess = check(response, {
      'get document type by id (200)': (r) => r.status === 200,
      'document type name is string': (r) => typeof r.body === 'string' && r.body.length > 0,
    });
    
    parametricErrorRate.add(!getByIdSuccess);
  });
}

// ====================
// EMPLOYMENT TYPE OPERATIONS
// ====================

function employmentTypeOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Parametric - Employment Type Operations', () => {
    // GET All Employment Types
    let response = http.get(
      `${baseUrl}/parametric/employment-types`,
      { headers, tags: { operation: 'employment_type_get_all' } }
    );
    
    employmentTypeOperationDuration.add(response.timings.duration);
    
    const getAllSuccess = check(response, {
      'get all employment types (200)': (r) => r.status === 200,
      'employment types list is array': (r) => Array.isArray(r.json()),
      'employment types have id': (r) => r.json().length > 0 && r.json()[0].id !== undefined,
      'employment types have name': (r) => r.json().length > 0 && r.json()[0].name !== undefined,
    });
    
    parametricErrorRate.add(!getAllSuccess);
    
    sleep(0.2);
    
    // GET Employment Type by ID
    const empTypeId = Math.floor(Math.random() * 2) + 1; // IDs 1-2
    response = http.get(
      `${baseUrl}/parametric/employment-types/${empTypeId}`,
      { headers, tags: { operation: 'employment_type_get_by_id' } }
    );
    
    employmentTypeOperationDuration.add(response.timings.duration);
    
    const getByIdSuccess = check(response, {
      'get employment type by id (200)': (r) => r.status === 200,
      'employment type name is string': (r) => typeof r.body === 'string' && r.body.length > 0,
    });
    
    parametricErrorRate.add(!getByIdSuccess);
  });
}

// ====================
// MODALITY OPERATIONS
// ====================

function modalityOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Parametric - Modality Operations', () => {
    // GET All Modalities
    let response = http.get(
      `${baseUrl}/parametric/modalities`,
      { headers, tags: { operation: 'modality_get_all' } }
    );
    
    modalityOperationDuration.add(response.timings.duration);
    
    const getAllSuccess = check(response, {
      'get all modalities (200)': (r) => r.status === 200,
      'modalities list is array': (r) => Array.isArray(r.json()),
      'modalities have id': (r) => r.json().length > 0 && r.json()[0].id !== undefined,
      'modalities have name': (r) => r.json().length > 0 && r.json()[0].name !== undefined,
    });
    
    parametricErrorRate.add(!getAllSuccess);
    
    sleep(0.2);
    
    // GET Modality by ID
    const modalityId = Math.floor(Math.random() * 2) + 1; // IDs 1-2
    response = http.get(
      `${baseUrl}/parametric/modalities/${modalityId}`,
      { headers, tags: { operation: 'modality_get_by_id' } }
    );
    
    modalityOperationDuration.add(response.timings.duration);
    
    const getByIdSuccess = check(response, {
      'get modality by id (200)': (r) => r.status === 200,
      'modality name is string': (r) => typeof r.body === 'string' && r.body.length > 0,
    });
    
    parametricErrorRate.add(!getByIdSuccess);
  });
}

// ====================
// CLASSROOM TYPE OPERATIONS
// ====================

function classroomTypeOperations(token, baseUrl) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
  
  group('Parametric - Classroom Type Operations', () => {
    // GET All Classroom Types
    let response = http.get(
      `${baseUrl}/parametric/classroom-types`,
      { headers, tags: { operation: 'classroom_type_get_all' } }
    );
    
    classroomTypeOperationDuration.add(response.timings.duration);
    
    const getAllSuccess = check(response, {
      'get all classroom types (200)': (r) => r.status === 200,
      'classroom types list is array': (r) => Array.isArray(r.json()),
      'classroom types have id': (r) => r.json().length > 0 && r.json()[0].id !== undefined,
      'classroom types have name': (r) => r.json().length > 0 && r.json()[0].name !== undefined,
    });
    
    parametricErrorRate.add(!getAllSuccess);
    
    sleep(0.2);
    
    // GET Classroom Type by ID
    const classroomTypeId = Math.floor(Math.random() * 3) + 1; // IDs 1-3
    response = http.get(
      `${baseUrl}/parametric/classroom-types/${classroomTypeId}`,
      { headers, tags: { operation: 'classroom_type_get_by_id' } }
    );
    
    classroomTypeOperationDuration.add(response.timings.duration);
    
    const getByIdSuccess = check(response, {
      'get classroom type by id (200)': (r) => r.status === 200,
      'classroom type name is string': (r) => typeof r.body === 'string' && r.body.length > 0,
    });
    
    parametricErrorRate.add(!getByIdSuccess);
  });
}

// ====================
// MAIN PARAMETRIC SCENARIO FUNCTION
// ====================

/**
 * Main entry point for Parametric module scenario
 * Distributes operations based on weights
 * @param {string} token - JWT authentication token
 * @param {string} baseUrl - Base URL of the API
 * @param {Object} data - Shared test data
 */
export default function (token, baseUrl, data) {
  // Weighted operation selection within Parametric module
  // Status: 20%, Role: 15%, DocumentType: 20%, EmploymentType: 15%, Modality: 15%, ClassroomType: 15%
  const operation = weightedRandom([
    { weight: 20, name: 'status', fn: statusOperations },
    { weight: 15, name: 'role', fn: roleOperations },
    { weight: 20, name: 'documentType', fn: documentTypeOperations },
    { weight: 15, name: 'employmentType', fn: employmentTypeOperations },
    { weight: 15, name: 'modality', fn: modalityOperations },
    { weight: 15, name: 'classroomType', fn: classroomTypeOperations },
  ]);
  
  // Execute selected operation
  operation.fn(token, baseUrl, data);
}
