# Object API

API for creating, retrieving, and invoking methods on objects.

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
* `{class-path}`: Qualified class name (e.g., org.kiwi.DemoClass) converted to a lowercase-hyphenated path (e.g., org/kiwi/demo-class).
* **Request Body(JSON):**
    * Constructor arguments (key-value).
    * `public` child objects (arrays, keyed by child class names, recursive).
    * Example: `PUT /api/org/kiwi/demo/order`
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
    *    `{receiver}`: The target of the invocation. This can be:
          * An {object-id} for instance methods.
          * A Bean Name for bean methods (the lowercase-hyphenated simple class name, e.g., user-service for UserService).
    *    `{method-name}`: `CamelCase` (e.g., `reduceStock`) converted to `lowercase-hyphenated` (e.g., `reduce-stock`)
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










