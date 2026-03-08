# 100 Q&A for Teacher Demo – Blockchain & This Project

Preparation for questions your teacher may ask during or after the demo, especially about blockchain.

---

## Part 1: Blockchain fundamentals (Q1–Q20)

**Q1. What is a blockchain?**  
A distributed ledger that stores transactions in blocks linked by hashes. Data is append-only and replicated across nodes so participants can verify history without a central authority.

**Q2. What is a distributed ledger?**  
A ledger (record of transactions/state) that is copied and shared across multiple nodes. No single party has exclusive control; consensus is used to agree on updates.

**Q3. What is consensus?**  
The process by which nodes in a network agree on the same version of the ledger (which transactions are valid and in what order) before appending them to the chain.

**Q4. What is the difference between public and permissioned blockchain?**  
Public: anyone can join, read, and submit transactions (e.g. Bitcoin, Ethereum). Permissioned: only identified, authorized participants can join; access is controlled (e.g. Hyperledger Fabric). Our project uses a permissioned blockchain.

**Q5. Why use blockchain for asset ownership?**  
To get immutability (past records cannot be altered), transparency (audit trail), and verifiability (transaction IDs and consensus) so ownership and history are trustworthy without a single central database.

**Q6. What is immutability in blockchain?**  
Once data is committed to the ledger, it is not changed or deleted in place. Corrections are done by new transactions (e.g. transfer), so history remains intact.

**Q7. What is a smart contract?**  
Code that runs on the blockchain and defines rules and logic for updating ledger state. In Hyperledger Fabric it is called chaincode.

**Q8. What is a transaction in blockchain?**  
A single operation (e.g. create asset, transfer) proposed by a client, endorsed by peers, ordered by the orderer, and then committed by all peers. Each transaction gets a unique transaction ID (tx_id).

**Q9. What is a block?**  
A batch of ordered transactions with a header (e.g. block number, previous block hash). Blocks form the chain; each block is linked to the previous one by hash.

**Q10. What is the world state?**  
A key–value view of the current state of the ledger (e.g. assetID → asset JSON). It is derived from the full transaction history and updated when new transactions are committed.

**Q11. What is endorsement?**  
In Fabric, one or more peers run the chaincode and sign the result (proposal response). The client collects enough endorsements according to the policy before submitting to the orderer.

**Q12. What is the orderer?**  
A node that orders transactions into blocks and broadcasts blocks to peers. It does not execute chaincode; it only establishes the order of transactions for consensus.

**Q13. What is a peer?**  
A node that holds a copy of the ledger (blocks + world state), runs chaincode (endorsement), and validates/commits blocks.

**Q14. What is a channel?**  
A private sub-network where only members of that channel see the ledger and can transact. Our project uses one channel: channel-org.

**Q15. What is chaincode?**  
In Hyperledger Fabric, the program (smart contract) that runs in a container on peers and reads/writes the ledger. Our chaincode is written in Go and named "basic".

**Q16. What is the difference between read and write in blockchain?**  
Read (query): read world state or history; no block is created; typically evaluateTransaction in the SDK. Write: change state; creates a transaction that is endorsed, ordered, and committed; submitTransaction in the SDK.

**Q17. What is a transaction ID (tx_id)?**  
A unique identifier for a single transaction on the ledger. Used for auditing and verification; our project shows tx_id in the history feature.

**Q18. What is double-spend?**  
Using the same asset/coin twice. Blockchain prevents it by ordering transactions and updating state consistently; our chaincode checks asset existence and ownership before transfer.

**Q19. What is a hash in blockchain?**  
A fixed-size fingerprint of data (e.g. SHA-256). Blocks contain the hash of the previous block so the chain is tamper-evident: changing past data would change hashes and be detected.

**Q20. Why not use a normal database instead of blockchain?**  
A central database can be altered by whoever controls it. Blockchain gives multiple parties a shared, agreed, and auditable record that no single party can rewrite, which is useful for ownership and compliance.

---

## Part 2: Hyperledger Fabric (Q21–Q50)

**Q21. What is Hyperledger Fabric?**  
An open-source, permissioned blockchain platform. It supports smart contracts (chaincode), channels, and pluggable consensus (we use Raft).

**Q22. What are the main components in our Fabric network?**  
Certificate Authorities (CA), orderers (3), peers (2 for Org1), CouchDB (state DB per peer), and CLI for channel/chaincode operations.

