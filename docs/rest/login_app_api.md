# Login & Application Management API

## Overview

This API manages user authentication and application lifecycle.

## Endpoints

All endpoints except `/login` require a logged-in user and an `X-App-ID: 2` header. Responses use the [Result Schema](#result).

### 1. Login
Authenticates a user.

*   `POST /login`
*   **Request Body:**

    | Field     | Type   | Description |
    |:----------|:-------|:------------|
    | `appId`   | `long` | Must be `2` |
    | `loginName`| `string`| User name   |
    | `password` | `string`| Password    |
*   **Response Data:** `LoginInfo`

*   **Cookie:** Sets `token_2`
*   **Example:**
    ```http
    POST /login
    Content-Type: application/json
    
    {
      "appId": 2, 
      "loginName": "demo", 
      "password": "123456" 
    }
    ```
    * Response:
    ```json
    {
      "code": 0,
      "data": {
        "appId": 2,
        "userId": "{user-id}"
      }
    }
    ```

### 2. Logout
Logs the current user out.

*   `POST /logout`
*   **Cookie:** Invalidates `token_2`
*   **Example:**
    ```http
    POST /logout
    X-App-ID: 2
    ```
    * Response
    ```json
    {
      "code": 0
    }
    ```
    
### 3. Get Login Info
Retrieves the current user's login details.

*  `GET /get-login-info`
*  **Response Data:** `LoginInfo` (Contains `appId` 2/`userId` if logged in, or `appId` -1 if not)
*  **Example:**
   ```http
   GET /get-login-info
   X-App-ID: 2
   ```
   ```json
   {
     "code": 0,
     "data": {
       "appId": 2,
       "userId": "{user-id}"
     }
   } 
   ```

### 4. List Applications
Retrieves a paginated list of applications.

*   `GET /app`
*   **Query Parameters:**

    | Parameter  | Required | Default | Description                         |
    |:-----------|:---------|:--------|:------------------------------------|
    | `page`     | Yes      | `1`     | Page number                         |
    | `pageSize` | Yes      | `20`    | Number of items per page            |
    | `searchText`| No       |         | Filter applications by name         |
*   **Response Data:** `Page<Application>`
*   **Example:**
    ```http
    GET /app?searchText=demo
    X-App-ID: 2
    ```
    * Response
    ```json
    {
      "code": 0,
      "data": {
        "items": [
          {
            "id": 1000002004,
            "name": "demo",
            "ownerId": "{user-id}"
          }   
        ],
        "total": 1
      }
    }
    ```

### 5. Save Application
Creates a new application or updates an existing one.

*   `POST /app`
*   **Request Body:** `Application`
    *   If `id` is `null` or omitted: Creates a new application.
    *   If `id` is provided: Updates the existing application with that ID.
    *   `ownerId` is not required in request.
*   **Response Data:** `long` (The application ID)
*   **Example:**
    ```http
    POST /app
    Content-Type: application/json
    X-App-ID: 2
    
    {
      "name": "shopping"
    }
    ```
    * Response
    ```json
    {
      "code": 0,
      "data": 1000002004
    }
    ```
### 6. Delete Application
Deletes an application.

*   `DELETE /app/{id}`
*   **Path Parameter:** `id` (long) - The ID of the application to delete.
*   **Example:**
    ```http
    DELETE /app/{id}
    X-App-ID: 2
    ```
    * Response
    ```json
    {
      "code": 0
    }
    ```

## Common Data Structures

### `Result`
Standard API response wrapper.

| Field      | Type     | Description                           |
|:-----------|:---------|:--------------------------------------|
| `code`     | `int`    | `0` for success, else error code      |
| `message`  | `string` | Error message when `code` is non-zero |
| `data`     | `T`      | Response data                         |

### `LoginInfo`
User login details.

| Field   | Type     | Description                   |
|:--------|:---------|:------------------------------|
| `appId` | `long`   | `2` if logged-in, `-1` if not |
| `userId`| `string` | User ID                       |

### `Application` 
Represents an application.

| Field     | Type     | Description          |
|:----------|:---------|:---------------------|
| `id`      | `long`   | Application ID       |
| `name`    | `string` | Application Name     |
| `ownerId` | `long`   | Application owner ID |

### `Page` 
Represents a paginated list of items.

| Field   | Type      | Description                             |
|:--------|:----------|:----------------------------------------|
| `items` | `T[]`     | Array of items for the current page     |
| `total` | `long`    | Total number of items across all pages  |

