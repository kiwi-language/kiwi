Manul: The Language of Persistent Objects
===========================================

Welcome to Manul. Follow this guide to install the CLI and deploy your first project.

### 1. Install via CLI
Run the following command to download and install the binary:

```bash
curl --proto '=https' --tlsv1.2 -sSf https://manul-lang.org/install.sh | sh
```

### 2. Configure Shell
Reload your shell configuration to update your path (ensure you use the config file matching your active shell, e.g., `.zshrc` or `.bashrc`):

```bash
source ~/.zshrc
```

### 3. Verify Installation
Confirm that Manul was installed correctly by checking the version:

```bash
manul --version
```

### 4. Quick Start
Create and deploy a basic "Hello World" project to ensure everything is working.

**Initialize Project:**
```bash
mkdir manul-test
cd manul-test
mkdir src

# Create a sample file
cat << EOF > test.manul
class Product(var name: string)
EOF

# Deploy
manul deploy
```

**Test Endpoint:**
Send a request to the local instance to create a new product:

```bash
curl -H "X-App-ID: {app-id}" \
     -X POST http://localhost:8080/api/product \
     --data-raw '{name: "Shoes"}'
```