**Q23. What is the role of the Certificate Authority (Fabric CA)?**  
To issue identities (enrollment): certificates and keys for organizations, orderers, and users. Our project has ca_orderer and ca.org1.ownify.com.

**Q24. What is MSP?**  
Membership Service Provider. It defines the rules for validating identities and roles for an organization (e.g. Org1MSP). Certificates from the CA are used by MSP.

**Q25. What is the channel name in this project?**  
channel-org. The API and chaincode use this channel; it is set in connection.yaml and FABRIC_CHANNEL.

**Q26. What is the chaincode name in this project?**  
basic. It is deployed on channel-org and contains asset and report-issue logic.

**Q27. What language is our chaincode written in?**  
Go (Golang). We use the fabric-contract-api-go library.

**Q28. What is Raft consensus?**  
A crash-fault-tolerant ordering protocol. We have 3 orderers; one is leader and the others are followers. They agree on the order of transactions before creating blocks.

**Q29. Why do we have multiple orderers?**  
For availability and fault tolerance: if one orderer fails, the others continue. The SDK can try another orderer (e.g. 7050, 8050, 9050) if one is unreachable.

**Q30. What is CouchDB used for in Fabric?**  
As the state database for the peer. It stores the world state (key–value) and supports rich queries. We use one CouchDB per peer (couchdb0, couchdb1, couchdb2).

**Q31. What is the difference between LevelDB and CouchDB in Fabric?**  
LevelDB is key–value only. CouchDB supports JSON documents and rich queries. Our project uses CouchDB.

**Q32. What is endorsement policy?**  
The rule that defines which peers must endorse a transaction (e.g. “majority of orgs”). Our chaincode uses the default (e.g. any one peer from the org).

**Q33. What is the Gateway in Fabric?**  
A client API (e.g. Fabric Gateway Java) that connects to the network using a connection profile and wallet identity, and invokes chaincode (evaluate/submit). Our API uses it to talk to the peer and orderer.

**Q34. What is the wallet in our project?**  
A file-based store (in ownership-api-master/wallet) that holds enrolled identities: certificate and private key for each user (e.g. admin) used to sign transactions when the API calls the blockchain.

**Q35. What is the connection profile (connection.yaml)?**  
A YAML file that describes the network: peers, orderers, channel, TLS cert paths, and which peers are used for endorsement/query. The API loads it and replaces paths/URLs when running on the host (e.g. 127.0.0.1).

**Q36. What is evaluateTransaction?**  
A read-only chaincode invocation. It runs on the peer and returns a result but does not create a transaction that gets ordered and committed. Used for QueryAsset, QueryAllAssets, GetAssetHistory.

**Q37. What is submitTransaction?**  
A write chaincode invocation. The peer endorses, the result is sent to the orderer, and the transaction is committed to the ledger. Used for CreateAsset, UpdateAsset, TransferAsset, DeleteAsset.

**Q38. What chaincode functions do we have for assets?**  
CreateAsset, QueryAsset, QueryAllAssets, UpdateAsset, TransferAsset, DeleteAsset, GetAssetHistory. We also have report-issue functions (e.g. CreateReportIssue, QueryReportIssue).

**Q39. How does CreateAsset work in our chaincode?**  
It checks the asset does not already exist (AssetExists), builds an Asset struct with the given parameters and timestamps, then calls PutState(ctx, assetID, asset) to write to the ledger.

**Q40. How does TransferAsset work in our chaincode?**  
It queries the asset (QueryAsset), validates assetID and newAssignTo are not empty, ensures newAssignTo is different from current AssignTo, updates AssignTo and UpdatedAt, then PutState to save. The transaction is then ordered and committed.

**Q41. How does GetAssetHistory work?**  
It uses ctx.GetStub().GetHistoryForKey(assetID) to get all historical values for that key, then formats each entry with TxID and timestamp (AssetHistoryEntry). This provides the audit trail.

**Q42. What is GetStateByRange used for?**  
In QueryAllAssets we use GetStateByRange("", "~") to iterate over all keys in the world state and return all assets. Empty and "~" mean from first to last key.

**Q43. What is PutState?**  
Chaincode API to write a key–value pair to the ledger state. Used when we create or update an asset (CreateAsset, UpdateAsset, TransferAsset).

**Q44. What is DelState?**  
Chaincode API to delete a key from the ledger state. Used in DeleteAsset.

**Q45. What is GetHistoryForKey?**  
Chaincode API that returns the history of all changes (and deletes) for a given key. We use it in GetAssetHistory to build the audit trail with tx_id and timestamp.

