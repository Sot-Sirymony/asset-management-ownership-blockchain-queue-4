# Version 3 – 200 Test Cases (Blockchain Network & API)

Test cases to find bugs and improve reliability when running against the **blockchain network** (Hyperledger Fabric) and related APIs. Use with a running stack (API + Fabric + DB). Mark **Result** (Pass/Fail/Blocked) and **Bug ID** when executing.

**How to use:** Run API + Fabric (e.g. `./start-all-projects.sh`), then:
- **Automated batch:** `node scripts/run-test-cases-api.js [baseUrl]` — runs a subset of cases (auth, assets, verification, report issue, dashboard, etc.) and prints Pass/Fail. Exit code 1 if any fail (for CI).
- **Manual:** Execute cases from the tables below; record **Result** and **Bug ID** in the execution tracking section.

---

## Legend

| Type | Meaning |
|------|--------|
| **Pos** | Positive – valid inputs, expect success |
| **Neg** | Negative – invalid/unauthorized, expect error |
| **Bound** | Boundary – empty, max length, special chars |
| **BC** | **Y** = directly uses blockchain (Fabric); **N** = API/DB only |

---

## 1. Authentication (TC-001 – TC-020)

| ID | Title | Flow | Steps | Expected | Type | BC |
|----|-------|------|--------|----------|------|-----|
| TC-001 | Login with valid admin credentials | POST /rest/auth/login | Send username+password for admin | 200, JWT in payload | Pos | N |
| TC-002 | Login with valid user credentials | POST /rest/auth/login | Send username+password for user | 200, JWT in payload | Pos | N |
| TC-003 | Login with wrong password | POST /rest/auth/login | Valid user, wrong password | 404, "Password incorrect" | Neg | N |
| TC-004 | Login with non-existent username | POST /rest/auth/login | Unknown username | 404, "Username not exists" | Neg | N |
| TC-005 | Login with empty username | POST /rest/auth/login | username="" | 400 validation error | Neg | N |
| TC-006 | Login with empty password | POST /rest/auth/login | password="" | 400 or 404 | Neg | N |
| TC-007 | Login with null body | POST /rest/auth/login | Body {} or null | 400 | Neg | N |
| TC-008 | Login with extra fields | POST /rest/auth/login | Valid creds + extra field | 200 (ignore extra) or 400 | Bound | N |
| TC-009 | Login case-insensitive username | POST /rest/auth/login | ADMIN vs admin | 200 for same user | Pos | N |
| TC-010 | Login with SQL-like input in username | POST /rest/auth/login | username "admin'--" | 404 or safe error | Neg | N |
| TC-011 | Login with very long password | POST /rest/auth/login | password 10k chars | 400 or 404 | Bound | N |
| TC-012 | Login without Content-Type JSON | POST /rest/auth/login | No Content-Type | 415 or 400 | Neg | N |
| TC-013 | Login with malformed JSON | POST /rest/auth/login | Invalid JSON body | 400 | Neg | N |
| TC-014 | Login GET instead of POST | GET /rest/auth/login | GET request | 405 | Neg | N |
| TC-015 | Login with disabled account | POST /rest/auth/login | Disabled user creds | 404, "Account Disabled" | Neg | N |
| TC-016 | Login then use token for protected API | Auth + GET /api/v1/user/getAllAsset | Login, then GET with Bearer token | 200 + asset list | Pos | Y |
| TC-017 | Access protected API without token | GET /api/v1/user/getAllAsset | No Authorization header | 401 | Neg | Y |
| TC-018 | Access protected API with expired token | GET /api/v1/user/getAllAsset | Expired JWT | 401 | Neg | Y |
| TC-019 | Access protected API with invalid token | GET /api/v1/user/getAllAsset | Random string as Bearer | 401 | Neg | Y |
| TC-020 | User token cannot call admin-only endpoint | GET /api/v1/admin/assetRequest | User JWT | 403 | Neg | Y |

---

## 2. Assets – Blockchain (TC-021 – TC-060)

