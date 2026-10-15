import { describe, expect, it } from 'vitest';

describe.runIf(Boolean(process.env.TEST_DATABASE_URL))('database integration', () => {
  it('connects to the isolated PostgreSQL test database', async () => {
    const { query, closeDatabase } = await import('../config/db.js');
    const result = await query('SELECT 1 AS ok');

    expect(result.rows[0].ok).toBe(1);

    await closeDatabase();
  });
});
