# Asset Creation and Registration on Blockchain – Step-by-Step Flow

End-to-end flow from the admin filling the form to the asset being stored on the Hyperledger Fabric ledger.

---

## Overview Diagram (high level)

```
[Admin Browser] → [Next.js UI] → [REST API] → [Fabric Gateway] → [Peer] → [Orderer] → [Ledger]
                                      ↓
                                [Chaincode: CreateAsset]
                                      ↓
                                [CouchDB State]
```

---

## Step-by-step flow

### **Step 1: Admin opens the Create Asset page**

- Admin is logged in (NextAuth session, JWT in cookie/state).
- Admin navigates to **`/admin/asset/create`**.
- Next.js loads the page; **AssetCreateClient** component mounts.
- **AssetCreateClient** fetches the list of users (e.g. `getAllUser(token)`) to populate the “Assign To” dropdown.

**Result:** Create Asset form is visible with fields: Asset Name, Qty, Unit, Condition, Attachment (optional), Assign To (user list).

---

### **Step 2: Admin fills the form and clicks Save**

- Admin enters:
  - **Asset Name** (e.g. "Laptop Dell XPS")
  - **Qty** (e.g. 1)
  - **Unit** (e.g. "Unit" or empty)
  - **Condition** (e.g. "New" / "Good")
  - **Attachment** (optional file upload)
  - **Assign To** (selects a user from the list)
- Admin clicks **Save**.

**Result:** `onFinish(values)` runs in AssetCreateClient.

---

### **Step 3: UI prepares the payload and calls the API**

- If an attachment was chosen, the UI may call **uploadImages** and get a file URL.
- UI builds the asset object:
  - `assetName`, `qty`, `unit`, `condition`, `attachment`, `assignTo` (user ID number).
- UI calls **addAsset(token, newAsset)** → which calls **createAsset(token, data)** in `asset.service.js`.
- **createAsset** sends:
  - **Method:** `POST`
  - **URL:** `{NEXT_PUBLIC_API_URL}/api/v1/admin/createAsset`
  - **Headers:** `Authorization: Bearer <JWT>`, `Content-Type: application/json`
  - **Body:** JSON with `assetName`, `qty`, `unit`, `condition`, `attachment`, `assignTo`

**Result:** HTTP request is sent from the browser to the backend API.

---

### **Step 4: API receives the request and authorizes**

- Request hits **AssetController** → **createAsset(@RequestBody Asset asset)**.
- **Spring Security** validates the JWT and loads the current user (admin).
- **@Valid** runs validation on the Asset model (e.g. required fields, assignTo).
- Controller calls **assetService.createAsset(asset)**.

**Result:** Service layer runs with the authenticated admin user and the asset payload.

---

### **Step 5: Service gets current user and acquires write lock**

- **AssetServiceImp.createAsset(asset)**:
  - Gets current user: **currentUserResponse()** (admin; from JWT / GetCurrentUser).
  - Calls **gatewayCache.runWithWriteLock(admin.getUsername(), ...)** so only one write at a time uses that Fabric identity.
  - Inside the lock: **getOrCreate(admin.getUsername())** returns (or creates) a **Gateway** for the admin’s wallet identity.

**Result:** API holds a Fabric Gateway connected as the admin identity (from the wallet).

---

### **Step 6: API generates asset ID and gets the contract**

- **AssetIdGenerator.generateAssetId()** produces a unique ID, e.g. `asset-<uuid-without-hyphens>`.
- Asset object is updated: **asset.setAssetId(assetId)**.
- Service gets the Fabric **Contract** for the chaincode **"basic"** on channel **channel-org**:  
  **fabricContract(gateway)** → **gateway.getNetwork("channel-org").getContract("basic")**.

**Result:** API has a Contract reference to invoke the “basic” chaincode.

---

### **Step 7: API invokes CreateAsset on the chaincode**

- Service builds parameters (with defaults for nulls):
  - `unit` (or ""), `condition` (or ""), `attachment` (or ""), `depName` (or "default").
