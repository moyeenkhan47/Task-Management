# Task Management Microservice

A Spring Boot 3 & Java 17 RESTful microservice for managing tasks, built with JPA, H2, and Amazon S3 export integration via AWS SDK v2.

---

## Features

- **CRUD Operations:** Manage tasks with fields `id`, `title`, `description`, `status` (`TODO`, `IN_PROGRESS`, `DONE`), and `dueDate`.
- **S3 CSV Export:** Generates CSV file of all tasks and directly streams/uploads to AWS S3.
- **In-Memory Database:** H2 DB enabled for zero-config testing (configurable for Postgres/MySQL).
- **Multi-Stage Docker Support:** Lightweight Alpine runtime container.

---

## Quick Start (Local Maven)

### Prerequisites
- JDK 17+
- Maven 3.8+

### Build and Run
```bash
# Clean and test
mvn clean test

# Run application locally
mvn spring-boot-run
