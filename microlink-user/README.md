# User Management Microservice (microlink-user)

This module handles user registration, authentication, and user-related workflows using Activiti.

## 1. Prerequisites & Installation

### Prerequisites
*   **Java 21**
*   **Maven**
*   **Docker** (for MySQL)

### Setup Database
Start a MySQL container using the project's compose file or manually:
```bash
# In project root
docker compose up -d mysql
```
Ensure the database `mydatabase` exists.

### Build & Run
```bash
cd microlink-user
mvn clean install
mvn spring-boot:run
```
The service will start on port **8081**.

## 2. Integration Details

### Spring Cloud / REST API
The service follows RESTful standards. All endpoints are prefixed with `/api/user`.
*   **Auth**: `/api/user/auth/*`
*   **User**: `/api/user/*`
*   **Process**: `/api/user/process/*`

### Activiti Workflow
We use **Activiti 7** to manage business processes.
*   **Definition**: `src/main/resources/processes/user-onboarding.bpmn20.xml` defines a simple approval process.
*   **Engine**: Automatically configured via `activiti-spring-boot-starter`.
*   **Verification**: We exposed endpoints to Start, Query, and Complete tasks.

## 3. Step-by-Step Verification Guide

Follow these steps to verify all functionalities.

### Step 1: Verify User Registration & Login (REST Functionality)

**1. Register a User**
```bash
curl -X POST http://localhost:8081/api/user/auth/register \
-H "Content-Type: application/json" \
-d '{"username": "testuser", "email": "test@example.com", "password": "password123"}'
```
*Expected Output*: `{"message":"User registered successfully!"}`

**2. Login**
```bash
curl -X POST http://localhost:8081/api/user/auth/login \
-H "Content-Type: application/json" \
-d '{"username": "testuser", "password": "password123"}'
```
*Expected Output*: JSON containing `"token": "eyJ..."`. **Copy this token.**

**3. Get User Profile**
```bash
curl -X GET http://localhost:8081/api/user/me \
-H "Authorization: Bearer <YOUR_TOKEN>"
```
*Expected Output*: User details JSON.

### Step 2: Verify Activiti Workflow (Workflow Functionality)

**1. Start Onboarding Process**
This starts the BPMN process defined in the project.
```bash
curl -X POST http://localhost:8081/api/user/process/start \
-H "Authorization: Bearer <YOUR_TOKEN>"
```
*Expected Output*: `{"processId":"...", "message":"Onboarding process started..."}`

**2. Verify Task Generation (Check "Approve User" Task)**
The process creates a task assigned to `admin`.
```bash
curl -X GET "http://localhost:8081/api/user/process/tasks?assignee=admin" \
-H "Authorization: Bearer <YOUR_TOKEN>"
```
*Expected Output*: A list containing a task named "Approve User". **Copy the task ID.**

**3. Complete the Task**
Simulate an admin approving the user.
```bash
curl -X POST http://localhost:8081/api/user/process/tasks/<TASK_ID>/complete \
-H "Authorization: Bearer <YOUR_TOKEN>"
```
*Expected Output*: `{"message":"Task completed"}`

**4. Verify Process End**
Query tasks again. The list should be empty.
```bash
curl -X GET "http://localhost:8081/api/user/process/tasks?assignee=admin" \
-H "Authorization: Bearer <YOUR_TOKEN>"
```
*Expected Output*: `[]`

## 4. Running Tests
To verify the code integrity (uses in-memory H2 database):
```bash
mvn test
```
