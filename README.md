# 🕰️ LifeLogix: An OS for an Intentional Life

[![Project Status: MVP Phase](https://img.shields.io/badge/status-MVP_In_Progress-blue.svg)](https://github.com/UTACt/lifelogix/issues)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](./LICENSE)
[![Backend: Spring Boot](https://img.shields.io/badge/Backend-Spring_Boot-6DB33F?logo=spring)](https://spring.io/)
[![Frontend: React](https://img.shields.io/badge/Frontend-React-61DAFB?logo=react)](https://react.dev/)
[![Architecture: DDD Inspired](https://img.shields.io/badge/Architecture-DDD_Inspired-9B59B6)](./docs/ARCHITECTURE.md)

> **Redesigning "busy" into "accomplishment."** LifeLogix is a web platform that transforms your daily records into a data-driven master plan to achieve your most ambitious goals.

---

## Introduction

LifeLogix is more than just a time tracker; it's a strategic tool designed for individuals and communities who are serious about personal and collective growth. By meticulously logging your life's activities, our system helps you discover the underlying logic (`Logix`) of your own success. Our ultimate vision is to foster a supportive environment where users can share their journeys, motivate each other, and achieve sustainable growth together.

---

## Key Features

| Feature | Description | User Value |
|:--- |:--- |:--- |
| **Timeline Logging** | Clearly record your day in 30-minute time blocks. | **Turn ambiguous time into hard data.** |
| **AI Goal Planner** | An AI proposes optimal action plans based on your past records. | **Your personal AI strategist.** |
| **Social Growth** | Share reflections with peers and inspire each other to grow. | **Motivation through community.** |
| **Life Dashboard** | A hexagonal chart provides an at-a-glance view of your life balance. | **Discover balance, prevent burnout.** |

---

## Architecture

This project is built on a modern, robust technical foundation designed for scalability and maintainability.

- **Backend:** A **Spring Boot** application guided by **Domain-Driven Design (DDD)**. It adopts the core principle of **Hexagonal Architecture** by separating the internal business logic (domain) from external concerns (API, persistence), which ensures a modular and maintainable structure.
- **Frontend:** A responsive and interactive user interface built with **React** and **Next.js**.
- **Database:** Utilizes **JPA (Hibernate)** for object-relational mapping, with **QueryDSL** for type-safe complex queries.

For a deeper dive into our technical principles and structure, please see our [**Architecture Guide**](./docs/ARCHITECTURE.md).

---

## Getting Started (Development Setup)

Follow these instructions to set up the development environment on your local machine.

### Prerequisites

- **Java 17** or later
- **Gradle 8.5** or later
- **Node.js 20.x** or later
- **pnpm** (`npm install -g pnpm`)

### 1. Clone the Repository

```bash
git clone https://github.com/UTACt/lifelogix.git
cd lifelogix
```

### 2. Configure Backend

The backend requires a local configuration file for database connections and other settings.

1.  Navigate to the backend resources directory:
    ```bash
    cd backend/src/main/resources
    ```
2.  Create a new configuration file named `application-local.yml` by copying the main application file:
    ```bash
    cp application.yml application-local.yml
    ```
3.  Open `application-local.yml` and modify the `datasource` properties (URL, username, password) to match your local database setup.

### 3. Build & Run

You will need two separate terminal sessions to run the backend and frontend servers.

**Terminal 1: Backend (Spring Boot)**

```bash
# Navigate to the backend directory
cd backend

# Build the project and run tests
gradle build

# Run the application
gradle bootRun
```
The backend server will start on `http://localhost:8080`.

**Terminal 2: Frontend (Next.js)**

```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies
pnpm install

# Run the development server
pnpm dev
```
The frontend development server will start on `http://localhost:3000`.

---

## Documentation

Project design, architectural decisions, and other important information are documented in the `/docs` directory.

- **[Architecture Guide](./docs/ARCHITECTURE.md)**: The core architectural principles and technical stack.
- **[API Contracts](./docs/api/)**: API specifications for each version. The latest is [v1.3.0](./docs/api/api-contract-mvp-v1.3.md).
- **[Knowledge Base (KB)](./docs/kb/)**: Records of troubleshooting and technical problem-solving.

### Architectural Decision Records (ADRs)

The following is a list of currently active architectural decisions. Superseded or deprecated decisions are moved to the `docs/adr/archive` directory.

- [ADR-001](./docs/adr/001-repository-pattern.md): Adopting the Repository Pattern for the Data Access Layer
- [ADR-003](./docs/adr/003-api-versioning.md): Adopting URL Path-Based API Versioning
- [ADR-005](./docs/adr/005-jwt-handling-unification.md): Standardizing JWT Handling and Library Roles
- [ADR-006](./docs/adr/006-hierarchical-category-model.md): Adopting a Hierarchical Hybrid Category Model
- [ADR-007](./docs/adr/007-domain-centric-package-structure.md): Adopting a Domain-Centric Package Structure
- [ADR-008](./docs/adr/008-api-layer-package-structure.md): Refining the API Layer Package Structure
- [ADR-009](./docs/adr/009-dto-design-with-records.md): Standardizing DTO Design with Java Records
- [ADR-010](./docs/adr/010-unit-test-data-creation.md): Standardizing Unit Test Data Creation
- [ADR-011](./docs/adr/011-token-based-authentication.md): Adopting Token-Based Authentication (JWT)
- [ADR-012](./docs/adr/012-centralized-exception-handling-with-errorcode.md): Adopting Centralized Exception Handling with ErrorCode Enum
- [ADR-013](./docs/adr/013-test-strategy-for-authenticated-api.md): Establishing a Test Strategy for Authenticated APIs

---

## Project Roadmap

- **v1.0.0: Core Engine Release 🎯**
    - **Goal**: Stabilize core features (Auth, Timeline, Category/Activity Management).
    - **Tech**: Establish architecture ([ADR-007](./docs/adr/007-domain-centric-package-structure.md)), data modeling, and test strategies.

- **v1.1.0: Enhanced Auth & Stability 🔒**
    - **Goal**: Improve security and scalability.
    - **Features**: OAuth social login, API Rate Limiting.

- **v1.2.0: Data Insights & Visualization 📊**
    - **Goal**: Increase service value through data analytics.
    - **Features**: Life Dashboard, Calendar View, Weekly/Monthly Reports.

- **v1.3.0: Social Interaction & Reflection 🌱**
    - **Goal**: Introduce community and self-reflection tools.
    - **Features**: Social features (Follow, Profiles), Gamification, Daily Reflections.

- **v1.4.0: AI-Powered Planning ✨**
    - **Goal**: Provide personalized planning and in-depth insights using AI.
    - **Features**: AI Goal Planner, Advanced Data Analytics.

---

## Contributing

Please read our [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.