# From repo root: stop API on 8081, then start it again
lsof -ti :8081 | xargs kill 2>/dev/null; sleep 2
cd ownership-api-master && mvn spring-boot:run > api.log 2>&1 &




Token obtained. Calling GET /api/v1/user/getAllAsset ...

HTTP 200 OK
{
    "message": "Success",
    "payload": [
        {
            "assetId": "AssetSeed001",
            "assetName": "Seed Laptop",
            "qty": "1",
            "condition": "New",
            "attachment": "seed-attachment",
            "assignDate": "2026-03-08 07:03:09.122323297 +0000 UTC m=+141.529809023",
            "depName": "IT",
            "assignTo": {
                "userId": "1",
                "fullName": "",
                "profileImg": "",
                "department": ""
            }
        }
    ],
    "httpStatus": "OK",
    "timestamp": "2026-03-08T07:08:25.255+00:00"
}

--- Total assets: 1 ---
sotsirymony@Sots-MacBook-Pro All In One Source % 





1. Run the full stack (if not already)
From repo root:
sh
./start-all-projects.sh
Or API + UI only:
./start-api-frontend.sh
./start-api-frontend.sh
Then open the UI (e.g. http://localhost:3000 or 3002), log in as admin, and try Assign asset at
http://localhost:3000/admin/asset/create
2. Confirm Assign asset is stable
Create an asset, set Assign To, save.
If it fails (e.g. orderer unreachable), run:
  ./restart-api-for-blockchain.sh
  ./restart-api-for-blockchain.sh