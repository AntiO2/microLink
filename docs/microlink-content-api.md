# Content Microservice API Documentation

## Overview
Base URL: `/api/content`

## Content Management

### Upload Media
Upload a media file (image/video) to be used in content.

- **URL**: `/upload`
- **Method**: `POST`
- **Headers**:
  - `Authorization`: `Bearer <token>`
  - `Content-Type`: `multipart/form-data`
- **Request Parameters**:
  - `file` (File): The media file.
- **Response**: `200 OK`
  ```json
  {
    "id": 101,
    "url": "https://s3...",
    "fileType": "IMAGE",
    "contentId": null
  }
  ```

### Publish Content
Publish new content.

- **URL**: `/publish`
- **Method**: `POST`
- **Headers**:
  - `Authorization`: `Bearer <token>`
  - `Content-Type`: `multipart/form-data`
- **Request Parameters**:
  - `title` (String, Optional): The content title.
  - `text` (String): The content text.
  - `contentType` (String, Optional): `POST` (default), `ARTICLE`, `VIDEO`.
  - `cover` (File, Optional): Cover image.
  - `media` (File, Optional): Main media file (e.g., Video).
  - `mediaIds` (List<Long>, Optional): List of media IDs (from `/upload`) used in the content.
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "title": "My Article",
    "text": "Hello World...",
    "contentType": "ARTICLE",
    "coverUrl": "https://s3...",
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
