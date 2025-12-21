# Content Microservice API Documentation

## Overview
Base URL: `/api/content`

## Content Management

### Publish Content
Upload text and media (image/video) to publish new content.

- **URL**: `/publish`
- **Method**: `POST`
- **Headers**:
  - `Authorization`: `Bearer <token>`
  - `Content-Type`: `multipart/form-data`
- **Request Parameters**:
  - `title` (String, Optional): The content title (Required for ARTICLE).
  - `text` (String): The content text.
  - `contentType` (String, Optional): `POST` (default), `ARTICLE`, `VIDEO`.
  - `file` (File, Optional): Image or Video file.
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "title": "My Article",
    "text": "Hello World",
    "contentType": "ARTICLE",
    "mediaUrl": "https://s3...",
    "status": "PENDING"
  }
  ```

### Get Content List
Get a list of content.
*   For **Authors**: Returns all published content AND all content authored by the requester (including PENDING/REJECTED).
*   For **Others**: Returns only PUBLISHED content.

- **URL**: `/list`
- **Method**: `GET`
- **Response**: `200 OK`
  ```json
  [
    {
      "id": 1,
      "text": "Hello World",
      "mediaUrl": "https://s3...",
      "status": "PUBLISHED",
      "authorId": 101
    }
  ]
  ```

## Content Review (Admin)

### Get Pending Review Tasks
- **URL**: `/review/tasks`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer <admin_token>`
- **Response**:
  ```json
  [
    {
      "taskId": "2501",
      "contentId": 1,
      "description": "Review content from user 101"
    }
  ]
  ```

### Complete Review
- **URL**: `/review/tasks/{taskId}`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "approved": true,
    "comment": "Looks good"
  }
  ```
