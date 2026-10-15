import { Pool, type PoolClient, type QueryResult, type QueryResultRow } from 'pg';
import { getDatabaseUrl } from './env.js';

let pool: Pool | null = null;

function createPool(): Pool {
  return new Pool({
    connectionString: getDatabaseUrl(),
    max: 10,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 5000,
    application_name: 'namma-nivasa-backend'
  });
}

function normalizeDatabaseError(error: unknown): Error {
  const sanitized = new Error('Database operation failed.');

  if (error instanceof Error) {
    const message = error.message ?? 'Database operation failed.';
    if (/password|credential|authentication|sasl|connection/i.test(message)) {
      const connectionError = new Error('Database connection failed.');
      Object.defineProperty(connectionError, 'cause', {
        value: error,
        enumerable: false,
        configurable: true,
        writable: true
      });
      return connectionError;
    }

    Object.defineProperty(sanitized, 'cause', {
      value: error,
      enumerable: false,
      configurable: true,
      writable: true
    });
    return sanitized;
  }

  Object.defineProperty(sanitized, 'cause', {
    value: error,
    enumerable: false,
    configurable: true,
    writable: true
  });
  return sanitized;
}

export function getPool(): Pool {
  if (!pool) {
    pool = createPool();
  }

  return pool;
}

export async function query<T extends QueryResultRow = QueryResultRow>(text: string, params: unknown[] = []): Promise<QueryResult<T>> {
  try {
    return await getPool().query<T>(text, params);
  } catch (error) {
    throw normalizeDatabaseError(error);
  }
}

export async function withTransaction<T>(callback: (client: PoolClient) => Promise<T>): Promise<T> {
  const client = await getPool().connect();

  try {
    await client.query('BEGIN');
    const result = await callback(client);
    await client.query('COMMIT');
    return result;
  } catch (error) {
    try {
      await client.query('ROLLBACK');
    } catch {
      // ignore rollback failures after a prior error to preserve the original failure
    }
    throw normalizeDatabaseError(error);
  } finally {
    client.release();
  }
}

export async function pingDatabase(): Promise<void> {
  const client = await getPool().connect();

  try {
    await client.query('SELECT 1 AS ok');
  } finally {
    client.release();
  }
}

export async function closeDatabase(): Promise<void> {
  if (pool) {
    await pool.end();
    pool = null;
  }
}
