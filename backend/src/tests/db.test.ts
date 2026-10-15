import { beforeEach, describe, expect, it, vi } from 'vitest';

const { mockQuery, mockConnect, mockRelease, mockEnd } = vi.hoisted(() => ({
  mockQuery: vi.fn(),
  mockConnect: vi.fn(),
  mockRelease: vi.fn(),
  mockEnd: vi.fn()
}));

vi.mock('pg', () => ({
  Pool: vi.fn(() => ({
    query: mockQuery,
    connect: mockConnect,
    end: mockEnd
  }))
}));

describe('database access layer', () => {
  beforeEach(() => {
    vi.resetModules();
    vi.clearAllMocks();
    process.env.NODE_ENV = 'test';
    delete process.env.DATABASE_URL;
    delete process.env.TEST_DATABASE_URL;
  });

  it('executes a successful query through the configured pool', async () => {
    process.env.TEST_DATABASE_URL = 'postgresql://namma_nivasa:change_me@127.0.0.1:5432/namma_nivasa_test';
    mockQuery.mockResolvedValueOnce({ rows: [{ ok: 1 }] });

    const { query } = await import('../config/db.js');
    const result = await query('SELECT 1 AS ok');

    expect(mockQuery).toHaveBeenCalledWith('SELECT 1 AS ok', []);
    expect(result.rows[0].ok).toBe(1);
  });

  it('returns a sanitized error for query failures', async () => {
    process.env.TEST_DATABASE_URL = 'postgresql://namma_nivasa:change_me@127.0.0.1:5432/namma_nivasa_test';
    mockQuery.mockRejectedValueOnce(new Error('SELECT failed with value = $1 and secret=abc'));

    const { query } = await import('../config/db.js');

    await expect(query('SELECT * FROM users WHERE email = $1', ['user@example.com'])).rejects.toThrow('Database operation failed.');
  });

  it('commits a transaction and releases the client', async () => {
    process.env.TEST_DATABASE_URL = 'postgresql://namma_nivasa:change_me@127.0.0.1:5432/namma_nivasa_test';
    const clientQuery = vi.fn();
    clientQuery.mockResolvedValueOnce({});
    clientQuery.mockResolvedValueOnce({});
    mockConnect.mockResolvedValueOnce({ query: clientQuery, release: mockRelease });

    const { withTransaction } = await import('../config/db.js');
    const result = await withTransaction(async (client) => {
      expect(client).toBeDefined();
      return 'committed';
    });

    expect(result).toBe('committed');
    expect(clientQuery).toHaveBeenNthCalledWith(1, 'BEGIN');
    expect(clientQuery).toHaveBeenNthCalledWith(2, 'COMMIT');
    expect(mockRelease).toHaveBeenCalledTimes(1);
  });

  it('rolls back and releases the client after a transaction failure', async () => {
    process.env.TEST_DATABASE_URL = 'postgresql://namma_nivasa:change_me@127.0.0.1:5432/namma_nivasa_test';
    const clientQuery = vi.fn();
    clientQuery.mockResolvedValueOnce({});
    clientQuery.mockResolvedValueOnce({});
    mockConnect.mockResolvedValueOnce({ query: clientQuery, release: mockRelease });

    const { withTransaction } = await import('../config/db.js');

    await expect(withTransaction(async () => {
      throw new Error('boom');
    })).rejects.toThrow('Database operation failed.');

    expect(clientQuery).toHaveBeenNthCalledWith(1, 'BEGIN');
    expect(clientQuery).toHaveBeenNthCalledWith(2, 'ROLLBACK');
    expect(mockRelease).toHaveBeenCalledTimes(1);
  });

  it('requires a database configuration when the pool is initialized', async () => {
    delete process.env.DATABASE_URL;
    delete process.env.TEST_DATABASE_URL;
    process.env.NODE_ENV = 'production';

    const { getPool } = await import('../config/db.js');

    expect(() => getPool()).toThrow('Database configuration is missing for production');
  });

  it('ends the pool when closeDatabase is called', async () => {
    process.env.TEST_DATABASE_URL = 'postgresql://namma_nivasa:change_me@127.0.0.1:5432/namma_nivasa_test';
    mockEnd.mockResolvedValueOnce(undefined);

    const { getPool, closeDatabase } = await import('../config/db.js');
    getPool();
    await closeDatabase();

    expect(mockEnd).toHaveBeenCalledTimes(1);
  });

  it('blocks test authentication in production', async () => {
    process.env.NODE_ENV = 'production';
    process.env.AUTH_MODE = 'test';
    delete process.env.DATABASE_URL;
    delete process.env.TEST_DATABASE_URL;

    await expect(import('../config/env.js')).rejects.toThrow('Production builds cannot enable test authentication');
  });
});