**Q46. Why does the API use 127.0.0.1 for peer/orderer URLs when running on host?**  
So the API (running on the host) can reach Docker-mapped ports (7050, 7051, etc.). Using 127.0.0.1 avoids IPv6/localhost issues and TLS SNI on some systems.

**Q47. What is FABRIC_DISCOVERY?**  
When true, the SDK uses service discovery to find endorsers. We set it to false because our setup does not use discovery roles; we use the static connection profile instead.

**Q48. What is FABRIC_CHANNEL?**  
Environment variable for the channel name. Our project uses channel-org. The API defaults to it so all chaincode calls use this channel.

**Q49. What is FABRIC_CRYPTO_PATH?**  
Path to the directory containing crypto-config (MSP and TLS certs). The API uses it to replace /etc/hyperledger/fabric in the connection profile when running on the host.

**Q50. How is the asset ID generated in this project?**  
In the API: AssetIdGenerator.generateAssetId() produces "asset-" + UUID (without hyphens). The ID is unique and is passed to CreateAsset as the ledger key.

---

## Part 3: This project – architecture and flow (Q51–Q75)

**Q51. What are the three main parts of this project?**  
(1) Frontend: Next.js/Refine UI. (2) Backend: Spring Boot API. (3) Blockchain: Hyperledger Fabric network (orderers, peers, chaincode, CouchDB).

**Q52. How does the UI talk to the blockchain?**  
The UI does not talk to the blockchain directly. It calls the REST API with JWT; the API uses the Fabric Gateway and wallet to invoke chaincode on the peer/orderer.

**Q53. What API endpoint creates an asset on the blockchain?**  
POST /api/v1/admin/createAsset. The body contains assetName, qty, unit, condition, attachment, assignTo. The API generates the asset ID and calls CreateAsset on the chaincode.

**Q54. What API endpoint gets all assets?**  
GET /api/v1/user/getAllAsset. The API calls QueryAllAssets on the chaincode, then filters by current user (non-admin see only their assets; admin sees all).

**Q55. What API endpoint transfers an asset?**  
PUT or POST for transfer (e.g. transfer asset). The API calls TransferAsset(assetId, newAssignTo) on the chaincode.

**Q56. Who can create an asset?**  
Only an admin (role ADMIN). The API checks the JWT and role; only then does it call CreateAsset.

**Q57. Who can transfer an asset?**  
The user who currently owns the asset (assign_to matches their user ID), or admin depending on your API rules. The chaincode only updates AssignTo; the API enforces who can call it.

**Q58. Where is the asset data stored?**  
On the blockchain: world state (CouchDB) keyed by asset ID. Optionally the API caches reads in memory (Caffeine) for a short time. User/department data are in PostgreSQL, not on the chain.

**Q59. What is stored in PostgreSQL in this project?**  
Users, departments, and asset requests (with status, assigned_asset_id). Not the asset ledger itself; that is on Fabric.

**Q60. What is the flow when admin clicks “Save” on Create Asset?**  
UI sends POST /api/v1/admin/createAsset with JWT and JSON → API validates admin, generates asset ID, gets Gateway (admin identity), calls submitTransaction("CreateAsset", ...) → peer runs chaincode → orderer orders → peers commit → API reads back with QueryAsset and returns response → UI redirects to asset list.

**Q61. What is the purpose of the write lock (runWithWriteLock) in the API?**  
To serialize write operations (e.g. CreateAsset, TransferAsset) per user identity so two concurrent requests for the same identity do not conflict on the same Gateway/connection.

**Q62. Why do we evict cache after creating an asset?**  
So the next getAllAsset or getAssetById returns fresh data from the ledger instead of stale cached data. We use @CacheEvict on createAsset, updateAsset, transferAsset, deleteAsset.

**Q63. What is the asset request workflow?**  
User submits a request (stored in DB) → Admin approves → API creates the asset on the blockchain (CreateAsset) and links it to the user (updates request with assigned_asset_id and status). So “approve” means “create on chain and assign.”

**Q64. How does the history feature get tx_id?**  
The API calls GetAssetHistory(assetId) on the chaincode. The chaincode uses GetHistoryForKey and returns each entry with TxID (from the Fabric response) and timestamp. The UI displays this in the History page.

**Q65. What is the difference between QueryAsset and QueryAllAssets?**  
QueryAsset returns one asset by ID. QueryAllAssets returns all assets by iterating the world state with GetStateByRange("", "~"). The API then filters by current user for non-admin.

