@echo off
REM Start API backend on Windows with Fabric env vars set so the CA cert path is resolved.
REM Run from the "All In One Source" root directory (same as start-all-projects.sh on Unix).
REM Ensure the blockchain network is running (e.g. Docker) before starting the API.

set ROOT=%~dp0
set ROOT=%ROOT:~0,-1%

set FABRIC_CRYPTO_PATH=%ROOT%\ownership-network-master\channel
set WALLET_PATH=%ROOT%\ownership-api-master\wallet
REM Optional: full path to CA cert if you prefer to set it explicitly
REM set FABRIC_CA_PEM_FILE=%FABRIC_CRYPTO_PATH%\crypto-config\peerOrganizations\org1.ownify.com\users\Admin@org1.ownify.com\tls\ca.crt

set FABRIC_PEER_URL=grpcs://localhost:7051
set FABRIC_ORDERER_URL=grpcs://localhost:7050
set FABRIC_CHANNEL=channel-org
set FABRIC_DISCOVERY=false
set COUCHDB_BASE_URL=http://localhost:5984

echo Using FABRIC_CRYPTO_PATH=%FABRIC_CRYPTO_PATH%
echo Using WALLET_PATH=%WALLET_PATH%
echo.
echo Starting API backend (ensure blockchain network is running)...
cd /d "%ROOT%\ownership-api-master"
mvn spring-boot:run