| ID | Title | Flow | Steps | Expected | Type | BC |
|----|-------|------|--------|----------|------|-----|
| TC-021 | Create asset with valid payload (admin) | POST /admin/createAsset | Valid asset JSON, admin JWT | 200, asset created on chain | Pos | Y |
| TC-022 | Create asset with minimal required fields | POST /admin/createAsset | Only required fields | 200 or 400 per schema | Pos | Y |
| TC-023 | Create asset with assignTo non-existent user | POST /admin/createAsset | assignTo=99999 | 404 or 400 | Neg | Y |
| TC-024 | Create asset with empty assetName | POST /admin/createAsset | assetName="" | 400 validation | Neg | Y |
| TC-025 | Create asset with negative qty | POST /admin/createAsset | qty=-1 | 400 or chain reject | Neg | Y |
| TC-026 | Create asset with zero qty | POST /admin/createAsset | qty=0 | 200 or 400 per biz rule | Bound | Y |
| TC-027 | Create asset with very long assetName | POST /admin/createAsset | 500-char name | 200 or 400 | Bound | Y |
| TC-028 | Create asset without auth | POST /admin/createAsset | No token | 401 | Neg | Y |
| TC-029 | Create asset as user role | POST /admin/createAsset | User JWT | 403 | Neg | Y |
| TC-030 | Get asset by valid ID (on chain) | GET /user/getAsset/{id} | Existing asset id, user JWT | 200, asset details | Pos | Y |
| TC-031 | Get asset by non-existent ID | GET /user/getAsset/{id} | Fake id | 404 | Neg | Y |
| TC-032 | Get asset by empty ID | GET /user/getAsset/ | Empty path or "" | 404 or 400 | Neg | Y |
| TC-033 | Get asset by ID with special chars | GET /user/getAsset/{id} | id with ../ or script | 404 or safe error | Neg | Y |
| TC-034 | Get all assets as user | GET /user/getAllAsset | User JWT | 200, array (from chain/DB) | Pos | Y |
| TC-035 | Get all assets as admin | GET /user/getAllAsset | Admin JWT | 200, array | Pos | Y |
| TC-036 | Update asset with valid payload | PUT /admin/updateAsset/{id} | Update fields, admin JWT | 200, asset updated on chain | Pos | Y |
| TC-037 | Update asset with non-existent ID | PUT /admin/updateAsset/{id} | Fake id | 404 | Neg | Y |
| TC-038 | Update asset as user | PUT /admin/updateAsset/{id} | User JWT | 403 | Neg | Y |
| TC-039 | Transfer asset from owner to new user | PUT /admin/transferAsset/{id} | Valid newAssignTo, admin | 200, ownership on chain | Pos | Y |
| TC-040 | Transfer asset to non-existent user | PUT /admin/transferAsset/{id} | newAssignTo=99999 | 404 | Neg | Y |
| TC-041 | Transfer asset without ownership permission | PUT /admin/transferAsset/{id} | Transfer as non-owner per biz rule | 404 "permission" or 403 | Neg | Y |
| TC-042 | Transfer non-existent asset | PUT /admin/transferAsset/{id} | Fake id | 404 | Neg | Y |
| TC-043 | Delete asset by ID (user/owner) | DELETE /user/deleteAsset/{id} | Valid id, owner JWT | 200 | Pos | Y |
| TC-044 | Delete non-existent asset | DELETE /user/deleteAsset/{id} | Fake id | 404 | Neg | Y |
| TC-045 | Delete asset as non-owner | DELETE /user/deleteAsset/{id} | Other user JWT | 403 or 404 | Neg | Y |
| TC-046 | Get all history (blockchain history) | GET /getAllHistory | Valid JWT | 200, list from chain/DB | Pos | Y |
| TC-047 | Get history by asset ID (admin) | GET /admin/getHistoryById/{id} | Existing asset id | 200, history entries | Pos | Y |
| TC-048 | Get history by non-existent ID | GET /admin/getHistoryById/{id} | Fake id | 404 | Neg | Y |
| TC-049 | Create two assets then verify both in list | Create + GET /user/getAllAsset | Create A, Create B, get all | 200, both present | Pos | Y |
| TC-050 | Create asset then get by ID immediately | Create + GET /user/getAsset/{id} | Create, then GET same id | 200, same data | Pos | Y |
| TC-051 | Create asset then update then get | Create, Update, GET | Full lifecycle | 200, updated values | Pos | Y |
| TC-052 | Create asset then transfer then get history | Create, Transfer, getHistoryById | Ownership change reflected | 200, history shows transfer | Pos | Y |
| TC-053 | Concurrent create asset (two requests same time) | 2x POST /admin/createAsset | Parallel requests | Both 200 or one conflict per idempotency | Bound | Y |
| TC-054 | Create asset with unicode in name | POST /admin/createAsset | assetName with é, 中文 | 200 or 400 | Bound | Y |
| TC-055 | Create asset with HTML in name | POST /admin/createAsset | assetName "<script>" | Stored escaped or 400 | Neg | Y |
| TC-056 | Get asset with ID that exists in DB but not chain | GET /user/getAsset/{id} | Stale or inconsistent id | 404 or 500 handled | Neg | Y |
| TC-057 | Update asset with empty body | PUT /admin/updateAsset/{id} | Body {} | 400 or 200 with no change | Bound | Y |
| TC-058 | Transfer with missing newAssignTo | PUT /admin/transferAsset/{id} | Body without newAssignTo | 400 | Neg | Y |
| TC-059 | Create asset with condition enum value | POST /admin/createAsset | condition=New/Used/etc | 200 | Pos | Y |
| TC-060 | Get all assets when chain returns empty | GET /user/getAllAsset | Empty ledger/DB | 200, [] | Pos | Y |

