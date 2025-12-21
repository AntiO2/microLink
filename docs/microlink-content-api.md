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
    "width": 1920,
    "height": 1080,
    "size": 102400,
    "contentId": null
  }
  ```

### Publish Content
Publish new content.

- **URL**: `/publish`
- **Method**: `POST`
- **Headers**:
  - `Authorization`: `Bearer <token>`
  - `Content-Type`: `application/json`
- **Request Body** (JSON):
  ```json
  {
    "title": "My Article",
    "content": "Hello World...",
    "contentType": "ARTICLE",
    "summary": "Short summary",
    "coverId": 101,
    "mainMediaId": 102,
    "mediaIds": [103, 104]
  }
  ```
  *   `contentType`: `POST` (default), `ARTICLE`, `VIDEO`.
  *   `coverId`: ID of the cover image (uploaded via `/upload`).
  *   `mainMediaId`: ID of the main video (uploaded via `/upload`).
  *   `mediaIds`: List of IDs for other images (uploaded via `/upload`).

- **Response**: `201 Created`
  ```json
  {
    "id": 1,
    "title": "My Article",
    "status": "PENDING",
    ...
  }
  ```

### Get Content List
Get a list of content.
*   For **Authors**: Returns all published content AND all content authored by the requester (including PENDING/REJECTED).
*   For **Others**: Returns only PUBLISHED content.

- **URL**: `/list`
- **Method**: `GET`
- **Parameters**:
    - `page` (int, default 0): Page number.
    - `size` (int, default 10): Page size.
    - `type` (String, optional): Filter by type (`POST`, `ARTICLE`, `VIDEO`).
    - `status` (String, optional): Filter by status (`PUBLISHED`, `PENDING`, `REJECTED`).
- **Response**: `200 OK`
  ```json
  {
    "content": [
      {
        "id": 1,
        "title": "My Article",
        "summary": "Short summary",
        "coverUrl": "https://s3...",
        "status": "PUBLISHED",
        "createdAt": "2023-10-01T12:00:00",
        "author": {
            "id": "101",
            "nickname": "John Doe",
            "avatar": null
        }
      }
    ],
    "totalPages": 10,
    "totalElements": 100,
    "size": 10,
    "number": 0
  }
  ```

### Update Content
Update existing content. Only allowed for the author.

- **URL**: `/update/{id}`
- **Method**: `PUT`
- **Headers**:
  - `Authorization`: `Bearer <token>`
  - `Content-Type`: `application/json`
- **Request Body** (JSON): Same as `/publish`.
- **Response**: `200 OK`

### Delete Content
Delete content. Only allowed for the author (or admin).

- **URL**: `/{id}`
- **Method**: `DELETE`
- **Headers**:
  - `Authorization`: `Bearer <token>`
- **Response**: `204 No Content`

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
