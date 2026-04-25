# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run (requires PostgreSQL running locally)
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.remar.EN.gestion.ApplicationTests"
```

Tests require a live PostgreSQL connection (see prerequisites below).

## Prerequisites

PostgreSQL must be running locally with:
- Database: `engestion_db`
- User: `postgres`
- Port: `5432`

The app runs on `http://localhost:8080`. A Postman collection is at `src/main/resources/EN.gestion.postman_collection.json`.

## Architecture

Standard Spring Boot 3.5 / Java 17 layered architecture:

```
Controller → Service → Repository → Entity
              ↑ DTOs (Request/Response)
```

- **Entity** (`Transferencia`) is the JPA-managed object mapped to the `transferencias` table. It sets `creadoEn` via `@PrePersist`.
- **Repository** (`TransferenciaRepository`) extends `JpaRepository` and adds `findByEstado`, `findByClienteContainingIgnoreCase`, and `existsByReferencia`.
- **Service** (`TransferenciaService`) owns business logic: duplicate `referencia` check before save, all reads are `readOnly = true`.
- **DTOs**: `TransferenciaRequestDTO` carries validation annotations; `TransferenciaResponseDTO` is constructed directly from the entity (no mapper library).

### Transfer state machine

`EstadoTransferencia` enum: `PENDIENTE` → `CONFIRMADA` | `RECHAZADA`. New transfers default to `PENDIENTE`.

## Current development phase

**Phase 1 (complete):** CRUD básico de transferencias via `POST/GET /api/transferencias`.

**Phase 2 (pending):** JWT authentication and role-based access control. `SecurityConfig` intentionally disables all security for now — do not add auth logic until Phase 2 is started.
