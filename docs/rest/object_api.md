# Object API

## Overview

API for creating, retrieving, searching and invoking methods on objects.

## API Data Types

| Type(s)                                           | API Representation          |
|:--------------------------------------------------|:----------------------------|
| `byte`, `short`, `int`, `long`, `float`, `double` | Number                      |
| `bool`                                            | Boolean                     |
| `char`, `string`                                  | String                      |
| `value object`                                    | Object                      |
| `array`                                           | Array                       |
| `reference`                                       | ID String                   |
| `enum constant`                                   | Enum constant name (String) |
| `child objects`                                   | Array of Objects            |

## Endpoints
All endpoints require an `X-App-ID: {app-id}` header. Responses use the [Result Schema](#result).

### Object Creation

* **`PUT /api/{class-path}`**
* `{class-path}`: Qualified class name (e.g., org.kiwi.DemoClass) converted to a lowercase-hyphenated path (e.g., org/kiwi/demo-class).
* **Request Body:** JSON object of constructor arguments and `public` child objects (arrays, keyed by child class names, recursive)
* **Response Data:**: Created object's ID (`String`)
* **Example**:
    ```http
    PUT /api/org/kiwi/demo/order
    X-App-ID: {app-id}
    Content-Type: application/json
  
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
    * Response
    ```json
    {
      "code": 0,
      "data": "{id}"
    } 
    ```

### Object Retrieval
* **`GET /api/{object-id}`**
* **Response Data:** JSON object containing `public` fields and `public` child objects (arrays, keyed by child class names, recursive).
* **Example**:
    ```http
    GET /api/{order-id}
    X-App-ID: {app-id}
    ```
    * Response
    ```json
    {
      "code": 0,
      "data": {
        "price": 14000,
        "confirmed": false,
        "Item": [
          {
            "product": "{product-id}",
            "quantity": 1
          }
        ]
      }
    }
    ```

### Search
* **`POST /api/search/{class-path}`**
    * `{class-path}`: See [Object Creation](#object-creation)
* **Request Body:**
  
  | Field            | Type        | Description                                                                                                                      |
  |:-----------------|:------------|:---------------------------------------------------------------------------------------------------------------------------------|
  | `criteria`       | JSON object | Zero or more `public` fields. Numeric fields support range queries (`[min, max]`)                                                |
  | `page`           | `int`       | Page number (default 1)                                                                                                          |
  | `pageSize`       | `int`       | Number of items per page (default 20)                                                                                            |
  | `includeObjects` | `boolean`   | `true` to return objects, `false` to return IDs (default false)                                                                  |
* **Response Data:**

  | Field    | Type     | Description                                               |
  |:---------|:---------|:----------------------------------------------------------|
  | `data`   | `any[]`  | Current page items (IDs or objects, per `includeObjects)` |
  | `total`  | `long`   | Total number of items across all pages                    |
*   *Note:* Objects in search results do not contain child objects.
* **Example:**
    ```http
    POST /api/search/org/kiwi/demo/product
    X-App-ID: {app-id}
    Content-Type: application/json
  
    {
      "criteria": {
        "name": "MacBook",
        "price": [10000, 150000]
      },
      "includeObjects": true
    }
    ```
    * Response:
    ```json
    {
      "code": 0,
      "data": {
        "data": [
          {
            "name": "MacBook Pro",
            "price": 14000,
            "stock": 100
          }   
        ],
        "total": 1 
      }
    }
    ```

### Method Invocation
*   **`POST /api/{receiver}/{method-name}`**
    *    `{receiver}`: The target of the invocation. This can be:
          * An `{object-id}` for instance methods.
          * A Bean Name for bean methods (the lowercase-hyphenated simple class name, e.g., user-service for UserService).
    *    `{method-name}`: `CamelCase` (e.g., `reduceStock`) converted to `lowercase-hyphenated` (e.g., `reduce-stock`)
*   **Request Body:** JSON object containing method arguments.
*   **Response Data:** Method's return value.
*   **Example:**
      ```http
      POST /api/{product-id}/reduce-stock
      X-App-ID: {app-id}
      Content-Type: application/json
    
      { 
        "quantity": 1
      }
      ```
      * Response:
      ```json
      { 
        "code": 0 
      }
      ```

## Common Data Structures

### `Result`

A generic wrapper for API responses.

| Field | Type | Description                           |
| :--- | :------- |:--------------------------------------|
| `code`  | `int`| `0` for success, non-zero for failure |
| `message` | `string` | Error message if `code` is non-zero   | 
| `data` | `T` | The actual response data              |










