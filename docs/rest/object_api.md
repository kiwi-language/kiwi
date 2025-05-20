# Object API

## Overview

API for creating, retrieving, searching and invoking methods on objects.

## Conventions

### API Data Types

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

### Path Naming
Symbol names (e.g., class, bean, method) in paths are converted:
1. `.` becomes `/`.
2. CamelCase becomes lowercase-hyphenated.

**Example**: org.kiw.demo.DemoClass -> org/kiwi/demo/demo-class

## Endpoints
All endpoints require an `X-App-ID: {app-id}` header. Responses use the [Result Schema](#result).

### Object Creation

* **`PUT /api/{qualified-class-name}`**
* **Request Body:** JSON object of constructor arguments and `public` child objects (arrays, keyed by child class names, recursive)
* **Response Data:**: Created object's ID (`String`)
* **Example**:
    ```kotlin
    package org.kiwi.demo
    
    class Order(val price: double) {
        
        var confirmed = false      
  
        class Item(
            val product: Product,
            val quantity: int
        )  
    }
    ```
    * Request
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
    * Request
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
* **`POST /api/search/{qualified-class-name}`**
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
    ```kotlin
    package org.kiwi.demo
  
    class Product(
        var name: string,
        var price: double,
        var stock: int
    ) {
        
        fn reduceStock(quantity: int) {
            require(stock >= quantity, "Out of stock")
            stock -= quantity  
        } 
    
    }
    ```
    * Request
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
          * Object ID for instance methods.
          * Bean name for bean methods.
*   **Request Body:** JSON object containing method arguments.
*   **Response Data:** Method's return value.
*   **Example:**
      * Request
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










