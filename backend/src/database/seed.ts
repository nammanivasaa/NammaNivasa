import { query, closeDatabase } from '../config/db.js';

const COMMUNITY_ID = '22222222-2222-4222-8222-222222222222';
const BUILDING_ID = 'b1111111-1111-4111-8111-111111111111';

const TEST_USERS = [
  { id: '11111111-1111-4111-8111-111111111111', firebaseUid: 'firebase-admin-01', name: 'Aarav Sharma (Admin)', email: 'admin@nammanivasa.com', role: 'COMMUNITY_ADMINISTRATOR' },
  { id: '22222222-2222-4222-8222-222222222222', firebaseUid: 'firebase-treasurer-01', name: 'Priya Patel (Treasurer)', email: 'treasurer@nammanivasa.com', role: 'TREASURER_FINANCE_MANAGER' },
  { id: '33333333-3333-4333-8333-333333333333', firebaseUid: 'firebase-owner-01', name: 'Rajesh Kumar (Owner)', email: 'owner@nammanivasa.com', role: 'OWNER' },
  { id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', firebaseUid: 'firebase-tenant-01', name: 'Suresh Iyer (Tenant)', email: 'tenant@nammanivasa.com', role: 'TENANT' },
  { id: '55555555-5555-4555-8555-555555555555', firebaseUid: 'firebase-guard-01', name: 'Ramesh Singh (Guard)', email: 'guard@nammanivasa.com', role: 'SECURITY_GUARD' }
];

async function seed() {
  console.log('🌱 Starting Namma Nivasa database seeding...');

  try {
    // 1. Insert Community
    await query(
      `INSERT INTO communities (id, name, slug, status, created_at, updated_at)
       VALUES ($1, $2, $3, 'ACTIVE', NOW(), NOW())
       ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name`,
      [
        COMMUNITY_ID,
        'Green Valley Apartments',
        'green-valley-560102'
      ]
    );
    console.log('✓ Community seeded: Green Valley Apartments');

    // 2. Insert Building
    await query(
      `INSERT INTO buildings (id, community_id, name, block_code, floors, status)
       VALUES ($1, $2, $3, $4, $5, 'ACTIVE')
       ON CONFLICT (id) DO NOTHING`,
      [BUILDING_ID, COMMUNITY_ID, 'Tower A', 'A', 10]
    );
    console.log('✓ Building seeded: Tower A');

    // 3. Insert Flats (A-101 to A-105)
    const flats = [
      { id: 'f1011111-1111-4111-8111-111111111111', number: 'A-101' },
      { id: 'f1022222-2222-4222-8222-222222222222', number: 'A-102' },
      { id: 'f1033333-3333-4333-8333-333333333333', number: 'A-103' },
      { id: 'f1044444-4444-4444-8444-444444444444', number: 'A-104' },
      { id: 'f1055555-5555-4555-8555-555555555555', number: 'A-105' }
    ];

    for (const flat of flats) {
      await query(
        `INSERT INTO flats (id, community_id, building_id, flat_number, unit_type, status)
         VALUES ($1, $2, $3, $4, 'APARTMENT', 'ACTIVE')
         ON CONFLICT (id) DO NOTHING`,
        [flat.id, COMMUNITY_ID, BUILDING_ID, flat.number]
      );
    }
    console.log('✓ Flats seeded: A-101 through A-105');

    // 4. Insert Core Roles
    const rolesMap: Record<string, string> = {};
    const roleCodes = [
      'COMMUNITY_ADMINISTRATOR',
      'TREASURER_FINANCE_MANAGER',
      'OWNER',
      'TENANT',
      'SECURITY_GUARD'
    ];

    for (const code of roleCodes) {
      const roleIdResult = await query<{ id: string }>(
        `INSERT INTO roles (id, community_id, code, label, description)
         VALUES (gen_random_uuid(), $1, $2, $3, $4)
         ON CONFLICT (community_id, code) DO UPDATE SET label = EXCLUDED.label
         RETURNING id`,
        [COMMUNITY_ID, code, code.replace(/_/g, ' '), `System role for ${code}`]
      );
      rolesMap[code] = roleIdResult.rows[0].id;
    }
    console.log('✓ Core roles seeded: Admin, Treasurer, Owner, Tenant, Guard');

    // 5. Insert Test Users & Memberships & Assignments
    for (const user of TEST_USERS) {
      await query(
        `INSERT INTO users (id, firebase_uid, full_name, email, status)
         VALUES ($1, $2, $3, $4, 'ACTIVE')
         ON CONFLICT (id) DO UPDATE SET full_name = EXCLUDED.full_name`,
        [user.id, user.firebaseUid, user.name, user.email]
      );

      await query(
        `INSERT INTO community_memberships (id, user_id, community_id, membership_status)
         VALUES (gen_random_uuid(), $1, $2, 'ACTIVE')
         ON CONFLICT (user_id, community_id) DO UPDATE SET membership_status = 'ACTIVE'`,
        [user.id, COMMUNITY_ID]
      );

      const roleId = rolesMap[user.role];
      if (roleId) {
        await query(
          `INSERT INTO community_role_assignments (id, user_id, community_id, role_id, status)
           VALUES (gen_random_uuid(), $1, $2, $3, 'ACTIVE')
           ON CONFLICT DO NOTHING`,
          [user.id, COMMUNITY_ID, roleId]
        );
      }
    }
    console.log('✓ Test users, memberships, and role assignments seeded successfully!');
    console.log('\n🎉 Database seeding completed successfully.');
  } catch (error) {
    console.error('❌ Database seeding failed:', error);
    process.exit(1);
  } finally {
    await closeDatabase();
  }
}

seed();
