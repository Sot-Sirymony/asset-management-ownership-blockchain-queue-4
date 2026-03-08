# Technologies Used – Asset Ownership Management Project

Complete list of technologies used in this project, by layer.

---

## Frontend (Ownership UI)

| Category | Technology | Version / Notes |
|----------|------------|------------------|
| **Framework** | Next.js | 14.1.x (App Router) |
| **UI library** | React | 18.x |
| **CRUD / data** | Refine | 4.54+ (Refine Core, Ant Design, Next.js Router, Simple REST) |
| **UI components** | Ant Design (antd) | 5.x |
| **Auth** | NextAuth | 4.24.x (credentials + JWT) |
| **HTTP client** | Axios | 1.7.x |
| **Styling** | Tailwind CSS | 3.4.x |
| **State** | Zustand | 5.x |
| **Forms** | React Hook Form | 7.x |
| **Animations** | AOS (Animate On Scroll) | 2.3.x |
| **Utilities** | js-cookie, classnames, jsonwebtoken, toastify-js, flowbite | — |
| **Build / dev** | TypeScript | 5.4.x |
| **E2E tests** | Playwright | 1.58.x |
| **Other** | Node.js | ≥18 (engines) |

---

## Backend (Ownership API)

| Category | Technology | Version / Notes |
|----------|------------|------------------|
| **Runtime** | Java | 17 |
| **Framework** | Spring Boot | 3.3.0 |
| **Web** | Spring Boot Starter Web | — |
| **Security** | Spring Boot Starter Security, Spring Security Test | — |
| **Validation** | Spring Boot Starter Validation | — |
| **Auth (tokens)** | JJWT (jjwt-api, jjwt-impl, jjwt-jackson) | 0.11.5 |
| **Database** | PostgreSQL | (driver: postgresql) |
| **ORM / SQL** | MyBatis Spring Boot Starter | 3.0.3 |
| **Blockchain SDK** | Hyperledger Fabric SDK Java | 2.2.26 |
| **Blockchain Gateway** | Fabric Gateway Java | 2.2.0, fabric-gateway 1.5.1 |
| **gRPC** | grpc-netty-shaded | 1.62.2 (runtime, for Fabric) |
| **API docs** | Springdoc OpenAPI (Swagger UI) | 2.0.2 |
| **Mapping** | ModelMapper | 3.1.1 |
| **Caching** | Spring Boot Starter Cache, Caffeine | — |
| **Rate limiting** | Bucket4j (bucket4j-core, bucket4j-jcache) | 8.7.0 |
| **JAXB** | jaxb-api | 2.3.0 (compatibility) |
| **Utilities** | Lombok | — |
| **Build** | Maven | (pom.xml) |
| **Tests** | Spring Boot Test, MyBatis Test, Security Test | — |
| **Observability (optional)** | OpenTelemetry Collector | 0.96.0 (Docker) |

---

## Blockchain (Hyperledger Fabric)

| Category | Technology | Version / Notes |
|----------|------------|------------------|
| **Platform** | Hyperledger Fabric | 2.5 |
| **Images** | fabric-orderer, fabric-peer, fabric-tools, fabric-ca | 2.5 / 1.5 |
| **Consensus** | Raft (etcdraft) | 3 orderers |
| **Chaincode language** | Go (Golang) | 1.25.x (go.mod) |
| **Chaincode API** | fabric-contract-api-go | 1.2.2 |
| **State database** | CouchDB | 3.x (one per peer) |
| **CA** | Fabric CA | 1.5 (org + orderer CAs) |
| **Containers / orchestration** | Docker, Docker Compose | — |

---

## Data & Infrastructure

| Category | Technology | Notes |
|----------|------------|--------|
| **Relational DB** | PostgreSQL | Users, departments, asset_request; port 55432 (local) |
| **State DB (Fabric)** | CouchDB | 3.x; state + history index per peer |
| **Containers** | Docker | Network, API (optional), DB (optional) |
| **Orchestration** | Docker Compose | Fabric network, CAs, Postgres, optional Otel |

---

## Development & Operations

| Category | Technology | Notes |
|----------|------------|--------|
| **Scripts** | Bash | net.sh, start-all-projects.sh, fix-blockchain.sh, etc. |
| **API tests** | JUnit 5, MockMvc, Spring Test | AssetControllerTest, etc. |
| **UI E2E** | Playwright | frontend-cases.spec.ts |
| **Version control** | Git | — |

---

## Summary (one-line checklist for slides)

**Frontend:** Next.js, React, Refine, Ant Design, NextAuth, Tailwind CSS, Axios, TypeScript, Playwright  

**Backend:** Java 17, Spring Boot 3.3, JWT (JJWT), PostgreSQL, MyBatis, Fabric SDK & Gateway (Java), Springdoc (Swagger), Caffeine, Bucket4j  

**Blockchain:** Hyperledger Fabric 2.5, Go chaincode (fabric-contract-api-go), Fabric CA, CouchDB, Docker  

**Infrastructure:** Docker & Docker Compose, PostgreSQL, CouchDB  

**Dev/Ops:** Bash, JUnit, MockMvc, Playwright, Git  
