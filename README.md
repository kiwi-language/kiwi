  Kiwi: The Persistent & Distributed Language body { font-family: sans-serif; line-height: 1.6; margin: 2em; } h1, h2, h3 { color: #333; } h1 { border-bottom: 2px solid #eee; padding-bottom: 0.3em; } h2 { border-bottom: 1px solid #eee; padding-bottom: 0.2em; margin-top: 2em; } code { background-color: #f4f4f4; padding: 0.2em 0.4em; border-radius: 3px; font-family: monospace; } pre { background-color: #f4f4f4; border: 1px solid #ddd; padding: 1em; border-radius: 4px; overflow-x: auto; /\* Handle long lines \*/ } pre code { background-color: transparent; /\* Avoid double background \*/ padding: 0; } ul, ol { padding-left: 2em; } li { margin-bottom: 0.5em; } .note { background-color: #fff L; border-left: 4px solid #ccc; padding: 0.5em 1em; margin: 1em 0; font-style: italic; } .note strong { font-style: normal; }

Kiwi: The Persistent & Distributed Language
===========================================

Kiwi is an open-source programming language featuring built-in persistence and distribution capabilities, streamlining the development of cloud-native applications.

How To Build
------------

### Requirements

*   JDK 21+
*   Maven

### Steps

1.  Clone the repository:
    
        git clone git@github.com:kiwi-language/kiwi.git
    
2.  Navigate into the cloned directory:
    
        cd kiwi
    
3.  Build the project using Maven:
    
        mvn package
    

Configure Datasource
--------------------

1.  Install PostgreSQL on your system.
2.  Create a database within PostgreSQL that Kiwi will use.
3.  Create the Kiwi configuration file at `/etc/kiwi/kiwi.yml`.
4.  Add your datasource configuration to the file:
    
        datasource:
            username: <your_postgres_username>
            password: <your_postgres_password>
            database: <your_kiwi_database_name>
    
    **Note:** Replace the placeholder values (`<...>`) with your actual PostgreSQL username, password, and the name of the database you created.
    

Start the Server
----------------

1.  Navigate to the assembly target directory within your cloned source code folder:
    
        cd <source_root>/assembly/target
    
    **Note:** Replace `<source_root>` with the actual path where you cloned the Kiwi repository.
    
2.  Execute the server JAR file:
    
        java -jar metavm-assembly-1.0-SNAPSHOT.jar
    

Initialize the Server
---------------------

Once the server is running, you need to initialize it by sending an HTTP request. Use a tool like `curl` or an API client (like Postman) to send the following:

    POST http://localhost:8080/system/init

No request body is needed for this initialization step.

Install the Compiler
--------------------

1.  Create a directory for the Kiwi development tools (if it doesn't exist):
    
        mkdir -p ~/develop/kiwi
    
2.  Navigate back to the root directory of your cloned Kiwi source code.
3.  Run the installation script:
    
        sh kiwi_install.sh
    
4.  Add the Kiwi compiler's bin directory to your system's PATH environment variable. Edit your shell's configuration file (e.g., `~/.bashrc`, `~/.zshrc`, `~/.profile`) and add this line:
    
        export PATH="$HOME/develop/kiwi/bin:$PATH"
    
5.  Apply the changes by either restarting your terminal session or sourcing the configuration file (e.g., `source ~/.bashrc`).

Test the Installation
---------------------

1.  Create a new directory for your test project and navigate into it:
    
        mkdir -p kiwi_demo
        cd kiwi_demo
    
2.  Create a subdirectory for source files:
    
        mkdir src
    
3.  Create a Kiwi source file named `src/test.kiwi` with the following example code:
    
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
                if (stock >= quantity) { // Note: >= might need escaping depending on context
                    stock -= quantity
                    return true
                }
                else {
                    return false
                }
            }
        }
    
4.  Build the project using the Kiwi compiler:
    
        kiwi build
    
    This command should compile your `test.kiwi` file.
    

Deploy the Artifact to the Server
---------------------------------

Use the Kiwi CLI to deploy your compiled application to the running Kiwi server:

    kiwi deploy

The command will prompt you for deployment details. Here are example inputs:

*   `name: demo`
*   `password: 123456` (Use a secure password in practice)
*   `application: demo`

Interact with the Application
-----------------------------

After deployment, you can interact with your `Product` class via HTTP requests to the Kiwi server. Replace `<id>` in the URLs below with the actual ID returned by the server when you create a product.

### Create a Product

Send a PUT request to create a new product instance. Ensure the request body is valid JSON.

    PUT http://localhost:8080/product
    
    {
        "name": "Kiwi Fruit",
        "price": 10.0,
        "stock": 100
    }

**Note:** The server should respond with the ID of the newly created product.

### Retrieve the Product

Send a GET request using the product's ID:

    GET http://localhost:8080/<id>

### Decrement the Stock

Send a POST request to the product's `decrement-stock` endpoint (derived from the `decrementStock` method). Ensure the request body is valid JSON.

    POST http://localhost:8080/<id>/decrement-stock
    
    {
        "quantity": 1
    }