---

## 3. Asset Requests (TC-061 – TC-090)

| ID | Title | Flow | Steps | Expected | Type | BC |
|----|-------|------|--------|----------|------|-----|
| TC-061 | Create asset request as user | POST /user/createAssetRequest | Valid body, user JWT | 200 | Pos | N |
| TC-062 | Create asset request without auth | POST /user/createAssetRequest | No token | 401 | Neg | N |
| TC-063 | Create asset request with invalid asset ref | POST /user/createAssetRequest | Non-existent asset id | 404 or 400 | Neg | N |
| TC-064 | Get asset request by ID (owner) | GET /assetRequest/{id} | Valid id, JWT | 200 | Pos | N |
| TC-065 | Get asset request by non-existent ID | GET /assetRequest/{id} | Fake id | 404 | Neg | N |
| TC-066 | List asset requests as admin | GET /admin/assetRequest | Admin JWT | 200, list | Pos | N |
| TC-067 | List asset requests as user | GET /user/assetRequest | User JWT | 200, own requests | Pos | N |
| TC-068 | Update asset request as owner | PUT /user/updateAssetRequest/{id} | Valid update, user JWT | 200 | Pos | N |
| TC-069 | Update asset request as other user | PUT /user/updateAssetRequest/{id} | Other user JWT | 403 or 404 | Neg | N |
| TC-070 | Delete asset request as owner | DELETE /user/deleteAssetRequest/{id} | Valid id, user JWT | 200 | Pos | N |
| TC-071 | Delete non-existent request | DELETE /user/deleteAssetRequest/{id} | Fake id | 404 | Neg | N |
| TC-072 | Create request with empty required field | POST /user/createAssetRequest | Missing required | 400 | Neg | N |
| TC-073 | Create request with very long description | POST /user/createAssetRequest | 5k char field | 200 or 400 | Bound | N |
| TC-074 | List requests when user has none | GET /user/assetRequest | New user | 200, [] | Pos | N |
| TC-075 | Admin sees requests from multiple users | GET /admin/assetRequest | After 2 users create requests | 200, both visible | Pos | N |
| TC-076 | Get request by ID without auth | GET /assetRequest/{id} | No token | 401 | Neg | N |
| TC-077 | Update request with invalid status transition | PUT /user/updateAssetRequest/{id} | Invalid status per biz rule | 400 or 409 | Neg | N |
| TC-078 | Create request for asset user doesn't own | POST /user/createAssetRequest | Asset id of other user | 400 or 403 per rule | Neg | N |
| TC-079 | Delete same request twice | DELETE twice /user/deleteAssetRequest/{id} | Second call | 404 or 200 idempotent | Bound | N |
| TC-080 | Create 10 requests then list | Create 10, GET /user/assetRequest | Verify all 10 | 200, 10 items | Pos | N |
| TC-081 | Update request with null optional fields | PUT /user/updateAssetRequest/{id} | Some nulls | 200 or 400 | Bound | N |
| TC-082 | Get request with ID that is numeric string | GET /assetRequest/123 | id=123 | 200 or 404 | Pos | N |
| TC-083 | Create request as admin (if allowed) | POST /user/createAssetRequest | Admin JWT | 200 or 403 per API | Bound | N |
| TC-084 | List admin asset request without pagination | GET /admin/assetRequest | Many records | 200, full list or capped | Bound | N |
| TC-085 | Asset request linked to blockchain asset | Create asset, create request for it | Request references chain asset | 200, consistency | Pos | Y |
| TC-086 | Request for deleted asset | Create request, delete asset, get request | Orphan handling | 404 or 200 with flag | Neg | Y |
| TC-087 | Create request with future date if supported | POST with date field | Future date | 200 or 400 | Bound | N |
| TC-088 | Update request ID in path vs body mismatch | PUT /user/updateAssetRequest/1 body id=2 | Path and body differ | 400 or path wins | Neg | N |
| TC-089 | Create request with special chars in text | POST /user/createAssetRequest | Description with <>&" | 200 escaped or 400 | Bound | N |
| TC-090 | Get request with expired JWT | GET /assetRequest/{id} | Expired token | 401 | Neg | N |