- **contract.submitTransaction(**
  - **"CreateAsset"**,
  - assetId,
  - assetName,
  - unit,
  - condition,
  - attachment,
  - String.valueOf(assignTo),
  - username (same as assignTo here),
  - depName,
  - String.valueOf(qty)
- **submitTransaction** is a **write**: it goes to the peer for endorsement, then to the orderer, then to all peers for commit.

**Result:** CreateAsset is submitted to the blockchain network.

---

### **Step 8: Peer endorses the transaction**

- The **peer** (e.g. peer0.org1.ownify.com) receives the invoke request.
- Peer runs the **“basic”** chaincode container and calls **CreateAsset** with the same arguments.
- **Chaincode (Go):**
  - **CreateAsset(ctx, assetID, assetName, unit, condition, attachment, assignTo, username, depName, qty)**:
    - Calls **AssetExists(ctx, assetID)**; if asset already exists, returns error.
    - Builds an **Asset** struct with the given fields and **CreatedAt** / **UpdatedAt** = current time.
    - Calls **PutState(ctx, assetID, asset)** to write the asset to the ledger state.
- Peer signs the proposal response (endorsement) and returns it to the SDK.

**Result:** Transaction is endorsed by the peer; state update is proposed.

---

### **Step 9: Orderer orders the transaction**

- **Fabric Gateway (SDK)** sends the endorsed transaction to an **orderer** (e.g. orderer.ownify.com:7050).
- Orderer batches it into a **block** with other transactions and delivers the block to all peers on the channel.

**Result:** Transaction is ordered and included in a block.

---

### **Step 10: Peers commit the block**

- Each **peer** on the channel receives the block, validates it, and **commits** it to the ledger.
- **State DB (CouchDB)** is updated: the new asset (assetID → JSON) is stored in the world state.
- The transaction is recorded in the **blockchain** (block + transaction ID).

**Result:** Asset is permanently on the ledger and visible to all peers.

---

### **Step 11: API reads back the created asset and clears cache**

- After **submitTransaction** returns successfully, the API calls **contract.evaluateTransaction("QueryAsset", assetId)** to read the new asset from the ledger.
- Response (JSON) is parsed and returned as the created asset payload.
- **@CacheEvict** runs: caches **assetById** and **allAssetsByUser** are cleared so the next list/detail request sees the new asset.
- Optionally **notificationService.sendAssetCreatedNotification(...)** is called to notify the assigned user (if implemented).

**Result:** Service returns the created asset JSON to the controller.

---

### **Step 12: API responds to the UI**

- Controller wraps the created asset in **ApiResponse** and returns **200 OK** with the payload.
- UI receives the success response.

**Result:** Browser gets a successful HTTP response with the new asset data.

---

### **Step 13: UI handles success and navigates**

- **addAsset** resolves without throwing.
- **AssetCreateClient** runs **router.push("/admin/asset")** to go to the asset list.
- If the UI shows a success message (e.g. toast), it is displayed.

**Result:** Admin sees the asset list; the new asset appears (after cache was evicted).

---

## Summary table

| Step | Layer        | Action |
|------|--------------|--------|
| 1    | UI           | Admin opens `/admin/asset/create`, form loads, user list fetched |
| 2    | UI           | Admin fills form and clicks Save |
| 3    | UI           | POST `/api/v1/admin/createAsset` with JWT and JSON body |
| 4    | API          | JWT validated, controller calls assetService.createAsset |
| 5    | API          | runWithWriteLock(admin), getOrCreate Gateway (admin identity) |
| 6    | API          | Generate asset ID, get Contract ("basic", channel-org) |
| 7    | API          | contract.submitTransaction("CreateAsset", ...) |
| 8    | Peer         | Chaincode CreateAsset runs; PutState(assetID, asset) |
| 9    | Orderer      | Transaction ordered into a block |
| 10   | Peers        | Block committed; state in CouchDB, block in ledger |
| 11   | API          | QueryAsset(assetId), cache evict, optional notification |
| 12   | API          | Return 200 with created asset JSON |
| 13   | UI           | router.push("/admin/asset"), show list |

---

## Data flow (key payloads)

**UI → API (POST body):**
- `assetName`, `qty`, `unit`, `condition`, `attachment`, `assignTo` (user ID).

**API → Chaincode (CreateAsset args):**
- assetID (generated), assetName, unit, condition, attachment, assignTo, username, depName, qty.

**Chaincode → Ledger:**
- One key-value in world state: key = assetID, value = JSON of Asset (asset_id, asset_name, qty, unit, condition, attachment, assign_to, username, created_at, updated_at, dep_name).

---

## Error handling (brief)

- **UI:** Network error or non-2xx → toast with message (e.g. “Blockchain orderer unreachable” if orderer/connectivity issue).
- **API:** Invalid payload → 400; not admin → 403; Fabric/orderer error → 503 or 500; asset already exists (chaincode) → error message in response.
- **Chaincode:** Duplicate assetID → “asset already exists”; PutState failure → transaction fails and is not committed.

---

*Use this document for presentations, onboarding, or debugging the asset creation flow.*
