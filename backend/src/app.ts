import cors from 'cors';
import express from 'express';
import { pingDatabase } from './config/db.js';
import { env } from './config/env.js';
import { errorHandler, notFoundHandler } from './middleware/error.js';
import { apiRouter } from './modules/api.js';

export const app = express();

app.use(cors());
app.use(express.json({ limit: '1mb' }));
app.disable('x-powered-by');

app.get('/health', (_req, res) => {
  res.json({ ok: true, status: 'healthy', environment: env.NODE_ENV });
});

app.get('/ready', async (_req, res) => {
  if (!process.env.DATABASE_URL && !process.env.TEST_DATABASE_URL) {
    res.status(503).json({ ok: false, status: 'database-not-configured' });
    return;
  }

  try {
    await pingDatabase();
    res.json({ ok: true, status: 'ready', environment: env.NODE_ENV });
  } catch {
    res.status(503).json({ ok: false, status: 'database-unavailable' });
  }
});

app.use(apiRouter);
app.use(notFoundHandler);
app.use(errorHandler);