---

## 4. Verification – Blockchain (TC-091 – TC-115)

| ID | Title | Flow | Steps | Expected | Type | BC |
|----|-------|------|--------|----------|------|-----|
| TC-091 | Internal verify existing asset | GET /user/verifyAsset/{id} | Valid asset id, user JWT | 200, verification payload | Pos | Y |
| TC-092 | External verify existing asset | GET /user/verifyAssetExternal/{id} | Valid asset id | 200, audit trail | Pos | Y |
| TC-093 | Get verification trail for asset | GET /user/verificationTrail/{id} | Valid asset id | 200, trail from chain | Pos | Y |
| TC-094 | Verify non-existent asset | GET /user/verifyAsset/{id} | Fake id | 404 | Neg | Y |
| TC-095 | Verify without auth | GET /user/verifyAsset/{id} | No token | 401 | Neg | Y |
| TC-096 | Verify with expired token | GET /user/verifyAsset/{id} | Expired JWT | 401 | Neg | Y |
| TC-097 | Verification trail for asset with no history | GET /user/verificationTrail/{id} | New asset | 200, empty or minimal trail | Pos | Y |
| TC-098 | Verify asset then verify again (idempotent) | GET verify twice same id | Second call | 200 same result | Pos | Y |
| TC-099 | External verify with admin JWT | GET /user/verifyAssetExternal/{id} | Admin token | 200 | Pos | Y |
| TC-100 | Verification trail for transferred asset | Transfer asset then get trail | Trail includes transfer | 200, full history | Pos | Y |
| TC-101 | Verify asset with ID path traversal attempt | GET /user/verifyAsset/../admin | Malformed id | 404 or 400 | Neg | Y |
| TC-102 | Verify with empty ID | GET /user/verifyAsset/ | Empty | 404 or 400 | Neg | Y |
| TC-103 | Internal verify returns verifiedBy current user | GET /user/verifyAsset/{id} | Check payload.verifiedBy | Matches JWT user | Pos | Y |
| TC-104 | External verify returns auditTrail text | GET /user/verifyAssetExternal/{id} | Check payload | auditTrail present | Pos | Y |
| TC-105 | Verification trail source = blockchain | GET /user/verificationTrail/{id} | Check source field | "Hyperledger Fabric" or similar | Pos | Y |
| TC-106 | Verify deleted asset | Delete asset then verify | 404 | Neg | Y |
| TC-107 | Verify with user who has no profile | GET /user/verifyAsset/{id} | User not in DB | 404 "Current user not found" | Neg | Y |
| TC-108 | Concurrent verify same asset (2 requests) | 2x GET verify same id | Both 200 | Pos | Y |
| TC-109 | Verification trail for asset with many transfers | Create, transfer 5x, get trail | 200, 5+ history entries | Pos | Y |
| TC-110 | Verify asset with unicode in chain data | Asset name with unicode, verify | 200, correct encoding | Bound | Y |
| TC-111 | External verify as user role | GET /user/verifyAssetExternal/{id} | User JWT | 200 | Pos | Y |
| TC-112 | Verify asset ID that exists in DB but not chain | Stale id | 404 or 500 handled | Neg | Y |
| TC-113 | Get trail with invalid ID format | GET /user/verificationTrail/abc%00 | Null byte or invalid | 404 or 400 | Neg | Y |
| TC-114 | Verify after chain reorg (if applicable) | N/A or simulate | Consistent response | Pos/Neg per design | Y |
| TC-115 | Verify asset then create report issue for same | Verify then create issue for asset | Both succeed | Pos | Y |