**Q66. What happens if the orderer is down when we try to create an asset?**  
submitTransaction will fail (e.g. “orderer unreachable” or 503). The API returns an error to the UI. We mitigate by using 127.0.0.1, multiple orderers (7050, 8050, 9050), and restart-api-for-blockchain.sh.

**Q67. What is the purpose of the CLI container in our Fabric setup?**  
To run channel and chaincode lifecycle commands: create/join channel, install and approve/commit chaincode. It is used by net.sh (e.g. channel create, peer channel join, lifecycle chaincode approve/commit).

**Q68. How do we deploy or update chaincode?**  
Using scripts in channel/deploy-chaincode: we package the Go code, install on peers, approve for the channel, and commit. redeploy-chaincode.sh bumps version/sequence for updates.

**Q69. What is genesis block?**  
The first block of a channel (block 0). It contains the channel configuration. Created by configtxgen and used by the orderer to bootstrap the channel.

**Q70. What is the difference between install and approve/commit for chaincode?**  
Install: put the chaincode package on each peer’s filesystem. Approve: each org agrees to the chaincode definition (name, version). Commit: the definition is committed to the channel and the chaincode becomes active.

**Q71. Why do we need TLS for peer and orderer?**  
To encrypt and authenticate communication between the API (or CLI) and Fabric nodes. Our connection profile uses grpcs and tlsCACerts so the client trusts the server certificate.

**Q72. What is the wallet identity used for when the API creates an asset?**  
The API loads the admin (or the logged-in user) identity from the wallet and uses it to sign the transaction proposal. The peer verifies the signature and MSP before endorsing.

**Q73. What is report issue in this project?**  
A feature to create/update/delete “report issue” records linked to assets. They can be stored on the blockchain (e.g. CreateReportIssue, QueryReportIssue) for audit, similar to assets.

**Q74. Can a user see another user’s assets?**  
Non-admin users see only assets where assign_to equals their user ID (filtered in getAllAsset). Admins see all assets. This is enforced in the API, not in the chaincode (chaincode returns all; API filters).

**Q75. What is the role of NextAuth in this project?**  
To authenticate users (login with credentials), issue/maintain session and JWT, and redirect by role (admin → /admin/dashboard, user → /user/asset). The API then validates the JWT and checks roles for endpoints.

---

## Part 4: Security and identity (Q76–Q88)

**Q76. How does the API authenticate the user?**  
The UI sends the JWT in the Authorization header. The API (Spring Security) validates the JWT and loads the user (e.g. from DB or token claims) and checks role (ADMIN/USER) for protected endpoints.

**Q77. What is the difference between application user (DB) and Fabric identity (wallet)?**  
Application user: stored in PostgreSQL; used for login and API authorization. Fabric identity: certificate and key in the wallet; used to sign blockchain transactions. Often the admin user has a corresponding wallet identity for writes.

**Q78. Why is the blockchain “permissioned”?**  
Only known organizations and users (with certificates from the CA) can join the network and submit transactions. This fits enterprise use where participants are identified and access is controlled.

**Q79. What could happen if someone stole the wallet private key?**  
They could sign transactions as that identity (e.g. create or transfer assets). So the wallet must be kept secure; in production, use proper secret management and access control.

**Q80. Does the chaincode check that the caller is the owner before transfer?**  
Our chaincode only checks that the asset exists and newAssignTo is different. The API enforces that only the current owner (or admin) can call the transfer endpoint. So authorization is in the API; chaincode does the state update.

**Q81. What is TLS in the context of Fabric?**  
Transport Layer Security: all gRPC connections to peers and orderers use TLS (grpcs). The client uses the server’s CA certificate (tlsCACerts in connection.yaml) to verify the server.

**Q82. What is FABRIC_USE_HOSTS?**  
When set to true, the API does not replace peer/orderer hostnames with 127.0.0.1; it uses the hostnames from the connection profile. You then need /etc/hosts (or DNS) to resolve those hostnames to the correct IP (e.g. 127.0.0.1).

**Q83. How do we enroll a new user for the wallet?**  
Using Fabric CA: register the user with the CA, then enroll to get cert and key. The API or a script stores them in the wallet directory. Our project may use a script or the Admin identity for API operations.

**Q84. What is the purpose of ssl-target-name-override in connection.yaml?**  
Because we connect to 127.0.0.1 but the server cert is for peer0.org1.ownify.com, the client would reject the cert. ssl-target-name-override tells the client to expect that hostname in the TLS handshake so verification succeeds.

