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

| Field   | Type   | Description                         |
|:--------|:-------|:------------------------------------|
| `code`    | `int`    | `0` for success, non-zero for failure |
| `message` | `string` | Error message if `code` is non-zero   |
| `data`    | `T`      | The actual response data            |

### `Response`
The main data payload for the schema endpoint.

| Field   | Type              | Description                                        |
|:--------|:------------------|:---------------------------------------------------|
| `classes` | [Class](#class)[] | List of top-level class, interface, enum definitions. |

### `Class`
Represents a class, interface, enum, value class, or bean definition.

| Field         | Type                            | Description                                                  |
|:--------------|:--------------------------------|:-------------------------------------------------------------|
| `access`        | `string`                          | Access modifier: `public`, `private`, `protected`, `package`   |
| `tag`           | `string`                          | Type of definition: `class`, `interface`, `enum`, `value`, `bean` |
| `isAbstract`    | `bool`                            | True if the class is abstract                                |
| `name`          | `string`                          | Simple name of the class (e.g., `Baz`)                         |
| `qualifiedName` | `string`                          | Fully qualified name (e.g., `foo.bar.Baz`)                   |
| `superTypes`    | [ClassType](#classtype)[]       | List of direct supertypes                                    |
| `constructor`   | [Constructor](#constructor)     | Class constructor definition                                 |
| `fields`        | [Field](#field)[]               | List of fields in the class                                  |
| `methods`       | [Method](#method)[]             | List of methods in the class                                 |
| `classes`       | [Class](#class)[]               | Nested class definitions                                     |
| `enumConstants` | [EnumConstant](#enumconstant)[] | For `enum` types, lists the enum constants                     |

### `Constructor`
Represents a class constructor.

| Field      | Type                      | Description                 |
|:-----------|:--------------------------|:----------------------------|
| `parameters` | [Parameter](#parameter)[] | List of constructor parameters |

### `Field`
Represents a class field.

| Field  | Type          | Description                |
|:-------|:--------------|:---------------------------|
| `access` | `string`        | Field access modifier (see `Class.access` for values) |
| `name`   | `string`        | Field name                 |
| `type`   | [Type](#type) | Field type                 |

### `Method`
Represents a class method.

| Field      | Type                      | Description                |
|:-----------|:--------------------------|:---------------------------|
| `access`     | `string`                    | Method access modifier (see `Class.access` for values) |
| `isAbstract` | `bool`                      | True if the method is abstract |
| `name`       | `string`                    | Method name                |
| `parameters` | [Parameter](#parameter)[] | List of method parameters  |
| `returnType` | [Type](#type)             | Method return type         |

### `EnumConstant`
Represents a constant within an enum.

| Field | Type   | Description          |
|:------|:-------|:---------------------|
| `name`  | `string` | Name of the enum constant |

### `Parameter`
Represents a parameter for a method or constructor.

| Field | Type           | Description    |
|:------|:---------------|:---------------|
| `name`  | `string`         | Parameter name |
| `type`  | [Type](#type)  | Parameter type |

### `Type`
A polymorphic type descriptor.

| Field | Type   | Description                                                              |
|:------|:-------|:-------------------------------------------------------------------------|
| `kind`  | `string` | Discriminator field for the specific subtype (e.g., `primitive`, `class`). |

Subtypes: [PrimitiveType](#primitivetype), [ClassType](#classtype), [ArrayType](#arraytype), [UnionType](#uniontype)

### `PrimitiveType`
Represents a primitive type.

| Field | Type     | Description                                                                                    |
|:------|:---------|:-----------------------------------------------------------------------------------------------|
| `kind`  | `string` | Value: `primitive`                                                                             |
| `name`  | `string`   | Values: `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `char`, `string`, `void`, `never`, `any`, `null` |

### `ClassType`
Represents a class type.

| Field         | Type   | Description      |
|:--------------|:-------|:-----------------|
| `kind`          | `string` | Value: `class`   |
| `qualifiedName` | `string` | Fully qualified name of the referenced class |

### `ArrayType`
Represents an array type.

| Field       | Type          | Description             |
|:------------|:--------------|:------------------------|
| `kind`        | `string`        | Value: `array`          |
| `elementType` | [Type](#type) | Type of the array elements |

### `UnionType`
Represents a type that can be one of several alternative types (e.g., `string | null`).

| Field        | Type            | Description                  |
|:-------------|:----------------|:-----------------------------|
| `kind`         | `string`          | Value: `union`               |
| `alternatives` | [Type](#type)[] | List of possible type alternatives |

## Example

### Kiwi Code
```kotlin
class Product(
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
        "isAbstract": false,
        "name": "Product",
        "qualifiedName": "Product",
        "superTypes": [],
        "constructor": {
          "parameters": [
            {"name": "name", "type": {"name": "string", "kind": "primitive"}},
            {"name": "price", "type": {"name": "double", "kind": "primitive"}},
            {"name": "stock", "type": {"name": "int", "kind": "primitive"}},
            {"name": "category", "type": {"qualifiedName": "Category", "kind": "class"}}
          ]
        },
        "fields": [
          {"access": "public", "name": "name", "type": {"name": "string", "kind": "primitive"}},
          {"access": "public", "name": "price", "type": {"name": "double", "kind": "primitive"}},
          {"access": "public", "name": "stock", "type": {"name": "int", "kind": "primitive"}},
          {"access": "public", "name": "category", "type": {"qualifiedName": "Category", "kind": "class"}}
        ],
        "methods": [
          {"access": "private", "isAbstract": false, "name": "__category__", "parameters": [], "returnType": {"qualifiedName": "Category", "kind": "class"}},
          {"access": "public", "isAbstract": false, "name": "reduceStock", "parameters": [{"name": "quantity", "type": {"name": "int", "kind": "primitive"}}], "returnType": {"name": "void", "kind": "primitive"}}
        ],
        "classes": [],
        "enumConstants": []
      },
      {
        "access": "public",
        "tag": "enum",
        "isAbstract": false,
        "name": "Category",
        "qualifiedName": "Category",
        "superTypes": [{"qualifiedName": "java.lang.Enum", "kind": "class"}],
        "constructor": {
          "parameters": [
            {"name": "enum$name", "type": {"name": "string", "kind": "primitive"}},
            {"name": "enum$ordinal", "type": {"name": "int", "kind": "primitive"}}
          ]
        },
        "fields": [],
        "methods": [
          {"access": "public", "isAbstract": false, "name": "values", "parameters": [], "returnType": {"elementType": {"qualifiedName": "Category", "kind": "class"}, "kind": "array"}},
          {"access": "public", "isAbstract": false, "name": "valueOf", "parameters": [{"name": "name", "type": {"alternatives": [{"name": "string", "kind": "primitive"}, {"name": "null", "kind": "primitive"}], "kind": "union"}}], "returnType": {"alternatives": [{"qualifiedName": "Category", "kind": "class"}, {"name": "null", "kind": "primitive"}], "kind": "union"}},
          {"access": "public", "isAbstract": false, "name": "__init_ELECTRONICS__", "parameters": [], "returnType": {"qualifiedName": "Category", "kind": "class"}},
          {"access": "public", "isAbstract": false, "name": "__init_CLOTHING__", "parameters": [], "returnType": {"qualifiedName": "Category", "kind": "class"}},
          {"access": "public", "isAbstract": false, "name": "__init_OTHER__", "parameters": [], "returnType": {"qualifiedName": "Category", "kind": "class"}}
        ],
        "classes": [],
        "enumConstants": [{"name": "ELECTRONICS"}, {"name": "CLOTHING"}, {"name": "OTHER"}]
      },
      {
        "access": "public",
        "tag": "class",
        "isAbstract": false,
        "name": "Order",
        "qualifiedName": "Order",
        "superTypes": [],
        "constructor": {
          "parameters": [{"name": "price", "type": {"name": "double", "kind": "primitive"}}]
        },
        "fields": [{"access": "public", "name": "price", "type": {"name": "double", "kind": "primitive"}}],
        "methods": [],
        "classes": [
          {
            "access": "public",
            "tag": "class",
            "isAbstract": false,
            "name": "Item",
            "qualifiedName": "Order.Item",
            "superTypes": [],
            "constructor": {
              "parameters": [
                {"name": "product", "type": {"qualifiedName": "Product", "kind": "class"}},
                {"name": "quantity", "type": {"name": "int", "kind": "primitive"}}
              ]
            },
            "fields": [
              {"access": "public", "name": "product", "type": {"qualifiedName": "Product", "kind": "class"}},
              {"access": "public", "name": "quantity", "type": {"name": "int", "kind": "primitive"}}
            ],
            "methods": [],
            "classes": [],
            "enumConstants": []
          }
        ],
        "enumConstants": []
      },
      {
        "access": "public",
        "tag": "bean",
        "isAbstract": false,
        "name": "OrderService",
        "qualifiedName": "OrderService",
        "superTypes": [],
        "constructor": {"parameters": []},
        "fields": [],
        "methods": [
          {"access": "public", "isAbstract": false, "name": "placeOrder", "parameters": [{"name": "products", "type": {"elementType": {"qualifiedName": "Product", "kind": "class"}, "kind": "array"}}], "returnType": {"qualifiedName": "Order", "kind": "class"}}
        ],
        "classes": [],
        "enumConstants": []
      }
    ]
  }
}
```

---