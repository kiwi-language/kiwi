# Kiwi: The Persistent & Distributed Language

Kiwi is an open-source programming language designed with built-in persistence and distribution capabilities, aiming to simplify the development of robust, scalable, cloud-native applications.

## Table of Contents

* [Prerequisites](#prerequisites)
* [Installation & Setup](#installation--setup)
    * [1. Build Kiwi Core](#1-build-kiwi-core)
    * [2. Configure Datasource (PostgreSQL)](#2-configure-datasource-postgresql)
    * [3. Start the Kiwi Server](#3-start-the-kiwi-server)
    * [4. Initialize the Server](#4-initialize-the-server)
    * [5. Install the Kiwi Compiler Tools](#5-install-the-kiwi-compiler-tools)
* [Your First Kiwi Application](#your-first-kiwi-application)
    * [1. Create Project Structure](#1-create-project-structure)
    * [2. Write Kiwi Code](#2-write-kiwi-code)
    * [3. Build the Project](#3-build-the-project)
    * [4. Deploy to Server](#4-deploy-to-server)
    * [5. Interact via HTTP](#5-interact-via-http)

## Prerequisites

Before you begin, ensure you have the following installed:

* JDK 21 or later
* Apache Maven
* Git
* PostgreSQL Server

## Installation & Setup

Follow these steps to get the Kiwi server and compiler tools running.

### 1. Build Kiwi Core

Clone the repository and use Maven to build the core components:

```bash
git clone git@github.com:kiwi-language/kiwi.git
cd kiwi
mvn package
