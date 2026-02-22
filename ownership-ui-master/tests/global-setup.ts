import http from 'http';

const baseURL = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:3000';

function checkUrl(url: string): Promise<void> {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const req = http.request(
      {
        hostname: u.hostname,
        port: u.port || 80,
        path: u.pathname || '/',
        method: 'GET',
        timeout: 5000,
      },
      (res) => {
        res.on('data', () => {});
        res.on('end', () => resolve());
      }
    );
    req.on('error', (err: NodeJS.ErrnoException) => {
      if (err.code === 'ECONNREFUSED') {
        reject(new Error('CONNECTION_REFUSED'));
      } else {
        reject(err);
      }
    });
    req.on('timeout', () => {
      req.destroy();
      reject(new Error('TIMEOUT'));
    });
    req.end();
  });
}

export default async function globalSetup() {
  try {
    await checkUrl(baseURL);
  } catch (err) {
    const message =
      err instanceof Error && err.message === 'CONNECTION_REFUSED'
        ? `
╔══════════════════════════════════════════════════════════════════╗
║  UI is not running at ${baseURL.padEnd(42)}║
║  Start the app before running e2e tests.                         ║
╠══════════════════════════════════════════════════════════════════╣
║  From repo root (All In One Source):                             ║
║    ./start-api-frontend.sh                                       ║
║  Then in another terminal:                                       ║
║    cd ownership-ui-master && npm run test:e2e                    ║
║                                                                  ║
║  Or start only the UI:                                           ║
║    cd ownership-ui-master && npm run dev                         ║
║  (API must be on http://localhost:8081 for login tests)          ║
╚══════════════════════════════════════════════════════════════════╝
`
        : `Failed to reach ${baseURL}: ${err}`;
    console.error(message);
    process.exit(1);
  }
}