---

## 5. Report Issue – Blockchain (TC-116 – TC-145)

| ID | Title | Flow | Steps | Expected | Type | BC |
|----|-------|------|--------|----------|------|-----|
| TC-116 | Create issue for existing asset | POST /user/createIssue | Valid issue body, asset id, user JWT | 200, issue on chain | Pos | Y |
| TC-117 | Create issue without auth | POST /user/createIssue | No token | 401 | Neg | Y |
| TC-118 | Create issue for non-existent asset | POST /user/createIssue | Fake asset id | 404 or 400 | Neg | Y |
| TC-119 | Create issue with empty problem description | POST /user/createIssue | problem="" or missing | 400 | Neg | Y |
| TC-120 | Get issue by ID (owner) | GET /user/getIssueById/{id} | Valid id, user JWT | 200 | Pos | Y |
| TC-121 | Get issue by non-existent ID | GET /user/getIssueById/{id} | Fake id | 404 | Neg | Y |
| TC-122 | Get all issues as user | GET /user/getAllIssue | User JWT | 200, list (from chain) | Pos | Y |
| TC-123 | Update issue as owner | PUT /user/updateIssue/{id} | Valid update, user JWT | 200 | Pos | Y |
| TC-124 | Update issue as other user | PUT /user/updateIssue/{id} | Other user JWT | 403 or 404 | Neg | Y |
| TC-125 | Delete issue as owner | DELETE /user/deleteIssue/{id} | Valid id, user JWT | 200 | Pos | Y |
| TC-126 | Delete non-existent issue | DELETE /user/deleteIssue/{id} | Fake id | 404 | Neg | Y |
| TC-127 | Create issue with very long problem text | POST /user/createIssue | 10k chars | 200 or 400 | Bound | Y |
| TC-128 | Create issue with attachment field | POST /user/createIssue | attachment filename | 200 | Pos | Y |
| TC-129 | Get all issues when none exist | GET /user/getAllIssue | New user | 200, [] or count 0 | Pos | Y |
| TC-130 | Create two issues for same asset | POST create issue twice, same asset | Both 200, two issues | Pos | Y |
| TC-131 | Create issue then get by ID | Create then GET /user/getIssueById/{id} | 200, same data | Pos | Y |
| TC-132 | Update issue then get | Update then GET | 200, updated data | Pos | Y |
| TC-133 | Delete issue then get by ID | Delete then GET | 404 | Neg | Y |
| TC-134 | Create issue for asset user doesn't own | POST /user/createIssue | Asset of other user | 400 or 403 per rule | Neg | Y |
| TC-135 | Get issue without auth | GET /user/getIssueById/{id} | No token | 401 | Neg | Y |
| TC-136 | Report issue chain consistency | Create issue, verify in getAllIssue | Same data as chain | Pos | Y |
| TC-137 | Create issue with HTML in problem | POST problem "<script>" | Escaped or 400 | Neg | Y |
| TC-138 | Update issue with empty body | PUT /user/updateIssue/{id} | {} | 400 or 200 no change | Bound | Y |
| TC-139 | List issues pagination if supported | GET /user/getAllIssue | Many issues | 200, list/count | Bound | Y |
| TC-140 | Delete same issue twice | DELETE twice | Second 404 or idempotent | Bound | Y |
| TC-141 | Create issue then verify asset still verifiable | Create issue, verify asset | Verify 200 | Pos | Y |
| TC-142 | Get issue by ID with special chars | GET /user/getIssueById/{id} | id with ../ | 404 or 400 | Neg | Y |
| TC-143 | Create issue with unicode in problem | POST problem with 中文 | 200 | Bound | Y |
| TC-144 | Report issue for deleted asset | Asset deleted, create issue for it | 404 or 400 | Neg | Y |
| TC-145 | Admin get all issues (if endpoint exists) | Admin list issues | 200 or 403 per API | Pos/Neg | Y |

