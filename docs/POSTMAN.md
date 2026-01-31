# Postman collection

This repo includes a Postman collection and environment for the Chronos API.

Files
- `postman/Chronos.postman_collection.json` — the Postman collection
- `postman/Chronos.postman_environment.json` — Postman environment (baseUrl, swagger creds)
- `package.json` — provides a `postman` npm script that runs the collection with Newman
- `postman/run-collection.sh` — convenience script to install deps and run the collection
- `.github/workflows/postman.yml` — optional GitHub Action to run the collection in CI

Prerequisites
- Node.js and npm (or use the GitHub Action to run in CI)
- Application running at the configured `baseUrl` in the Postman environment (default: http://localhost:8080)

Run locally
1. Install dependencies (will install newman):

```bash
npm ci
```

2. Run the collection using the npm script:

```bash
npm run postman
# or
./postman/run-collection.sh
```

If your app requires Basic Auth for the OpenAPI endpoints (configured via `swagger.security.enabled=true`), the environment includes `swaggerUsername` and `swaggerPassword` with the defaults `swagger`/`swagger`. Change them locally if you have different values.

Run in CI
- The included GitHub Actions workflow `.github/workflows/postman.yml` installs Node.js and runs `npm run postman`. It can be triggered manually via `workflow_dispatch` or on pushes to Postman files.

Security note
- The environment file contains plaintext credentials for convenience in local development. Do not store production credentials here.
