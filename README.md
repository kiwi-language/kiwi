Kiwi: The Persistent & Distributed Language
===========================================

Kiwi is an open-source programming language featuring built-in persistence and distribution capabilities, streamlining the development of cloud-native applications.

Download Pre-built Release
--------------------------

You can find the latest official releases on the Kiwi GitHub Releases page:

[**Visit the Kiwi Releases Page**](https://github.com/kiwi-language/kiwi/releases)

Build From Source
-----------------

### Requirements

*   JDK 21
*   Maven

### Steps

1.  Clone the repository:
    
        git clone git@github.com:kiwi-language/kiwi.git
    
2.  Navigate into the cloned directory:
    
        cd kiwi
    
3.  Build the project using Maven:
    
        mvn package

4.  Unpack the generated `dist/target/kiwi.zip` archive to a location of your choice.


Add Kiwi to PATH
----------------

To run Kiwi commands like `kiwi-server`, `kiwi build`, and `kiwi deploy` from any location in your terminal, you should add the Kiwi `bin` directory to your system's PATH environment variable.

The `bin` directory is located within the root directory of your Kiwi installation.

### Linux / macOS

1.  Identify the full path to the Kiwi `bin` directory (e.g., `/path/to/your/kiwi-install/bin`).
2.  Open your shell configuration file (e.g., `~/.bashrc`, `~/.zshrc`, `~/.profile`, or `~/.bash_profile` depending on your shell).
3.  Add the following line, replacing `/path/to/your/kiwi-install/bin` with the actual path:
    
        export PATH="$PATH:/path/to/your/kiwi-install/bin"
    
4.  Save the file and reload the configuration (e.g., by running `source ~/.bashrc` or opening a new terminal window).

### Windows

1.  Identify the full path to the Kiwi `bin` directory (e.g., `C:\path\to\your\kiwi-install\bin`).
2.  Search for "Environment Variables" in the Windows search bar and select "Edit the system environment variables".
3.  Click the "Environment Variables..." button.
4.  In the "System variables" or "User variables" section, find the `Path` variable and click "Edit...".
5.  Click "New" and paste the full path to the Kiwi `bin` directory.
6.  Click "OK" on all open dialog boxes.
7.  You may need to restart any open Command Prompt or PowerShell windows for the changes to take effect.

Configure Datasource
--------------------

1.  Install PostgreSQL.
2.  Create a database within PostgreSQL that Kiwi will use.
3.  Locate the `kiwi.yml` configuration file. It can be found in the `config` directory within your Kiwi installation root (e.g., `/path/to/your/kiwi-install/config/kiwi.yml`).
4.  Edit the `kiwi.yml` file and update the datasource section with your PostgreSQL details:
    
        datasource:
            host: <your_postgres_host>
            port: <your_postgres_port>
            username: <your_postgres_username>
            password: <your_postgres_password>
            database: <your_kiwi_database_name>

Configure Elasticsearch
--------------------

1.  Install Elasticsearch.
2.  Edit the `kiwi.yml` file and update the es section with your Elasticsearch details:

        es:
            host: <your_elasticsearch_host>
            port: <your_elasticsearch_port>
            user: <your_elasticsearch_user>
            password: <your_elasticsearch_password>

Start the Server
----------------

### Requirements

*   JDK 21

Start the server using this command (assuming the `bin` directory is in your PATH):

    kiwi-server start

Initialize the Server
---------------------

Once the server is running, you need to initialize it by sending the following HTTP request.

    curl -X POST http://localhost:8080/system/init

Test the Installation
---------------------

1.  Create a new directory for your test project and navigate into it:
    
        mkdir kiwi_demo
        cd kiwi_demo
    
2.  Create a subdirectory for source files:
    
        mkdir src
    
3.  Create a Kiwi source file named `src/test.kiwi` with the following example code:
    
        class Product(
            var name: string,
            var price: double,
            var stock: int
        ) {
        
            reduceStock(quantity: int) -> boolean {
                if (stock >= quantity) {
                    stock -= quantity
                    return true
                }
                else {
                    return false
                }
            }

        }
    
4.  Build the project using the Kiwi compiler (assuming the `bin` directory is in your PATH):
    
        kiwi build
    

Deploy the Artifact to the Server
---------------------------------

Use the Kiwi CLI to deploy your compiled application to the running Kiwi server (assuming the `bin` directory is in your PATH):

    kiwi deploy

The command will prompt you for deployment details. Here are the expected inputs:

*   `name: demo`
*   `password: 123456`
*   `application: demo`

Interact with the Application
-----------------------------

After deployment, you can interact with your `Product` class via HTTP requests to the Kiwi server. Replace `<id>` in the URLs below with the actual ID returned by the server when you create a product.

### Create a Product

Send a PUT request to create a new product instance.

    curl -X PUT -H "Content-Type: application/json" -d '{"name": "Kiwi Fruit", "price": 10.0, "stock": 100}' http://localhost:8080/product

**Note:** The server should respond with the ID of the newly created product.

### Retrieve the Product

Send a GET request using the product's ID:

    curl http://localhost:8080/<id>

### Decrement the Stock

Send a POST request to the product's `reduce-stock` endpoint (derived from the `reduceStock` method).

    curl -X POST -H "Content-Type: application/json" -d '{"quantity": 1}' http://localhost:8080/<id>/reduce-stock