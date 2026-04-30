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

The app runs on `http://localhost:8080`. A Bruno collection is at `bruno/` (open the folder in Bruno, use the `local` environment).

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

### Comprobantes module

`POST /api/comprobantes/leer` — sends image/PDF to Gemini, persists a `ComprobanteLeido` with estado `PENDIENTE`, and returns it with a client suggestion from memory.

`GET /api/comprobantes?estado=PENDIENTE` — lists comprobantes, optionally filtered by estado.

`POST /api/comprobantes/{id}/confirmar` — body: `{ clienteId, monto?, fecha?, actualizarCuenta? }`. Creates a `Transferencia` and links it to the comprobante. If `cuentaOrigen` is already mapped to a different client in `CuentaCliente`, returns HTTP 409 unless `actualizarCuenta: true/false` is explicitly sent.

`DELETE /api/comprobantes/{id}` — marks as `DESCARTADO`.

**Account memory**: `CuentaCliente` maps `cuentaOrigen` (CBU/CVU) → `Cliente`. Populated automatically on confirm. One client can have many accounts; each account maps to one client.

## Current development phase

**Phase 1 (complete):** CRUD básico de transferencias via `POST/GET /api/transferencias`.

**Phase 1.5 (complete):** Lectura de comprobantes via Gemini con persistencia y memoria de cuentas.

**Phase 2 (pending):** JWT authentication and role-based access control. `SecurityConfig` intentionally disables all security for now — do not add auth logic until Phase 2 is started.
