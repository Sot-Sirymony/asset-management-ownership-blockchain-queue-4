# Asset Ownership Management – Presentation Outline

Use this outline to build your slides (e.g. PowerPoint, Google Slides, or Marp). Each `---` block can be one slide.

---

## Slide 1: Title

**Asset Ownership Management System**  
*Blockchain-Based Asset Tracking & Transfer*

- Your name / team  
- Date / course

---

## Slide 2: Overview

**What is this project?**

- A full-stack system for **managing asset ownership** (e.g. laptops, equipment) within an organization.
- **Admin** assigns assets to users; **users** view their assets, request new ones, transfer ownership, and see history.
- **Blockchain (Hyperledger Fabric)** stores asset lifecycle so ownership and history are **immutable and auditable**.

---

# Part 1: Features

---

## Slide 3: Features – User Roles

**Two main roles**

| Role   | Capabilities |
|--------|----------------|
| **Admin** | Create/assign assets, update/delete assets, approve asset requests, manage users & departments, view all history, report issues |
| **User**  | View my assets, request new assets, transfer asset to another user, view my history, report issues, edit profile |

- Login → redirect to **Admin Dashboard** or **User Asset** based on role.

---

## Slide 4: Features – Asset Lifecycle

**Core asset operations (on blockchain)**

1. **Assign asset** – Admin creates an asset and assigns it to a user (name, quantity, unit, condition, department).
2. **Update asset** – Admin updates asset details.
3. **Transfer asset** – User (owner) transfers ownership to another user.
4. **Delete asset** – Admin removes asset from the ledger (when applicable by policy).

- Every write is a **transaction on the blockchain** (CreateAsset, UpdateAsset, TransferAsset, DeleteAsset).

---

## Slide 5: Features – Asset Requests & History

**Asset requests (off-chain + blockchain)**

- **User** submits a request for an asset (stored in DB).
- **Admin** approves → system **creates the asset on the blockchain** and links it to the user.
- **History** – Users see their asset history; admins see full history with **transaction IDs** (tx_id) for audit.

**Report issues**

- Users/admins can create/update/delete **report issues** linked to assets (also on chain for audit).

---

## Slide 6: Features – Summary List

**Feature checklist**

- Role-based access (Admin / User)
- Create, read, update, delete assets (blockchain)
- Assign asset to user (admin)
- Transfer asset between users (blockchain)
- Asset request workflow (request → approve → create on chain)
- Full asset history with transaction IDs
- Report issues (blockchain)
- User & department management (DB)
- REST API with JWT auth
- Responsive UI (dashboard, lists, forms, history)

---

# Part 2: Technology

---

## Slide 7: Technology – Stack Overview

**Three layers**

| Layer        | Technology |
|-------------|------------|
| **Frontend** | Next.js 14, React, Refine (Ant Design), NextAuth, Tailwind |
| **Backend**  | Java 17, Spring Boot 3.3, JWT, REST API |
| **Blockchain** | Hyperledger Fabric 2.5, Go chaincode, CouchDB (state DB) |
| **Data**     | PostgreSQL (users, departments, asset requests), CouchDB (Fabric state + history index) |

---

## Slide 8: Technology – Frontend

**Ownership UI**

- **Next.js 14** – App Router, server/client components.
- **Refine** – CRUD framework (resources, auth, data provider).
- **Ant Design** – Tables, forms, modals, layout.
- **NextAuth** – Login (credentials + JWT), session, role-based redirect.
- **Tailwind CSS** – Styling.
- **Axios** – API calls with bearer token.

---

## Slide 9: Technology – Backend API

**Ownership API (Spring Boot)**

- **Spring Boot 3.3**, Java 17 – REST controllers, services, JPA.
- **JWT (jjwt)** – Token-based auth; admin/user roles.
- **PostgreSQL** – Users, departments, asset_request (status, assigned_asset_id).
- **Hyperledger Fabric Java SDK (Gateway)** – Connect to peer/orderer, invoke chaincode (evaluate + submit).
- **Caffeine cache** – Short TTL cache for blockchain reads (e.g. 30s).
- **Swagger/OpenAPI** – API docs at `/swagger-ui`.

---

## Slide 10: Technology – Blockchain

**Hyperledger Fabric**

