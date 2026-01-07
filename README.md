# Ecommerce API

A Spring Boot application for a simple ecoomerce api

## Prerequisites

Before running the application, ensure you have the following installed on your system:

- **Java 21** (or higher)
- The project includes the **Maven Wrapper** (`./mvnw` / `mvnw.cmd`), so you don’t need Maven installed separately.

Check your Java version with:

```bash
java -version
```

## Getting started

### 1) Clone the repository

```bash
git clone https://github.com/Kingsleyaham/spring-boot-ecommerce-api.git
cd <PROJECT_DIRECTORY>
```

> Replace `<PROJECT_DIRECTORY>` with the folder name created after cloning.

### 2) Configure environment variables

This project uses environment variables for configuration.

1. Locate the **`.env.example`** file in the project root.
2. Add **each variable listed in `.env.example`** to your environment and set the **correct values** for your setup.

#### Option A: Export variables in your terminal (macOS/Linux)

```bash
export SOME_VAR="value"
export ANOTHER_VAR="value"
```

#### Option B: Set variables in PowerShell (Windows)

```powershell
$env:SOME_VAR="value"
$env:ANOTHER_VAR="value"
```

### 3) Ensure PostgreSQL is available

This application uses **PostgreSQL** as its database. Make sure you have a running PostgreSQL instance and that the database credentials/URL you set in your environment variables are correct.

## Running the application

Start the Spring Boot app using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Running tests

Run the full test suite:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```
