# Login & Application Management API

## Overview

This API manages user authentication and application lifecycle. All endpoints except `/login` require a logged-in user and an `X-App-ID: 2` header.

## Common Data Structures

*   **`Result<T>`**: Standard API response wrapper.

    | Field     | Type     | Description                           |
    |:----------|:---------|:--------------------------------------|
    | `code`    | `int`    | `0` for success, else error code      |
    | `message` | `string` | Error message when `code` is non-zero |
    | `data`    | `T`      | Response data                         |
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

## Endpoints

### 1. Login
Authenticates a user.

*   `POST /login`
*   **Request Body:**

    | Field     | Type   | Description |
    |:----------|:-------|:------------|
    | `appId`   | `long` | Must be `2` |
    | `loginName`| `string`| User name   |
    | `password` | `string`| Password    |
*   **Response:** `Result<LoginInfo>`
*   **`LoginInfo`**:

    | Field   | Type   | Description |
    |:--------|:-------|:------------|
    | `appId` | `long` | Will be `2` |
    | `userId`| `string`| User ID     |
*   **Cookie:** Sets `token_2`
*   **Example Request:**
    ```json
    { "appId": 2, "loginName": "demo", "password": "123456" }
    ```

### 2. Logout
Logs the current user out.

*   `POST /logout`
*   **Response:** `Result<Void>`
    *   *Note: This invalidates the `token_2` cookie.*

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