- **Fabric 2.5** – Permissioned blockchain (orderers + peers).
- **Channel:** `channel-org` – one org (Org1), one chaincode.
- **Chaincode (Go):** Asset CRUD + history (CreateAsset, QueryAsset, QueryAllAssets, UpdateAsset, TransferAsset, DeleteAsset, GetAssetHistory); ReportIssue support.
- **State DB:** CouchDB per peer (rich queries, history index).
- **CA:** Fabric CA for orderer & org identities (enrollment, MSP/TLS certs).
- **API** uses Gateway + wallet (enrolled identities) to submit/evaluate transactions.

---

## Slide 11: Technology – Summary Diagram (Text)

**Data flow (simplified)**

```
[Browser] → Next.js (NextAuth, Refine) → REST API (Spring Boot, JWT)
                                              ↓
                                    Fabric Gateway (wallet identity)
                                              ↓
                    Peer (endorse) ← → Orderer (order) ← → Ledger (block + state)
                                              ↓
                                    CouchDB (state + history)
```

- **Reads:** API → Gateway → Peer → Chaincode (evaluate) → CouchDB/state.
- **Writes:** API → Gateway → Peer (endorse) → Orderer (order) → Block → all peers commit.

---

# Part 3: Architecture

---

## Slide 12: Architecture – High-Level

**Three-tier architecture**

1. **Presentation** – Next.js app (port 3000).  
   - Pages: Home, Login, Admin (dashboard, asset CRUD, asset request, users, departments, history, report issue), User (my assets, asset request, transfer, history, report issue, profile).

2. **Application** – Spring Boot API (port 8081).  
   - Auth (login, JWT), Asset API (blockchain), Asset Request API (DB + blockchain on approve), User/Department API (DB), History/Report API (blockchain + optional CouchDB).

3. **Data & consensus** – PostgreSQL, Hyperledger Fabric network (orderers, peers, CouchDB), Fabric CA.

---

## Slide 13: Architecture – Network Topology

**Fabric network (Docker)**

- **Orderers (Raft):** orderer.ownify.com (7050), orderer2 (8050), orderer3 (9050) – consensus.
- **Peers (Org1):** peer0.org1.ownify.com (7051), peer1 (8051) – endorse, commit, state.
- **CouchDB:** one per peer (5984, 5985, 5986) – state DB + history index.
- **CLI:** channel create/join, chaincode lifecycle (install, approve, commit).
- **CA:** ca_orderer (9054), ca.org1.ownify.com (7054) – identities.
- **Channel:** `channel-org`; **chaincode:** `basic` (asset + report).

---

## Slide 14: Architecture – API ↔ Blockchain

**How the API talks to Fabric**

- **Connection profile** (`connection.yaml`) – peer/orderer URLs, TLS certs, channel name (`channel-org`).
- **Wallet** – file-based; stores enrolled identities (e.g. admin, users) with certs and keys.
- **Gateway** – builds channel connection, gets contract (chaincode `basic`), then:
  - **evaluateTransaction** – read-only (QueryAsset, QueryAllAssets, GetAssetHistory).
  - **submitTransaction** – write (CreateAsset, UpdateAsset, TransferAsset, DeleteAsset).
- **Caching** – short TTL for read results; cache evicted on writes.

---

## Slide 15: Architecture – Security & Flow

**Security**

- **Frontend:** NextAuth session; middleware redirects unauthenticated users from `/admin` and `/user` to `/`.
- **API:** JWT in `Authorization: Bearer <token>`; role checks (ADMIN vs USER) on sensitive endpoints.
- **Blockchain:** TLS for peer/orderer; Fabric identity (MSP) from wallet for each transaction.

**Typical flow (Assign Asset)**

1. Admin logs in → UI → POST `/api/v1/admin/createAsset` (JWT).
2. API loads admin identity from wallet → Gateway → submitTransaction("CreateAsset", ...).
3. Peer endorses → orderer orders → block committed → state updated.
4. API returns new asset; UI shows success; cache evicted so next list is fresh.

---

## Slide 16: Thank You / Q&A

**Summary**

- **Features:** Role-based asset management, assign/transfer/history, asset requests, report issues.
- **Technology:** Next.js + Refine, Spring Boot + Fabric Java SDK, Hyperledger Fabric 2.5 + Go chaincode, PostgreSQL + CouchDB.
- **Architecture:** UI → REST API → Fabric Gateway → Peer/Orderer → Ledger; DB for users and request workflow.

**Questions?**

- Repo: [your repo link]  
- Demo: [optional: live or video]

---

*End of outline. Copy each section into your slide tool; you can add screenshots (UI, Swagger, Docker topology) and a simple diagram for architecture.*
