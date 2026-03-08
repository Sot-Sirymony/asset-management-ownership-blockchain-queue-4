# Unstable or Fragile Areas – Checklist

Things that can still be flaky or environment-dependent. Use this list to harden or avoid surprises.

---

## 1. **Blockchain network startup**

- **Orderer “not ready” / SERVICE_UNAVAILABLE**  
  When the channel already exists, `net.sh up` fetches the genesis block from the orderer. The orderer can return SERVICE_UNAVAILABLE for a while (Raft not ready).  
  - **Mitigation:** `net.sh` now waits 30s before channel steps and retries fetch 12×10s.  
  - **If it still fails:** Run `./net.sh reset && ./net.sh up` for a clean channel create.

- **Channel “already exists” but fetch fails**  
  Stale ledger/crypto from a previous run.  
  - **Fix:** `./reset-blockchain-and-up.sh` then restart API (`./restart-api-for-blockchain.sh` or `./fix-blockchain.sh`).

---

## 2. **API ↔ Fabric (Assign Asset / orderer)**

- **Running API without start scripts**  
  If you start the API from an IDE or `mvn spring-boot:run` without setting env, it may use wrong channel, localhost (unstable on some Macs), or discovery on.  
  - **Mitigation:** Use `./start-all-projects.sh`, `./start-api-frontend.sh`, or `./restart-api-for-blockchain.sh` so FABRIC_* and 127.0.0.1 are set.  
  - **Manual run:** Set `FABRIC_CRYPTO_PATH` (absolute), `FABRIC_ORDERER_URL=grpcs://127.0.0.1:7050`, `FABRIC_PEER_URL=grpcs://127.0.0.1:7051`, `FABRIC_CHANNEL=channel-org`, `FABRIC_DISCOVERY=false`.

- **fix-blockchain.sh**  
  Now uses 127.0.0.1 and absolute `FABRIC_CRYPTO_PATH` (aligned with other scripts).

---

## 3. **API – crypto path resolution**

- **resolveCryptoPath()** in `GatewayHelperV1` tries relative paths from `user.dir` (`ownership-network-master/channel`, `../ownership-network-master/channel`, etc.).  
  - **Unstable when:** API is run from a directory where none of those paths exist (e.g. different repo layout or cwd).  
  - **Mitigation:** Start scripts set absolute `FABRIC_CRYPTO_PATH`; prefer those or set the env yourself.

---

## 4. **API – blockchain cache**

- **Asset/issue reads** are cached 30s (`blockchainCacheManager`). After an assign/transfer, the list can show old data for up to 30s.  
  - **Mitigation:** Writes use `@CacheEvict` so cache is cleared on create/update/transfer; if you see stale data, wait a few seconds or refresh.  
  - **Optional:** Lower TTL in `CacheConfig.java` if you need fresher reads (at the cost of more Fabric calls).

---

## 5. **UI – Playwright E2E**

- **Login modal / FE-040**  
  Depends on modal opening after clicking Login, network idle, and no existing session.  
  - **Mitigation:** Tests clear cookies, wait for `#popup-modal`, and use a short delay after click.  
  - **Unstable when:** UI port differs (e.g. 3002); set `PLAYWRIGHT_BASE_URL=http://localhost:3002` if needed.

- **baseURL**  
  Default `http://localhost:3000`. If the dev server runs on another port, tests can fail unless `PLAYWRIGHT_BASE_URL` is set.

- **Retries**  
  Playwright retries are 0 locally (2 in CI). Flaky tests may pass on retry in CI but fail once locally.

---

## 6. **Port and process assumptions**

- **8081 (API), 3000 (UI), 55432 (Postgres), 5984 (CouchDB), 7050/7051 (orderer/peer)**  
  Scripts and tests assume these. If something else uses the port, startup or health checks can fail.  
  - **Mitigation:** Use `kill_port.md` or `lsof -ti :8081 | xargs kill` etc. to free ports; or change config (e.g. `SERVER_PORT`, Postgres port) and keep scripts in sync.

- **“API already running on 8081”**  
  Start scripts skip starting the API but do not restart it with fresh env. An API started earlier without Fabric env will keep using old env.  
  - **Mitigation:** Stop the API (kill 8081) then run the start script again, or use `./restart-api-for-blockchain.sh`.

---

## 7. **Postgres / DB**

- **SPRING_DATASOURCE_URL**  
  Default `localhost:55432`. If Postgres runs on a different host/port or in Docker without that port mapped, the API will fail to start.  
  - **Mitigation:** Start scripts run `ensure_local_postgres` / `bootstrap_local_postgres_schema`; ensure Postgres is on the expected port or set `SPRING_DATASOURCE_*` accordingly.

---

## 8. **CouchDB (history)**

- **getAllAssetHistroy** uses `COUCHDB_BASE_URL` (default `http://localhost:5984`).  
  - **Unstable when:** CouchDB is not running or not mapped to 5984. History will fail; asset list (from chain) can still work.

---

## 9. **Wallet and identities**

- **Fabric wallet** lives in `ownership-api-master/wallet` (or `WALLET_PATH`). Admin/user identities must be enrolled; if the wallet is removed or corrupted, “user does not exist in wallet” will appear.  
  - **Mitigation:** Don’t delete the wallet; re-enroll if you reset the network and need new certs.

---

## 10. **Next.js UI**

- **ChunkLoadError** on routes (e.g. `/admin/asset/create`)  
  Stale or incomplete `.next` build.  
  - **Fix:** `rm -rf ownership-ui-master/.next` then `npm run dev`.

- **EMFILE (too many open files)**  
  Next.js / watchpack can hit the system file descriptor limit.  
  - **Mitigation:** `ulimit -n 10240` or close other apps; dev server usually still runs.

---

## Quick reference

| Area              | Risk                         | Mitigation |
|-------------------|-----------------------------|------------|
| Orderer not ready | Channel fetch fails         | `./net.sh reset && ./net.sh up` |
| Assign asset 503  | API can’t reach orderer     | Start API via script; use 127.0.0.1; `./restart-api-for-blockchain.sh` |
| Stale asset list  | 30s cache                   | Wait or refresh; writes evict cache |
| Playwright login  | Modal/timeout               | Set baseURL if not 3000; ensure API up |
| API started by hand | Wrong Fabric env         | Set FABRIC_* or use start/restart scripts |
| ChunkLoadError    | Bad .next build             | Delete `.next`, restart dev |
| Port in use       | Startup fails               | Kill process on 8081/3000/55432 as needed |
