# User Management Microservice API Documentation

## Overview
Base URL: `/api/user`

**Unified Response Format**:
All APIs return data in the following format:
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## Authentication

### Check Username
Check if a username is already taken.
- **URL**: `/auth/check-username?username=xxx`
- **Method**: `GET`
- **Response**: `200 OK` (Available) or `409 Conflict` (Taken)

### Register User
Register a new user. Automatically starts onboarding workflow.

- **URL**: `/auth/register`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "username": "john_doe",
    "nickname": "John",
    "password": "securePassword123",
    "email": "john@example.com"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "code": 200,
    "message": "User registered successfully!",
    "data": {
       "id": 1,
       "username": "john_doe",
       "nickname": "John",
       ...
    }
  }
  ```

### Login
Authenticate user and retrieve a JWT token.

- **URL**: `/auth/login`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "username": "john_doe",
    "password": "securePassword123"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "type": "Bearer",
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "roles": ["ROLE_USER"],
      "expiresIn": 86400
    }
  }
  ```

### Logout
Log out the current user.

- **URL**: `/auth/logout`
- **Method**: `POST`
- **Response**: `200 OK`

## User Management

### Get Current User Profile
Get details of the currently authenticated user.

- **URL**: `/me`
- **Method**: `GET`
- **Headers**:
  - `Authorization`: `Bearer <token>`
- **Response**: `200 OK`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "username": "john_doe",
      "nickname": "John",
      "email": "john@example.com",
      "avatarUrl": "...",
      "bio": "...",
      "lastLoginTime": "2023-10-27T10:00:00",
      "roles": ["ROLE_USER"]
    }
  }
  ```

### Update User Profile
Update nickname, avatar, bio, etc.

- **URL**: `/me`
- **Method**: `PATCH`
- **Headers**:
  - `Authorization`: `Bearer <token>`
- **Request Body**: (All fields optional)
  ```json
  {
    "nickname": "New Nickname",
    "bio": "New Bio",
    "avatarUrl": "http://...",
    "phoneNumber": "123456789"
  }
  ```
- **Response**: `200 OK` (Returns updated user object)

## Workflow (Activiti/Flowable)

### Start Onboarding Process
Manually start an onboarding process (usually auto-started by register).

- **URL**: `/process/start`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer <token>`

### My Tasks
Get tasks assigned to the current user.

- **URL**: `/process/my-tasks`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK`
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": "task_123",
        "name": "Approve User"
      }
    ]
  }
  ```

### Complete Task
Complete a task by ID.

- **URL**: `/process/tasks/{taskId}/complete`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer <token>`
