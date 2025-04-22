# Kiwi: The Persistent & Distributed Language

Kiwi is an open-source programming language featuring built-in persistence and distribution capabilities,
streamlining the development of cloud-native applications.

### How To Build

Requirement

- JDK 21+
- Maven

git clone git@github.com:kiwi-language/kiwi.git

cd kiwi

mvn package

### Configure datasource

Install postgresql. Create a database. And create the Kiwi config file with the datasource config.

Create a kiwi config file /etc/kiwi/kiwi.yml:

datasource:
    username: <username>
    password: <password>
    database: <database>

### Start the server

cd <source_root>/assembly/target

java -jar metavm-assembly-1.0-SNAPSHOT.jar

### Initialize the server
Issuing the following HTTP request to the server

POST http://localhost:8080/system/init

### Install the compiler

mkdir -p ~/develop/kiwi

In the source root, type the following command:

sh kiwi_install.sh

Add $HOME/develop/kiwi/bin to system PATH

### Test the installation
Create a project

mkdir -p kiwi_demo

cd kiwi_demo

Create a kiwi source file src/test.kiwi:


class Product {
    priv var name: string
    priv var price: double
    priv val stock: int

    init(name: string, price: double, stock: int) {
        this.name = name
        this.price = price
        this.stock = stock
    }

    getPrice(): double -> {
        return price
    }

    getName(): string -> {
        return name
    }

    getStock(): int {
        return stock
    }

    decrementStock(quantity: int) -> boolean {
        if (stock >= quantity) {
            stock -= quantity
            return true
        }
        else
            return false
    }

}

Build the project

kiwi build

### Deploy the artifact to the server

kiwi deploy

name: demo

password: 123456

application: demo

### Interact with the application
- Create a product
PUT http://localhost:8080/product

{
    name: "Kiwi Fruit",
    price: 10.0
}

- Retrieve the product
GET http://localhost:8080/<id>

- Decrement the stock
POST http://localhost:8080/<id>/decrement-stock

{
    amount: 1
}