**Q85. What data is not on the blockchain?**  
Users, passwords, departments, asset request metadata (e.g. status, request text), and application session/JWT. Only asset (and report issue) state and history are on the ledger.

**Q86. Can we delete an asset from the blockchain?**  
Yes. Our chaincode has DeleteAsset(assetID), which calls DelState(assetID). The key is removed from the world state; history (GetHistoryForKey) can still show that the key was deleted and when.

**Q87. What is the lifecycle of a transaction from the API’s perspective?**  
Client calls API → API gets Gateway and Contract → submitTransaction(...) → SDK sends proposal to peer → peer endorses → SDK sends to orderer → orderer includes in block → block is delivered to peers → peers commit → submitTransaction returns to API → API may query state and return result.

**Q88. What is the purpose of the Fabric Gateway cache in the API?**  
To reuse one Gateway per user identity instead of opening a new connection for every request. This reduces overhead and connection churn; the cache is keyed by username (wallet identity).

---

## Part 5: Operations and troubleshooting (Q89–Q100)

**Q89. How do we start the full project (blockchain + API + UI)?**  
Run ./start-all-projects.sh from the repo root. It starts the Fabric network (net.sh up), then the API with Fabric env vars, then the UI. Alternatively: start-api-frontend.sh for API + UI only (blockchain must already be up).

**Q90. What does net.sh up do?**  
Starts CAs, generates crypto (if needed), generates channel artifacts, starts orderers and peers and CouchDB, creates/joins the channel, deploys chaincode, and optionally starts the explorer.

**Q91. What does net.sh reset do?**  
Stops containers, removes volumes, and deletes crypto and channel-artifacts so the next net.sh up does a clean setup (new channel create, fresh ledger).

**Q92. When do we need to run net.sh reset?**  
When the channel already exists but the orderer returns SERVICE_UNAVAILABLE when fetching the genesis block (stale or inconsistent state), or after changing crypto/network topology and we want a clean channel.

**Q93. Why might “Assign Asset” fail with “orderer unreachable”?**  
The API cannot reach the orderer (wrong URL, orderer down, or TLS/network issue). Fix: ensure the blockchain is up (docker ps), set FABRIC_ORDERER_URL=grpcs://127.0.0.1:7050 (or 8050/9050), set FABRIC_CRYPTO_PATH, and restart the API (e.g. restart-api-for-blockchain.sh).

**Q94. What is restart-api-for-blockchain.sh for?**  
To stop the API on 8081 and start it again with the correct Fabric env (FABRIC_CRYPTO_PATH, FABRIC_ORDERER_URL, FABRIC_PEER_URL, FABRIC_CHANNEL, FABRIC_DISCOVERY=false) so Assign Asset and other write operations work.

**Q95. Why do we set FABRIC_DISCOVERY=false?**  
With discovery enabled, the SDK looks for peers with the “discover” role. Our connection profile does not configure that, so we get an error. With discovery false, the SDK uses the static peer/orderer list from the connection profile.

**Q96. What port does the API use?**  
8081 (configurable via server.port or SERVER_PORT). The UI calls NEXT_PUBLIC_API_URL (e.g. http://localhost:8081).

**Q97. What port does the UI use?**  
3000 by default (Next.js). Playwright tests use baseURL http://localhost:3000 unless PLAYWRIGHT_BASE_URL is set.

**Q98. How can we verify that an asset is really on the blockchain?**  
Use the History page (or getHistoryById API) and check the tx_id. We can also use the Fabric explorer (if running) or peer chaincode query (e.g. QueryAsset, GetAssetHistory) via CLI.

**Q99. What is the difference between channel-org and mychannel?**  
mychannel is a default name in many Fabric samples. Our project uses channel-org as the channel name. The API and net.sh are configured for channel-org; using mychannel would cause “channel not found” if that channel does not exist.

**Q100. In one sentence, what does this project demonstrate?**  
It demonstrates a full-stack asset ownership system where assets are created, updated, transferred, and audited on a permissioned Hyperledger Fabric blockchain, with a Next.js UI and Spring Boot API that use the Fabric Gateway to invoke Go chaincode for immutable, verifiable ownership and history.

---

*Use this document to prepare for your teacher’s questions. Focus on the areas your teacher usually emphasizes (e.g. consensus, chaincode, or security) and practice explaining flows (e.g. asset creation, transfer) in your own words.*
