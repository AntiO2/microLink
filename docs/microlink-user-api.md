# User Management Microservice API Documentation

## Overview
Base URL: `/api/user`

## Authentication

### Register User
Register a new user in the system.

- **URL**: `/auth/register`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "username": "john_doe",
    "password": "securePassword123",
    "email": "john@example.com"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "message": "User registered successfully"
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
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "john_doe",
    "roles": ["ROLE_USER"]
  }
  ```

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
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"]
  }
  ```

## Workflow (Activiti)

### Start Onboarding Process
Start a user onboarding workflow process (Demo for Activiti integration).

- **URL**: `/process/start`
- **Method**: `POST`
- **Headers**:
  - `Authorization`: `Bearer <token>`
- **Response**: `200 OK`
  ```json
  {
    "processId": "2501",
    "message": "Onboarding process started for user: john_doe"
  }
  ```