---

## 6. Users & Profile (TC-146 – TC-170)

| ID | Title | Flow | Steps | Expected | Type | BC |
|----|-------|------|--------|----------|------|-----|
| TC-146 | Register user as admin | POST /admin/register_user | Valid user body, admin JWT | 200 | Pos | N |
| TC-147 | Register user without admin auth | POST /admin/register_user | User JWT or no token | 403 or 401 | Neg | N |
| TC-148 | Register user with duplicate username | POST twice same username | Second 400 or 409 | Neg | N |
| TC-149 | Get all users as admin | GET /admin/getAllUser | Admin JWT | 200, list | Pos | N |
| TC-150 | Get user by ID (admin) | GET /admin/getUser/{id} | Valid id, admin JWT | 200 | Pos | N |
| TC-151 | Get user by non-existent ID | GET /admin/getUser/99999 | 404 | Neg | N |
| TC-152 | Update user as admin | PUT /admin/updateUser/{id} | Valid update, admin JWT | 200 | Pos | N |
| TC-153 | Delete user as admin | DELETE /admin/deleteUser/{id} | Valid id, admin JWT | 200 | Pos | N |
| TC-154 | Get profile as logged-in user | GET /getProfile | User JWT | 200, own profile | Pos | N |
| TC-155 | Update profile as user | PUT /updateProfile | Valid update, user JWT | 200 | Pos | N |
| TC-156 | Change password with correct old password | PUT /changePassword | Valid old + new, user JWT | 200 | Pos | N |
| TC-157 | Change password with wrong old password | PUT /changePassword | Wrong old password | 404 or 400 | Neg | N |
| TC-158 | Register user with empty username | POST /admin/register_user | username="" | 400 | Neg | N |
| TC-159 | Register user with weak password | POST /admin/register_user | password=123 | 200 or 400 per rule | Bound | N |
| TC-160 | Get profile without auth | GET /getProfile | No token | 401 | Neg | N |
| TC-161 | Update user as non-admin | PUT /admin/updateUser/{id} | User JWT | 403 | Neg | N |
| TC-162 | Delete own user (admin) | DELETE /admin/deleteUser/{id} | Admin deletes self | 200 or 400 per rule | Bound | N |
| TC-163 | Get all users as user role | GET /admin/getAllUser | User JWT | 403 | Neg | N |
| TC-164 | Register user then login | Register, POST /rest/auth/login | 200 login | Pos | N |
| TC-165 | Change password then login with new | Change password, login new | 200 | Pos | N |
| TC-166 | Profile update with unicode name | PUT /updateProfile | name with é | 200 | Bound | N |
| TC-167 | Get user by ID zero or negative | GET /admin/getUser/0 | 404 or 400 | Neg | N |
| TC-168 | Register user with very long username | POST /admin/register_user | 500 char username | 400 or 200 | Bound | N |
| TC-169 | Update profile with null optional fields | PUT /updateProfile | Some nulls | 200 or 400 | Bound | N |
| TC-170 | New password same as old in changePassword | PUT /changePassword | new == old | 400 or 404 | Neg | N |

---

## 7. Departments & Dashboard (TC-171 – TC-190)

