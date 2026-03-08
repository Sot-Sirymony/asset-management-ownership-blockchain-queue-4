# Project Features – Asset Ownership Management

List of features in the current project.

---

## 1. Asset creation and registration on blockchain

Admin creates assets (name, quantity, unit, condition, department) and assigns them to users. Each asset is recorded on the Hyperledger Fabric ledger via the CreateAsset chaincode function.

---

## 2. Secure ownership transfer between users

Users who own an asset can transfer ownership to another user. The transfer is submitted as a transaction to the blockchain (TransferAsset), so ownership change is immutable and verifiable.

---

## 3. Complete audit trail and verification

Full history of each asset (create, update, transfer) is stored on the blockchain. History is queryable with transaction IDs (tx_id) for audit and verification. Users see their asset history; admins can view full history.

---

## 4. User and department management

Admin manages users (create, edit, roles) and departments. User and department data are stored in PostgreSQL and used for asset assignment, requests, and reporting.

---

## 5. Asset request workflow

Users submit requests for new assets. Requests are stored in the database with status (e.g. pending, approved, rejected). Admin approves a request → the system creates the asset on the blockchain and links it to the requesting user.

---

## 6. Report issue tracking

Users and admins can create, update, and delete report issues linked to assets. Report issue data can be stored on the blockchain for an auditable record of issues.

---

*Use this list for presentations, README, or project documentation.*
