import { Client } from 'pg';
import request from 'supertest';
import { beforeAll, describe, expect, it } from 'vitest';

process.env.AUTH_MODE = 'test';

const TEST_USER_ID = '11111111-1111-4111-8111-111111111111';
const TEST_TENANT_ID = 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa';
const TEST_COMMUNITY_ID = '22222222-2222-4222-8222-222222222222';
const UNAUTHORIZED_COMMUNITY_ID = '44444444-4444-4444-8444-444444444444';

const { app } = await import('../app.js');

async function ensureTestDatabaseFixtures(): Promise<void> {
  const databaseUrl = process.env.TEST_DATABASE_URL ?? process.env.DATABASE_URL;
  if (!databaseUrl) {
    return;
  }

  const client = new Client({ connectionString: databaseUrl });

  try {
    await client.connect();

    await client.query('DELETE FROM notices WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM maintenance_requests WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM work_assignments WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM visitor_requests WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM payment_verification_events WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM payment_records WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM bills WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM audit_logs WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM file_refs WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM community_role_assignments WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM roles WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM community_memberships WHERE community_id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM communities WHERE id = $1', [TEST_COMMUNITY_ID]);
    await client.query('DELETE FROM users WHERE id IN ($1, $2)', [TEST_USER_ID, TEST_TENANT_ID]);

    await client.query(
      `INSERT INTO users (id, firebase_uid, full_name, email, phone, status)
       VALUES ($1, $2, $3, $4, $5, 'ACTIVE')
       ON CONFLICT (id) DO UPDATE SET
        firebase_uid = EXCLUDED.firebase_uid,
        full_name = EXCLUDED.full_name,
        email = EXCLUDED.email,
        phone = EXCLUDED.phone,
        status = EXCLUDED.status`,
      [TEST_USER_ID, 'firebase-user-123', 'Test Resident', 'resident@example.com', '+91-90000-00000']
    );

    await client.query(
      `INSERT INTO users (id, firebase_uid, full_name, email, phone, status)
       VALUES ($1, $2, $3, $4, $5, 'ACTIVE')
       ON CONFLICT (id) DO UPDATE SET
        firebase_uid = EXCLUDED.firebase_uid,
        full_name = EXCLUDED.full_name,
        email = EXCLUDED.email,
        phone = EXCLUDED.phone,
        status = EXCLUDED.status`,
      [TEST_TENANT_ID, 'firebase-tenant-123', 'Tenant Resident', 'tenant@example.com', '+91-90000-00001']
    );

    await client.query(
      `INSERT INTO communities (id, name, slug, status)
       VALUES ($1, $2, $3, 'ACTIVE')
       ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        slug = EXCLUDED.slug,
        status = EXCLUDED.status`,
      [TEST_COMMUNITY_ID, 'Test Community', 'test-community']
    );

    await client.query(
      `INSERT INTO roles (id, community_id, code, label, description, is_customizable)
       VALUES (gen_random_uuid(), $1, 'COMMUNITY_ADMINISTRATOR', 'Community Administrator', 'Manage notices', false),
             (gen_random_uuid(), $1, 'TENANT', 'Tenant', 'Basic resident access', false)
       ON CONFLICT (community_id, code) DO NOTHING`,
      [TEST_COMMUNITY_ID]
    );

    const adminRoleResult = await client.query(
      `SELECT id FROM roles WHERE community_id = $1 AND code = 'COMMUNITY_ADMINISTRATOR' LIMIT 1`,
      [TEST_COMMUNITY_ID]
    );
    const tenantRoleResult = await client.query(
      `SELECT id FROM roles WHERE community_id = $1 AND code = 'TENANT' LIMIT 1`,
      [TEST_COMMUNITY_ID]
    );

    if (adminRoleResult.rows[0]) {
      await client.query(
       `INSERT INTO community_role_assignments (id, user_id, community_id, role_id, status, assigned_at)
        VALUES (gen_random_uuid(), $1, $2, $3, 'ACTIVE', NOW())`,
       [TEST_USER_ID, TEST_COMMUNITY_ID, adminRoleResult.rows[0].id]
      );
    }

    if (tenantRoleResult.rows[0]) {
      await client.query(
       `INSERT INTO community_role_assignments (id, user_id, community_id, role_id, status, assigned_at)
        VALUES (gen_random_uuid(), $1, $2, $3, 'ACTIVE', NOW())`,
       [TEST_USER_ID, TEST_COMMUNITY_ID, tenantRoleResult.rows[0].id]
      );
      await client.query(
       `INSERT INTO community_role_assignments (id, user_id, community_id, role_id, status, assigned_at)
        VALUES (gen_random_uuid(), $1, $2, $3, 'ACTIVE', NOW())`,
       [TEST_TENANT_ID, TEST_COMMUNITY_ID, tenantRoleResult.rows[0].id]
      );
    }

    await client.query(
      `INSERT INTO community_memberships (id, user_id, community_id, membership_status, joined_at, status_updated_at)
       VALUES (gen_random_uuid(), $1, $2, 'ACTIVE', NOW(), NOW())
       ON CONFLICT (user_id, community_id) DO UPDATE SET
        membership_status = EXCLUDED.membership_status,
        status_updated_at = NOW()`,
      [TEST_USER_ID, TEST_COMMUNITY_ID]
    );

    await client.query(
      `INSERT INTO community_memberships (id, user_id, community_id, membership_status, joined_at, status_updated_at)
       VALUES (gen_random_uuid(), $1, $2, 'ACTIVE', NOW(), NOW())
       ON CONFLICT (user_id, community_id) DO UPDATE SET
        membership_status = EXCLUDED.membership_status,
        status_updated_at = NOW()`,
      [TEST_TENANT_ID, TEST_COMMUNITY_ID]
    );
  } finally {
    await client.end();
  }
}

