Manul: The Language of Persistent Objects
===========================================

Welcome to Manul. Follow this guide to install the CLI and deploy your first project.

### 1. Install via CLI
Run the following command to download and install the binary:

```bash
curl -sSf https://manul-lang.org/install.sh | sh
```

### 2. Update Your Path
Source the environment file to make the `manul` command immediately available in your current shell:

```bash
source ~/.manul/bin/env
```

### 3. Verify Installation
Confirm that Manul was installed correctly by checking the version:

```bash
manul --version
```

### 4. Quick Start
Create and deploy a simple project to ensure everything is working.

**Initialize Project:**
```bash
mkdir -p manul-test/src
cd manul-test

# Create an application and select it
manul create-app quickstart
manul set-app quickstart

# Create a sample file
cat << EOF > product.mnl
class Product(var name: string)
EOF

# Deploy
manul deploy
```

**Test Endpoint:**
Send a request to the local instance to create a new product:

```bash
curl -X POST http://localhost:8080/api/quickstart/product --data-raw '{name: "Shoes"}'
```

Retrieve the created product (replace `<app-id>` with the output from the previous request):

```bash
curl http://localhost:8080/api/quickstart/product/<app-id>
```
