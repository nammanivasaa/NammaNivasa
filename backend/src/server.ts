import { app } from './app.js';
import { closeDatabase } from './config/db.js';
import { env } from './config/env.js';

const server = app.listen(env.PORT, () => {
  // eslint-disable-next-line no-console
  console.log(`Namma Nivasa backend listening on port ${env.PORT}`);
});

const shutdown = async () => {
  server.close(async () => {
    try {
      await closeDatabase();
    } finally {
      process.exit(0);
    }
  });
};

process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
