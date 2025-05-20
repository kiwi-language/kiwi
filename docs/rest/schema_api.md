# Schema API

## Overview

Provides a GET /schema endpoint to retrieve structural information about defined types.

## Endpoint

*   `GET /schema`
*   **Header:** `X-App-ID: {app-id}`
*   **Response:** `Result<Response>` (See data structures below)

## Data Structures

### `Result`
A generic wrapper for API responses.

| Field     | Type     | Description                            |
|:----------|:---------|:---------------------------------------|
| `code`    | `int`    | `0` for success, non-zero for failure  |
| `message` | `string` | Error message if `code` is non-zero    |
| `data`    | `T`      | The actual response data               |

### `Response`
The main data payload for the schema endpoint.

| Field      | Type              | Description                                             |
|:-----------|:------------------|:--------------------------------------------------------|
| `classes`  | [Class](#class)[] | List of top-level class, interface, enum definitions.   |

### `Class`
Represents a class, interface, enum, or value class

| Field           | Type                                | Description                                                  |
|:----------------|:------------------------------------|:-------------------------------------------------------------|
| `access`        | `string`                            | Access modifier: `public`, `private`, `protected`, `package` |
| `tag`           | `string`                            | Type of definition: `class`, `interface`, `enum`, `value`    |
| `abstract`    | `bool`                              | True if the class is abstract                                |
| `name`          | `string`                            | Simple name of the class (e.g., `Baz`)                       |
| `qualifiedName` | `string`                            | Fully qualified name (e.g., `foo.bar.Baz`)                   |
| `superTypes`    | [ClassType](#classtype)[]           | List of direct supertypes                                    |
| `constructor`   | [Constructor](#constructor)         | Class constructor definition                                 |
| `fields`        | [Field](#field)[]                   | List of fields in the class                                  |
| `methods`       | [Method](#method)[]                 | List of methods in the class                                 |
| `classes`       | [Class](#class)[]                   | Nested class definitions                                     |
| `enumConstants` | [EnumConstant](#enumconstant)[]     | For `enum` types, lists the enum constants                   |
| `beanName`      | `string?`                           | Optional bean name; if present, indicates a bean class.      |
| `label`         | `string`                            | Display label                                                |

### `Constructor`
Represents a class constructor.

| Field         | Type                      | Description                       |
|:--------------|:--------------------------|:----------------------------------|
| `parameters`  | [Parameter](#parameter)[] | List of constructor parameters    |

### `Field`
Represents a class field.

| Field     | Type          | Description                                           |
|:----------|:--------------|:------------------------------------------------------|
| `access`  | `string`      | Field access modifier (see `Class.access` for values) |
| `name`    | `string`      | Field name                                            |
| `type`    | [Type](#type) | Field type                                            |
| `summary` | `boolean`     | True if this is the summary field                     |
| `label`   | `string`      | Display label                                         |

### `Method`
Represents a class method.

| Field        | Type                        | Description                                            |
|:-------------|:----------------------------|:-------------------------------------------------------|
| `access`     | `string`                    | Method access modifier (see `Class.access` for values) |
| `abstract`   | `bool`                      | True if the method is abstract                         |
| `name`       | `string`                    | Method name                                            |
| `parameters` | [Parameter](#parameter)[]   | List of method parameters                              |
| `returnType` | [Type](#type)               | Method return type                                     |
| `label`      | `string`                    | Display label                                          |

### `EnumConstant`
Represents a constant within an enum.

| Field  | Type      | Description               |
|:-------|:----------|:--------------------------|
| `name` | `string`  | Name of the enum constant |
| `label`| `string`  | Display label             |

### `Parameter`
Represents a parameter for a method or constructor.

| Field   | Type          | Description    |
|:--------|:--------------|:---------------|
| `name`  | `string`      | Parameter name |
| `type`  | [Type](#type) | Parameter type |
| `label` | `string`      | Display label  |

### `Type`
A polymorphic type descriptor.

| Field | Type   | Description                                                              |
|:------|:-------|:-------------------------------------------------------------------------|
| `kind`  | `string` | Discriminator field for the specific subtype (e.g., `primitive`, `class`). |

Subtypes: [PrimitiveType](#primitivetype), [ClassType](#classtype), [ArrayType](#arraytype), [UnionType](#uniontype)

### `PrimitiveType`
Represents a primitive type.

| Field  | Type      | Description                                                                                                            |
|:-------|:----------|:-----------------------------------------------------------------------------------------------------------------------|
| `kind` | `string`  | Value: `primitive`                                                                                                     |
| `name` | `string`  | Values: `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `char`, `string`, `void`, `never`, `any`, `null` |

### `ClassType`
Represents a class type.

| Field           | Type     | Description                                  |
|:----------------|:---------|:---------------------------------------------|
| `kind`          | `string` | Value: `class`                               |
| `qualifiedName` | `string` | Fully qualified name of the referenced class |

### `ArrayType`
Represents an array type.

| Field         | Type           | Description                |
|:--------------|:---------------|:---------------------------|
| `kind`        | `string`       | Value: `array`             |
| `elementType` | [Type](#type)  | Type of the array elements |

### `UnionType`
Represents a type that can be one of several alternative types (e.g., `string | null`).

| Field          | Type             | Description                        |
|:---------------|:-----------------|:-----------------------------------|
| `kind`         | `string`         | Value: `union`                     |
| `alternatives` | [Type](#type)[]  | List of possible type alternatives |

## Example

### Kiwi Code
```kotlin
class Product(
    @Summary
    var name: string,
    var price: double,
    var stock: int,
    var category: Category
) {

    priv fn __category__() -> Category {
        return Category.OTHER
    }

    fn reduceStock(quantity: int) {
        require(stock >= quantity, "Out of stock")
        stock -= quantity
    }

}

bean OrderService {

    fn placeOrder(products: Product[]) -> Order {
        val price = products.map<double>(p -> p.price).sum()
        val order = Order(price)
        products.forEach(p -> {
            p.reduceStock(1)
            order.Item(p, 1)
        })
        return order
    }

}

class Order(val price: double) {

    class Item(
        val product: Product,
        val quantity: int
    )

}

enum Category {
    ELECTRONICS,
    CLOTHING,
    OTHER

;
}
```

### Schema
```http
GET /schema
X-App-ID: {app-id}
```
* Response
```json
{
  "code": 0,
  "data": {
    "classes": [
      {
        "access": "public",
        "tag": "class",
        "abstract": false,
        "name": "Order",
        "qualifiedName": "Order",
        "superTypes": [],
        "constructor": {
          "parameters": [
            {
              "name": "price",
              "type": {
                "name": "double",
                "kind": "primitive"
              },
              "label": "Price"
            }
          ]
        },
        "fields": [
          {
            "access": "public",
            "name": "price",
            "type": {
              "name": "double",
              "kind": "primitive"
            },
            "summary": false,
            "label": "Price"
          }
        ],
        "methods": [],
        "classes": [
          {
            "access": "public",
            "tag": "class",
            "abstract": false,
            "name": "Item",
            "qualifiedName": "Order.Item",
            "superTypes": [],
            "constructor": {
              "parameters": [
                {
                  "name": "product",
                  "type": {
                    "qualifiedName": "Product",
                    "kind": "class"
                  },
                  "label": "Product"
                },
                {
                  "name": "quantity",
                  "type": {
                    "name": "int",
                    "kind": "primitive"
                  },
                  "label": "Quantity"
                }
              ]
            },
            "fields": [
              {
                "access": "public",
                "name": "product",
                "type": {
                  "qualifiedName": "Product",
                  "kind": "class"
                },
                "summary": false,
                "label": "Product"
              },
              {
                "access": "public",
                "name": "quantity",
                "type": {
                  "name": "int",
                  "kind": "primitive"
                },
                "summary": false,
                "label": "Quantity"
              }
            ],
            "methods": [],
            "classes": [],
            "enumConstants": [],
            "label": "Item"
          }
        ],
        "enumConstants": [],
        "label": "Order"
      },
      {
        "access": "public",
        "tag": "class",
        "abstract": false,
        "name": "Product",
        "qualifiedName": "Product",
        "superTypes": [],
        "constructor": {
          "parameters": [
            {
              "name": "name",
              "type": {
                "name": "string",
                "kind": "primitive"
              },
              "label": "Name"
            },
            {
              "name": "price",
              "type": {
                "name": "double",
                "kind": "primitive"
              },
              "label": "Price"
            },
            {
              "name": "stock",
              "type": {
                "name": "int",
                "kind": "primitive"
              },
              "label": "Stock"
            },
            {
              "name": "category",
              "type": {
                "qualifiedName": "Category",
                "kind": "class"
              },
              "label": "Category"
            }
          ]
        },
        "fields": [
          {
            "access": "public",
            "name": "name",
            "type": {
              "name": "string",
              "kind": "primitive"
            },
            "summary": true,
            "label": "Name"
          },
          {
            "access": "public",
            "name": "price",
            "type": {
              "name": "double",
              "kind": "primitive"
            },
            "summary": false,
            "label": "Price"
          },
          {
            "access": "public",
            "name": "stock",
            "type": {
              "name": "int",
              "kind": "primitive"
            },
            "summary": false,
            "label": "Stock"
          },
          {
            "access": "public",
            "name": "category",
            "type": {
              "qualifiedName": "Category",
              "kind": "class"
            },
            "summary": false,
            "label": "Category"
          }
        ],
        "methods": [
          {
            "access": "public",
            "abstract": false,
            "name": "__category__",
            "parameters": [],
            "returnType": {
              "qualifiedName": "Category",
              "kind": "class"
            },
            "label": "__category__"
          },
          {
            "access": "public",
            "abstract": false,
            "name": "reduceStock",
            "parameters": [
              {
                "name": "quantity",
                "type": {
                  "name": "int",
                  "kind": "primitive"
                },
                "label": "Quantity"
              }
            ],
            "returnType": {
              "name": "void",
              "kind": "primitive"
            },
            "label": "Reduce Stock"
          }
        ],
        "classes": [],
        "enumConstants": [],
        "label": "Product"
      },
      {
        "access": "public",
        "tag": "enum",
        "abstract": false,
        "name": "Category",
        "qualifiedName": "Category",
        "superTypes": [
          {
            "qualifiedName": "java.lang.Enum",
            "kind": "class"
          }
        ],
        "constructor": {
          "parameters": [
            {
              "name": "enum$name",
              "type": {
                "name": "string",
                "kind": "primitive"
              },
              "label": "Enum$name"
            },
            {
              "name": "enum$ordinal",
              "type": {
                "name": "int",
                "kind": "primitive"
              },
              "label": "Enum$ordinal"
            }
          ]
        },
        "fields": [],
        "methods": [
          {
            "access": "public",
            "abstract": false,
            "name": "values",
            "parameters": [],
            "returnType": {
              "elementType": {
                "qualifiedName": "Category",
                "kind": "class"
              },
              "kind": "array"
            },
            "label": "Values"
          },
          {
            "access": "public",
            "abstract": false,
            "name": "valueOf",
            "parameters": [
              {
                "name": "name",
                "type": {
                  "alternatives": [
                    {
                      "name": "string",
                      "kind": "primitive"
                    },
                    {
                      "name": "null",
                      "kind": "primitive"
                    }
                  ],
                  "kind": "union"
                },
                "label": "Name"
              }
            ],
            "returnType": {
              "alternatives": [
                {
                  "name": "null",
                  "kind": "primitive"
                },
                {
                  "qualifiedName": "Category",
                  "kind": "class"
                }
              ],
              "kind": "union"
            },
            "label": "Value Of"
          },
          {
            "access": "public",
            "abstract": false,
            "name": "__init_ELECTRONICS__",
            "parameters": [],
            "returnType": {
              "qualifiedName": "Category",
              "kind": "class"
            },
            "label": "__init_ ELECTRONICS__"
          },
          {
            "access": "public",
            "abstract": false,
            "name": "__init_CLOTHING__",
            "parameters": [],
            "returnType": {
              "qualifiedName": "Category",
              "kind": "class"
            },
            "label": "__init_ CLOTHING__"
          },
          {
            "access": "public",
            "abstract": false,
            "name": "__init_OTHER__",
            "parameters": [],
            "returnType": {
              "qualifiedName": "Category",
              "kind": "class"
            },
            "label": "__init_ OTHER__"
          }
        ],
        "classes": [],
        "enumConstants": [
          {
            "name": "ELECTRONICS",
            "label": "ELECTRONICS"
          },
          {
            "name": "CLOTHING",
            "label": "CLOTHING"
          },
          {
            "name": "OTHER",
            "label": "OTHER"
          }
        ],
        "label": "Category"
      },
      {
        "access": "public",
        "tag": "class",
        "abstract": false,
        "name": "OrderService",
        "qualifiedName": "OrderService",
        "superTypes": [],
        "constructor": {
          "parameters": []
        },
        "fields": [],
        "methods": [
          {
            "access": "public",
            "abstract": false,
            "name": "placeOrder",
            "parameters": [
              {
                "name": "products",
                "type": {
                  "elementType": {
                    "qualifiedName": "Product",
                    "kind": "class"
                  },
                  "kind": "array"
                },
                "label": "Products"
              }
            ],
            "returnType": {
              "qualifiedName": "Order",
              "kind": "class"
            },
            "label": "Place Order"
          }
        ],
        "classes": [],
        "enumConstants": [],
        "beanName": "orderService",
        "label": "Order Service"
      }
    ]
  }
}
```

---