| ID | Title | Flow | Steps | Expected | Type | BC |
|----|-------|------|--------|----------|------|-----|
| TC-171 | List departments as admin | GET /admin/department | Admin JWT | 200, list | Pos | N |
| TC-172 | Get department by ID | GET /admin/department/{id} | Valid id, admin JWT | 200 | Pos | N |
| TC-173 | Create department as admin | POST /admin/department | Valid body, admin JWT | 200 | Pos | N |
| TC-174 | Update department as admin | PUT /admin/department/{id} | Valid update, admin JWT | 200 | Pos | N |
| TC-175 | Delete department as admin | DELETE /admin/department/{id} | Valid id, admin JWT | 200 | Pos | N |
| TC-176 | Get dashboard as admin | GET /admin/dashboard | Admin JWT | 200, counts (assets, requests, issues) | Pos | Y |
| TC-177 | Dashboard when no data | GET /admin/dashboard | Fresh DB/chain | 200, zeros | Pos | Y |
| TC-178 | List departments as user | GET /admin/department | User JWT | 403 | Neg | N |
| TC-179 | Create department without auth | POST /admin/department | No token | 401 | Neg | N |
| TC-180 | Get department by non-existent ID | GET /admin/department/99999 | 404 | Neg | N |
| TC-181 | Create department with duplicate name | POST twice same name | Second 400 or 409 or 200 | Bound | N |
| TC-182 | Delete department then get by ID | Delete then GET | 404 | Neg | N |
| TC-183 | Update department with empty name | PUT /admin/department/{id} | name="" | 400 | Neg | N |
| TC-184 | Dashboard counts match actual assets/issues | Create assets/issues, GET dashboard | Counts consistent | Pos | Y |
| TC-185 | Delete non-existent department | DELETE /admin/department/99999 | 404 | Neg | N |
| TC-186 | Create department with very long name | POST name 500 chars | 200 or 400 | Bound | N |
| TC-187 | Get department ID path traversal | GET /admin/department/../1 | 404 or 400 | Neg | N |
| TC-188 | List departments when empty | GET /admin/department | No departments | 200, [] | Pos | N |
| TC-189 | Update department as user | PUT /admin/department/{id} | User JWT | 403 | Neg | N |
| TC-190 | Dashboard after creating asset (blockchain) | Create asset, GET dashboard | totalAsset or similar incremented | Pos | Y |

---

## 8. Files (TC-191 – TC-200)

| ID | Title | Flow | Steps | Expected | Type | BC |
|----|-------|------|--------|----------|------|-----|
| TC-191 | Upload file (jpg) | POST /api/v1/files | Multipart jpg, auth | 200, file ref | Pos | N |
| TC-192 | Upload file (png) | POST /api/v1/files | Multipart png | 200 | Pos | N |
| TC-193 | Upload without auth | POST /api/v1/files | No token | 401 | Neg | N |
| TC-194 | Upload invalid type (e.g. exe) | POST /api/v1/files | exe or disallowed type | 400 or 404 "File must be jpg, png, jpeg" | Neg | N |
| TC-195 | Get file by filename | GET /api/v1/files?fileName=x | Valid name after upload | 200, binary | Pos | N |
| TC-196 | Get file by non-existent name | GET /api/v1/files?fileName=fake | 404 "File not found" | Neg | N |
| TC-197 | List files | GET /api/v1/files | Auth | 200, list | Pos | N |
| TC-198 | Upload empty file | POST /api/v1/files | 0-byte file | 200 or 400 | Bound | N |
| TC-199 | Upload very large file | POST /api/v1/files | 10MB+ if allowed | 200 or 413 | Bound | N |
| TC-200 | Upload then use file ref in report issue | Upload, create issue with attachment | 200 both | Pos | N |

---

## Execution tracking (template)

Use this table to record runs. Copy and fill **Result** (Pass / Fail / Blocked) and **Bug ID** (if failed).

| ID range | Result | Bug ID | Date |
|----------|--------|--------|------|
| TC-001 – TC-020 | | | |
| TC-021 – TC-060 | | | |
| TC-061 – TC-090 | | | |
| TC-091 – TC-115 | | | |
| TC-116 – TC-145 | | | |
| TC-146 – TC-170 | | | |
| TC-171 – TC-190 | | | |
| TC-191 – TC-200 | | | |

---

*Run with API + blockchain network up (e.g. `./start-all-projects.sh`). Use [V3-Test-Environment.md](V3-Test-Environment.md) for setup. Failures are candidates for bug fixes and improvement.*
