# Login & Application Management API

## Overview

This API manages user authentication and application lifecycle. Users first log into the central **platform application (ID: 2)**. Once authenticated on the platform, they can "enter" other user applications they are members of.

## Common Data Structures

*   **`Result<T>`**: Standard API response wrapper.

    | Field     | Type     | Description                           |
    |:----------|:---------|:--------------------------------------|
    | `code`    | `int`    | `0` for success, else error code      |
    | `message` | `string` | Error message when `code` is non-zero |
    | `data`    | `T`      | Response data                         |
*   **`LoginInfo`**: Information returned upon successful login/entry.

    | Field   | Type   | Description    |
    |:--------|:-------|:---------------|
    | `appId` | `long` | Application ID |
    | `userId`| `string`| User ID        |
*   **`Application`**: Represents an application.

    | Field   | Type   | Description        |
    |:--------|:-------|:-------------------|
    | `id`    | `long` | Application ID     |
    | `name`  | `string`| Application Name   |
    | `ownerId`| `long` | Application owner ID |
*   **`Page<T>`**: Represents a paginated list of items.
* 
    | Field   | Type     | Description                             |
    |:--------|:---------|:----------------------------------------|
    | `data`  | `T[]`      | Array of items for the current page     |
    | `total` | `long`   | Total number of items across all pages  |

## Authentication

*   Endpoints generally require a valid `token_{app_id}` cookie from a previous login or "enter app" action, relevant to the context (platform or specific app).
*   The "Enter Application" endpoint specifically requires being logged into the platform (i.e., having a `token_2` cookie and sending `X-App-ID: 2`).

## Endpoints

### 1. Login
Logs a user into an application (typically the platform).

*   `POST /login`
*   **Request Body:**

    | Field     | Type   | Description                                  |
    |:----------|:-------|:---------------------------------------------|
    | `appId`   | `long` | Application ID (e.g., `2` for platform)      |
    | `loginName`| `string`| User name                                    |
    | `password` | `string`| Password                                     |
*   **Response:** `Result<LoginInfo>`
*   **Cookie:** Sets `token_{appId}` (using `appId` from request).
*   **Example Request:**
    ```json
    { "appId": 2, "loginName": "demo", "password": "123456" }
    ```

### 2. Logout
Logs the current user out.

*   `POST /logout`
*   **Response:** `Result<Void>`
    *   *Note: This invalidates the current `token_{app_id}` cookie.*

### 3. List Applications
Retrieves a paginated list of applications.

*   `GET /app`
*   **Query Parameters:**

    | Parameter  | Required | Default | Description                         |
    |:-----------|:---------|:--------|:------------------------------------|
    | `page`     | Yes      | `1`     | Page number                         |
    | `pageSize` | Yes      | `20`    | Number of items per page            |
    | `searchText`| No       |         | Filter applications by name         |
*   **Response:** `Result<Page<Application>>`

### 4. Save Application
Creates a new application or updates an existing one.

*   `POST /app`
*   **Request Body:** `Application`
    *   If `id` is `null` or omitted: Creates a new application.
    *   If `id` is provided: Updates the existing application with that ID.
*   **Response:** `Result<Long>` (data is the created/updated application's ID)

### 5. Delete Application
Deletes an application.

*   `DELETE /app/{id}`
*   **Path Parameter:** `id` (long) - The ID of the application to delete.
*   **Response:** `Result<Void>`

### 6. Enter Application
Allows a platform-logged-in user to enter a specific user application.

*   `POST /platform-user/enter-app/{app-id}`
*   **Path Parameter:** `app-id` (long) - The ID of the application to enter.
*   **Headers:** `X-App-ID: 2` (caller must be in platform context).
*   **Authentication:** Requires prior login to the platform (appId: 2).
*   **Response:** `Result<LoginInfo>` (for the entered application).
*   **Cookie:** Sets `token_{app-id}` (using `app-id` from path parameter).

---