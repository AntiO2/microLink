# User Management Microservice (microlink-user)

This module handles user registration, authentication, and user-related workflows using Flowable.

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

### Flowable Workflow
We use **Flowable** to manage business processes.
*   **Definition**: `src/main/resources/processes/user-onboarding.bpmn20.xml` defines a simple approval process.
*   **Engine**: Automatically configured via `flowable-spring-boot-starter`.
*   **Integration**:
    *   **Auto-Trigger**: User registration automatically starts an `user-onboarding` process.
    *   **Business Key**: Processes are linked to `userId`.

## 3. Step-by-Step Verification Guide

### Step 1: Verify User Registration & Login (REST Functionality)

**1. Register a User**
```bash
curl -X POST http://localhost:8081/api/user/auth/register \
-H "Content-Type: application/json" \
-d '{"username": "testuser", "nickname": "Test Nick", "email": "test@example.com", "password": "password123"}'
```
*Expected Output*: `{"code":200, "message":"User registered successfully!", "data": {...}}`

**2. Login**
```bash
curl -X POST http://localhost:8081/api/user/auth/login \
-H "Content-Type: application/json" \
-d '{"username": "testuser", "password": "password123"}'
```
*Expected Output*: JSON containing `data.token`, `data.userId`, `data.expiresIn`. **Copy this token.**

**3. Get User Profile**
```bash
curl -X GET http://localhost:8081/api/user/me \
-H "Authorization: Bearer <YOUR_TOKEN>"
```
*Expected Output*: User details JSON with extended fields.

**4. Update User Profile**
```bash
curl -X PATCH http://localhost:8081/api/user/me \
-H "Authorization: Bearer <YOUR_TOKEN>" \
-H "Content-Type: application/json" \
-d '{"bio": "New Bio", "avatarUrl": "http://example.com/avatar.jpg"}'
```
*Expected Output*: Updated user details.

### Step 2: Verify Workflow Functionality

**1. Verify Auto-Started Process**
Registration automatically started a process. You can verify by checking tasks.

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
*Expected Output*: `{"code":200, "message":"Task completed"}`

**4. My Tasks**
Check tasks assigned to the current user.
```bash
curl -X GET "http://localhost:8081/api/user/process/my-tasks" \
-H "Authorization: Bearer <YOUR_TOKEN>"
```

## 4. Running Tests
To verify the code integrity (uses in-memory H2 database):
```bash
mvn test
```