beforeAll(async () => {
  await ensureTestDatabaseFixtures();
});

describe('Namma Nivasa backend', () => {
  it('returns health status', async () => {
    const response = await request(app).get('/health');
    expect(response.status).toBe(200);
    expect(response.body.ok).toBe(true);
  });

  it('requires authentication for the user endpoint', async () => {
    const response = await request(app).get('/api/v1/me');
    expect(response.status).toBe(401);
    expect(response.body.error.code).toBe('UNAUTHENTICATED');
  });

  it('allows test-authenticated users to read profile data', async () => {
    const response = await request(app)
      .get('/api/v1/me')
      .set('x-test-user', TEST_USER_ID);

    expect(response.status).toBe(200);
    expect(response.body.data.id).toBe(TEST_USER_ID);
  });

  it('lists notices for an active community member', async () => {
    const response = await request(app)
      .get(`/api/v1/communities/${TEST_COMMUNITY_ID}/notices`)
      .set('x-test-user', TEST_USER_ID);

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body.data)).toBe(true);
  });

  it('creates a draft notice when the user has management permission', async () => {
    const response = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/notices`)
      .set('x-test-user', TEST_USER_ID)
      .send({ title: 'Water outage alert', message: 'Supply is being restored shortly.', published: false });

    expect(response.status).toBe(201);
    expect(response.body.data.status).toBe('DRAFT');
  });

  it('rejects notice creation for users without notice-management permission', async () => {
    const response = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/notices`)
      .set('x-test-user', TEST_TENANT_ID)
      .send({ title: 'Bad notice', message: 'No permission for this', published: false });

    expect(response.status).toBe(403);
    expect(response.body.error.code).toBe('UNAUTHORIZED');
  });

  it('rejects publishing notices without PUBLISH_NOTICE permission', async () => {
    const existingNotice = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/notices`)
      .set('x-test-user', TEST_USER_ID)
      .send({ title: 'Draft notice', message: 'Awaiting publish', published: false });

    const response = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/notices/${existingNotice.body.data.id}/publish`)
      .set('x-test-user', TEST_TENANT_ID);

    expect(response.status).toBe(403);
    expect(response.body.error.code).toBe('UNAUTHORIZED');
  });

  it('creates a maintenance request for an active community member', async () => {
    const response = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/maintenance-requests`)
      .set('x-test-user', TEST_USER_ID)
      .send({ title: 'Leak under sink', description: 'Water keeps dripping on the cabinet', category: 'PLUMBING', priority: 'HIGH' });

    expect(response.status).toBe(201);
    expect(response.body.data.status).toBe('OPEN');
  });

  it('lists only the authenticated user’s maintenance requests unless a management role is present', async () => {
    const response = await request(app)
      .get(`/api/v1/communities/${TEST_COMMUNITY_ID}/maintenance-requests`)
      .set('x-test-user', TEST_USER_ID);

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body.data)).toBe(true);
    expect(response.body.data.every((request: { createdByUserId: string }) => request.createdByUserId === TEST_USER_ID)).toBe(true);
  });

  it('rejects invalid maintenance payloads', async () => {
    const response = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/maintenance-requests`)
      .set('x-test-user', TEST_USER_ID)
      .send({ title: '', description: 'bad request', category: '' });

    expect(response.status).toBe(400);
    expect(response.body.error.code).toBe('INVALID_INPUT');
  });

  it('denies visitor approval when the user lacks the permission', async () => {
    const response = await request(app)
      .post(`/api/v1/communities/${UNAUTHORIZED_COMMUNITY_ID}/visitors/v-1/approve`)
      .set('x-test-user', TEST_USER_ID);

    expect(response.status).toBe(403);
    expect(response.body.error.code).toBe('UNAUTHORIZED');
  });

  it('allows an owner to approve a visitor in the active community', async () => {
    const response = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/visitors/v-1/approve`)
      .set('x-test-user', TEST_USER_ID);

    expect(response.status).toBe(200);
    expect(response.body.data.status).toBe('APPROVED');
  });

  it('prevents cross-community access by rejecting a mismatched community membership', async () => {
    const response = await request(app)
      .get(`/api/v1/communities/${UNAUTHORIZED_COMMUNITY_ID}/notices`)
      .set('x-test-user', TEST_USER_ID);

    expect(response.status).toBe(403);
    expect(response.body.error.code).toBe('UNAUTHORIZED');
  });
});

describe('financial backend', () => {
  it('creates a bill and verifies a payment for the active community', async () => {
    const dueAt = new Date(Date.now() + 60 * 60 * 24 * 30 * 1000).toISOString();
    const billResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/bills`)
      .set('x-test-user', TEST_USER_ID)
      .send({
        billType: 'Maintenance',
        amount: '2500.00',
        currency: 'INR',
        dueAt,
        periodStart: new Date(Date.now() - 60 * 60 * 24 * 7 * 1000).toISOString(),
        periodEnd: new Date(Date.now() + 60 * 60 * 24 * 7 * 1000).toISOString(),
        flatId: null
      });

    expect(billResponse.status).toBe(201);

    const paymentResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments`)
      .set('x-test-user', TEST_USER_ID)
      .send({
        billId: billResponse.body.data.id,
        payerUserId: TEST_USER_ID,
        amount: '2500.00',
        currency: 'INR',
        paymentMethod: 'UPI',
        visibility: 'PRIVATE_TO_OWNER'
      });

    expect(paymentResponse.status).toBe(201);
    expect(paymentResponse.body.data.status).toBe('PENDING');

    const verifyResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments/${paymentResponse.body.data.id}/verify`)
      .set('x-test-user', TEST_USER_ID)
      .send({ reason: 'Payment received in account.' });

    expect(verifyResponse.status).toBe(200);
    expect(verifyResponse.body.data.status).toBe('PAID');

    const reverseResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments/${paymentResponse.body.data.id}/reverse`)
      .set('x-test-user', TEST_USER_ID)
      .send({ reason: 'Manual correction.' });

    expect(reverseResponse.status).toBe(200);
    expect(reverseResponse.body.data.status).toBe('REVERSED');
  });

  it('prevents a tenant from viewing another resident’s private payment without authorization', async () => {
    const dueAt = new Date(Date.now() + 60 * 60 * 24 * 20 * 1000).toISOString();
    const billResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/bills`)
      .set('x-test-user', TEST_USER_ID)
      .send({
        billType: 'Sinking Fund',
        amount: '1500.00',
        currency: 'INR',
        dueAt,
        periodStart: new Date(Date.now() - 60 * 60 * 24 * 7 * 1000).toISOString(),
        periodEnd: new Date(Date.now() + 60 * 60 * 24 * 7 * 1000).toISOString()
      });

    const paymentResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments`)
      .set('x-test-user', TEST_USER_ID)
      .send({
        billId: billResponse.body.data.id,
        payerUserId: TEST_USER_ID,
        amount: '1500.00',
        currency: 'INR',
        paymentMethod: 'UPI',
        visibility: 'PRIVATE_TO_OWNER'
      });

    const response = await request(app)
      .get(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments/${paymentResponse.body.data.id}`)
      .set('x-test-user', TEST_TENANT_ID);

    expect(response.status).toBe(403);
    expect(response.body.error.code).toBe('UNAUTHORIZED');
  });

  it('allows explicit household access grants and revokes them after the grant is removed', async () => {
    const dueAt = new Date(Date.now() + 60 * 60 * 24 * 40 * 1000).toISOString();
    const billResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/bills`)
      .set('x-test-user', TEST_USER_ID)
      .send({
        billType: 'Maintenance',
        amount: '900.00',
        currency: 'INR',
        dueAt,
        periodStart: new Date(Date.now() - 60 * 60 * 24 * 14 * 1000).toISOString(),
        periodEnd: new Date(Date.now() + 60 * 60 * 24 * 14 * 1000).toISOString()
      });

    const paymentResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments`)
      .set('x-test-user', TEST_USER_ID)
      .send({
        billId: billResponse.body.data.id,
        payerUserId: TEST_USER_ID,
        amount: '900.00',
        currency: 'INR',
        paymentMethod: 'UPI',
        visibility: 'AUTHORIZED_HOUSEHOLD_ONLY'
      });

    const beforeGrant = await request(app)
      .get(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments/${paymentResponse.body.data.id}`)
      .set('x-test-user', TEST_TENANT_ID);
    expect(beforeGrant.status).toBe(403);

    const grantResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/household-financial-access`)
      .set('x-test-user', TEST_USER_ID)
      .send({
        ownerUserId: TEST_USER_ID,
        granteeUserId: TEST_TENANT_ID,
        reason: 'Authorized household access',
        notes: 'Temporary access for the tenant.'
      });

    expect(grantResponse.status).toBe(201);

    const afterGrant = await request(app)
      .get(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments/${paymentResponse.body.data.id}`)
      .set('x-test-user', TEST_TENANT_ID);

    expect(afterGrant.status).toBe(200);

    const revokeResponse = await request(app)
      .post(`/api/v1/communities/${TEST_COMMUNITY_ID}/household-financial-access/${grantResponse.body.data.id}/revoke`)
      .set('x-test-user', TEST_USER_ID);

    expect(revokeResponse.status).toBe(200);

    const afterRevoke = await request(app)
      .get(`/api/v1/communities/${TEST_COMMUNITY_ID}/payments/${paymentResponse.body.data.id}`)
      .set('x-test-user', TEST_TENANT_ID);

    expect(afterRevoke.status).toBe(403);
  });
});
