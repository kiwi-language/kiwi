# Object API

API for creating, retrieving, and invoking methods on objects.

## Name-to-Path Conversion
Convert names (e.g., foo.bar.BazService) to paths (e.g., foo/bar/baz-service):

1.   Replace `.` with `/`.
2.   Convert `CamelCase` to `lowercase-hypenated`.

## API Data Types

| Type(s)        | API Representation         |
| :---------------------- | :------------------------- |
| `byte`, `short`, `int`, `long`, `float`, `double` | Number    |
| `bool`                  | Boolean   |
| `char`, `string`        | String     |
| `value object`          | Object     |
| `array`                 | Array       |
| `reference`             | ID String  |
| `child objects`         | Array of Objects         |

## Endpoints
All endpoints require an `X-App-ID: {app-id}` header. Responses use the [Result Schema](#result).

### Object Creation

* **`PUT /api/{class-path}`**
* **Request Body(JSON):**
    * Constructor arguments (key-value).
    * `public` child objects (arrays, keyed by child class names, recursive).
    * Example: `PUT /org/kiwi/demo/order`
        ```json
        {
           "price": 14000,
           "Item": [
              {
                 "product": "{product-id}",
                 "quantity": 1
              }
           ]
        }
        ```

### Object Retrieval
* **`GET /api/{object-id}`**
* **Response:** `Result<object>`.
* **Response Body:**
    * `public` fields (key-value).
    * `public` child objects (arrays, keyed by child class names, recursive).
    * Example: `GET /api/{order-id}`
        ```json
        {
            "price": 14000,
            "confirmed": false,
            "Item": [
                {
                    "product": "{product-id}",
                    "quantity": 1
                }
            ]
        }
        ```

### Method Invocation
*   **`POST /api/{receiver}/{method-name}`**
    *   `{receiver}`: Object ID (instance methods), or Bean Name (bean methods).
*   **Request Body (JSON):** Method arguments.
*   **Response:** `Result<any>` (data is the method's return value, see [API Data Types](#api-data-types)).
    *   Example: `POST /api/{product-id}/reduce-stock`
          ```json
          { 
              "quantity": 1
          }
          ```
          ```json
          { 
              "code": 0 
          }
          ```

## Data Structures

### `Result`

A generic wrapper for API responses.

| Field | Type | Description                           |
| :--- | :------- |:--------------------------------------|
| `code`  | `int`| `0` for success, non-zero for failure |
| `message` | `string` | Error message if `code` is non-zero   | 
| `data` | `T` | The actual response data              |










