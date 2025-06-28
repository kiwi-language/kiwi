# Object API

## Overview

API for managing Kiwi objects: save, retrieve, delete, search, and invoke methods.

## Code Example

This document uses the following Kiwi program for all examples:

```kotlin
package org.kiwi.demo

class Order(val price: Money) {

    var confirmed = false
    
    class Item(
        val product: Product, 
        val quantity: int
    )

}

value class Money(
    val amount: double, 
    val currency: Currency
)

enum Currency {
        USD,
        CNY,
        EURO,
        ;
}

class Product(
    @Summary
    var name: string,
    var price: Money,
    var stock: int
) {

    var active = true
    
    fn reduceStock(quantity: int) {
        require(active && stock >= quantity, "Inactive or out of stock")
        stock -= quantity
    }
    
}
```

## Conventions

### API Data Types

| Type(s)                                            | API Representation                                  |
|:---------------------------------------------------|:----------------------------------------------------|
| `byte`, `short`, `int`, `long`, `float`, `double`  | Number                                              |
| `bool`                                             | Boolean                                             |
| `char`, `string`                                   | String                                              |
| `array`                                            | Array                                               |
| `object`                                           | See [Object Representation](#object-representation) |

### Object Representation

Kiwi objects are represented as JSON objects with the following fields. The specific fields available depend on the context (see notes below).

| Field      | Type        | Description                                                                               |
|:-----------|:------------|:------------------------------------------------------------------------------------------|
| `id`       | `string`    | Object ID                                                                                 |
| `type`     | `string`    | Qualified class name (e.g., `org.kiwi.demo.Order`).                                       |
| `name`     | `string`    | Enum constant name or bean name (e.g., `USD`, `orderService`)                             |
| `summary`  | `string`    | Brief text for display (e.g., `MacBook Pro` for a product)                                |
| `fields`   | JSON object | Key-value pairs of public fields or constructor arguments (during creation)               |
| `children` | JSON object | Public child objects, organized as arrays keyed by child class name (recursive structure) |

**Field Availability by Context:**
* **Creation Request:** `type`, `fields`, `children`.
* **Update Request:** `id`, `type`, `fields`, `children`.
* **Retrieval Response:** `id`, `type`, `summary`, `fields`, `children`.
* **Search Result Item:** `id`, `type`, `summary`, `fields`.
* **Reference (Request Body):** `id`.
* **Reference (Response Body):** `id`, `type`, `summary`.
* **Value Object:** `type`, `summary` (response), `fields`.
* **Enum Constant:** `type`, `summary` (response), `name`.
* **Bean:** `name`.

**Example `Order` Object (Retrieval):**
```json
{
  "id": "...",
  "type": "org.kiwi.demo.Order",
  "fields": {
    "price": {
      "type": "org.kiwi.demo.Money",
      "fields": {
        "amount": 14000,
        "currency": {
          "type": "org.kiwi.demo.Currency",
          "name": "CNY"
        }
      }
    },
    "confirmed": false
  },
  "children": {
    "Item": [
      {
        "id": "...",
        "type": "org.kiwi.demo.Order.Item",
        "fields": {
          "product": {
            "id": "...",
            "type": "org.kiwi.demo.Product",
            "summary": "MackBook Pro"
          },
          "quantity": 1
        },
        "children": {}
      }
    ]
  }
}
```

## Endpoints
All endpoints require an `X-App-ID: {app-id}` header. Responses use the [Result Schema](#result).

### Save Object

* **`POST /object`**
* **Request Body:**

  | Field      | Type        | Description                                     |
     |:-----------|:------------|:------------------------------------------------|
  | `object`   | JSON Object | [Object Representation](#object-representation) |
* **Response Data:**: Saved object's ID (`String`)
* **Create Example**:
  * Request
    ```http
    POST /object
    X-App-ID: {app-id}
    Content-Type: application/json
  
    {
      "object": { 
        "type": "org.kiwi.demo.Product",
        "fields": {
          "name": "MacBook Pro",
          "price": {
            "type": "org.kiwi.demo.Money",
            "fields": {
              "amount": 14000,
              "currency": {
                "type": "org.kiwi.demo.Currency",
                "name": "CNY"
              }
            }
          },
          "stock": 100
        },
        "children": {}
      }
    }
    ```
  * Response
    ```json
    {
      "code": 0,
      "data": "{id}"
    } 
    ```
* **Update Example**:
  * Request
    ```http
    POST /object
    X-App-ID: {app-id}
    Content-Type: application/json
  
    {
      "object": { 
        "id": "..."
        "type": "org.kiwi.demo.Product",
        "fields": {
          "name": "MacBook Pro",
          "price": {
            "type": "org.kiwi.demo.Money",
            "fields": {
              "amount": 14000,
              "currency": {
                "type": "org.kiwi.demo.Currency",
                "name": "CNY"
              }
            }
          },
          "stock": 100,
          "active": true
        },
        "children": {}
      }
    }
    ```
  * Response
    ```json
    {
      "code": 0,
      "data": "{id}"
    } 
    ```


### Get Object
* **`GET /object/{id}`**
* **Response Data**: [Object Representation](#object-representation)

* **Example**:
  * Request
    ```http
    GET /object/{product-id}
    X-App-ID: {app-id}
    ```
  * Response
    ```json
    {
      "code": 0,
      "data": {
        "id": "...",
        "type": "org.kiwi.demo.Product",
        "fields": {
          "name": "MacBook Pro",
          "price": {
            "type": "org.kiwi.demo.Money",
            "amount": 14000,
            "currency": {
              "type": "org.kiwi.demo.Currency",
              "name": "CNY"
            }
          },
          "stock": 100,
          "active": true
        },
        "children": {}
      }
    }
    ```

### Multi-get Objects
*   **`POST /object/multi-get`**
*   **Request Body:**

| Field | Type | Description |
|:---|:---|:---|
| `ids` | `string[]` | An array of object IDs to retrieve. |
| `excludeChildren` | `bool` | Optional. If `true`, the response will exclude child objects. Defaults to `false`. |
| `excludeFields` | `bool` | Optional. If `true`, the `fields` object will be excluded from the response for each retrieved object. Defaults to `false`. |

*   **Response Data:** A JSON array containing the [Object Representation](#object-representation) for each requested ID. The order of objects in the response array corresponds to the order of IDs in the request body.

* **Example:**
  * Request
    ```http
    POST /object/multi-get
    X-App-ID: {app-id}
    Content-Type: application/json
  
    {
      "ids": ["{order-id-1}", "{order-id-2}"]
    }
    ```
  * Response
    ```json
    {
      "code": 0,
      "data": [
        {
          "id": "{order-id-1}",
          "type": "org.kiwi.demo.Order",
          "fields": {
            "price": {
              "type": "org.kiwi.demo.Money",
              "fields": { "amount": 14000, "currency": { "type": "org.kiwi.demo.Currency", "name": "CNY" } }
            },
            "confirmed": true
          },
          "children": {
            "Item": [{
              "id": "{item-id-1}",
              "type": "org.kiwi.demo.Order.Item",
              "fields": {
                "product": { "id": "{product-id-1}", "type": "org.kiwi.demo.Product", "summary": "MacBook Pro" },
                "quantity": 1
              },
              "children": {}
            }]
          }
        },
        {
          "id": "{order-id-2}",
          "type": "org.kiwi.demo.Order",
          "fields": {
            "price": {
              "type": "org.kiwi.demo.Money",
              "fields": { "amount": 800, "currency": { "type": "org.kiwi.demo.Currency", "name": "USD" } }
            },
            "confirmed": false
          },
          "children": {
            "Item": [{
              "id": "{item-id-2}",
              "type": "org.kiwi.demo.Order.Item",
              "fields": {
                "product": { "id": "{product-id-2}", "type": "org.kiwi.demo.Product", "summary": "Magic Mouse" },
                "quantity": 2
              },
              "children": {}
            }]
          }
        }
      ]
    }
    ```

### Delete Object
* **`DELETE /object/{id}`**
* **Response Data**: None

* **Example**:
  * Request
    ```http
    DELETE /object/{product-id}
    X-App-ID: {app-id}
    ```
  * Response
    ```json
    {
      "code": 0
    }
    ```

### Search Objects
* **`POST /object/search`**
* **Request Body:**

  | Field            | Type          | Description                                                                                                             |
    |:-----------------|:--------------|:------------------------------------------------------------------------------------------------------------------------|
  | `type`           | `string`      | Qualified class name                                                                                                    |
  | `criteria`       | JSON object   | Zero or more `public` fields. Numeric fields support range queries (`[min, max]`)                                       |
  | `page`           | `int`         | Page number (default `1`)                                                                                               |
  | `pageSize`       | `int`         | Number of items per page (default `20`)                                                                                 |
  | `newlyCreatedId` | `string`      | Newly created object ID. Use for searches immediately after creation to ensure inclusion despite potential system lag.  |
* **Response Data:**

  | Field   | Type               | Description                                                                |
    |:--------|:-------------------|:---------------------------------------------------------------------------|
  | `items` | JSON object array  | Current page items (See [Object Representation](#object-representation))   |
  | `total` | `long`             | Total number of items across all pages                                     |
* **Example:**
  * Request
    ```http
    POST /object/search
    X-App-ID: {app-id}
    Content-Type: application/json
  
    {
      "type": "org.kiwi.demo.Order"
      "criteria": {
        "name": "MacBook",
        "stock": [1, 200],
        "active": true
      }
    }
    ```
  * Response:
    ```json
    {
      "code": 0,
      "data": {
        "items": [
          {
            "id": "...",
            "type": "org.kiwi.demo.Product",
            "fields": {
              "name": "MacBook Pro",
              "price": {
                "type": "org.kiwi.demo.Money",
                "amount": 14000,
                "currency": {
                  "type": "org.kiwi.demo.Currency",
                  "name": "CNY"
                }
              },
              "stock": 100,
              "active": true
            }
          }   
        ],
        "total": 1 
      }
    }
    ```

### Invoke Method
*   **`POST /object/invoke`**
*   **Request Body:**

    | Field       | Type        | Description                         |
        |:------------|:------------|:------------------------------------|
    | `receiver`  | JSON Object | Target object reference             |
    | `method`    | `string`    | Method name                         |
    | `arguments` | JSON Object | Key-value pairs of method arguments |

*   **Response Data:** Method's return value (if any).
*   **Example:**
  * Request
      ```http
      POST /object/invoke
      X-App-ID: {app-id}
      Content-Type: application/json
    
      { 
        "receiver": {
          "id": "..."
        },
        "method": "reduceStock",
        "arguments": {
          "quantity": 1
        }
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

| Field     | Type     | Description                           |
|:----------|:---------|:--------------------------------------|
| `code`    | `int`    | `0` for success, non-zero for failure |
| `message` | `string` | Error message if `code` is non-zero   | 
| `data`    | `T`      | The actual response data              |