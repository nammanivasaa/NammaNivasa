export const shorthands = undefined;

export const up = (pgm) => {
  pgm.sql('CREATE EXTENSION IF NOT EXISTS pgcrypto;');

  pgm.createTable('users', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    firebase_uid: { type: 'varchar(255)', notNull: true, unique: true },
    full_name: { type: 'varchar(255)', notNull: true },
    email: { type: 'varchar(255)' },
    phone: { type: 'varchar(50)' },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.addConstraint('users', 'users_status_valid', "CHECK (status IN ('ACTIVE', 'INVITED', 'SUSPENDED', 'DELETED'))");
  pgm.addConstraint('users', 'users_firebase_uid_not_blank', "CHECK (firebase_uid <> '')");
  pgm.addConstraint('users', 'users_full_name_not_blank', "CHECK (full_name <> '')");
  pgm.createIndex('users', 'firebase_uid', { name: 'idx_users_firebase_uid' });
  pgm.createIndex('users', 'email', { name: 'idx_users_email' });

  pgm.createTable('communities', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    name: { type: 'varchar(255)', notNull: true },
    slug: { type: 'varchar(100)', notNull: true, unique: true },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.addConstraint('communities', 'communities_status_valid', "CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))");
  pgm.addConstraint('communities', 'communities_slug_not_blank', "CHECK (slug <> '')");
  pgm.createIndex('communities', 'slug', { name: 'idx_communities_slug' });

  pgm.createTable('buildings', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    name: { type: 'varchar(255)', notNull: true },
    block_code: { type: 'varchar(50)', notNull: true },
    floors: { type: 'integer', notNull: true },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' }
  });
  pgm.addConstraint('buildings', 'buildings_status_valid', "CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))");
  pgm.addConstraint('buildings', 'buildings_floors_positive', 'CHECK (floors > 0)');
  pgm.addConstraint('buildings', 'buildings_community_block_name_unique', 'UNIQUE (community_id, block_code, name)');
  pgm.createIndex('buildings', 'community_id', { name: 'idx_buildings_community_id' });

  pgm.createTable('flats', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    building_id: { type: 'uuid', notNull: true, references: 'buildings(id)', onDelete: 'RESTRICT' },
    flat_number: { type: 'varchar(100)', notNull: true },
    unit_type: { type: 'varchar(100)', notNull: true },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' },
    area_sq_ft: { type: 'numeric(10,2)' }
  });
  pgm.addConstraint('flats', 'flats_status_valid', "CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))");
  pgm.addConstraint('flats', 'flats_area_positive', 'CHECK (area_sq_ft IS NULL OR area_sq_ft > 0)');
  pgm.addConstraint('flats', 'flats_community_building_number_unique', 'UNIQUE (community_id, building_id, flat_number)');
  pgm.createIndex('flats', 'community_id', { name: 'idx_flats_community_id' });
  pgm.createIndex('flats', 'building_id', { name: 'idx_flats_building_id' });

  pgm.createTable('community_memberships', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    membership_status: { type: 'varchar(20)', notNull: true, default: 'PENDING' },
    approved_by_user_id: { type: 'uuid', references: 'users(id)' },
    joined_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    ended_at: { type: 'timestamptz' },
    status_updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    status_reason: { type: 'text' }
  });
  pgm.addConstraint('community_memberships', 'community_memberships_status_valid', "CHECK (membership_status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'REVOKED', 'EXPIRED'))");
  pgm.addConstraint('community_memberships', 'community_memberships_ended_after_joined', 'CHECK (ended_at IS NULL OR ended_at >= joined_at)');
  pgm.addConstraint('community_memberships', 'community_memberships_status_updated_after_joined', 'CHECK (status_updated_at >= joined_at)');
  pgm.addConstraint('community_memberships', 'community_memberships_revoked_requires_reason', "CHECK (membership_status <> 'REVOKED' OR ended_at IS NOT NULL OR status_reason IS NOT NULL)");
  pgm.addConstraint('community_memberships', 'community_memberships_user_community_unique', 'UNIQUE (user_id, community_id)');
  pgm.createIndex('community_memberships', ['user_id', 'community_id'], { name: 'idx_memberships_user_community' });
  pgm.createIndex('community_memberships', ['community_id', 'membership_status'], { name: 'idx_memberships_community_status' });

  pgm.createTable('roles', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    code: { type: 'varchar(60)', notNull: true },
    label: { type: 'varchar(120)', notNull: true },
    description: { type: 'text', notNull: true },
    is_customizable: { type: 'boolean', notNull: true, default: false },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.addConstraint('roles', 'roles_code_valid', "CHECK (code IN ('OWNER', 'TENANT', 'AUTHORIZED_HOUSEHOLD_MEMBER', 'COMMUNITY_ADMINISTRATOR', 'COMMITTEE_MEMBER', 'TREASURER_FINANCE_MANAGER', 'MAINTENANCE_MANAGER', 'SECURITY_GUARD', 'FACILITY_MANAGER'))");
  pgm.addConstraint('roles', 'roles_community_code_unique', 'UNIQUE (community_id, code)');

  pgm.createTable('role_permissions', {
    role_id: { type: 'uuid', notNull: true, references: 'roles(id)', onDelete: 'CASCADE' },
    permission_code: { type: 'varchar(80)', notNull: true },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.addConstraint('role_permissions', 'role_permissions_pkey', 'PRIMARY KEY (role_id, permission_code)');

  pgm.createTable('community_role_assignments', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    role_id: { type: 'uuid', notNull: true, references: 'roles(id)', onDelete: 'RESTRICT' },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' },
    assigned_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    revoked_at: { type: 'timestamptz' }
  });
  pgm.addConstraint('community_role_assignments', 'community_role_assignments_status_valid', "CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))");
  pgm.addConstraint('community_role_assignments', 'community_role_assignments_revoked_after_assigned', 'CHECK (revoked_at IS NULL OR revoked_at >= assigned_at)');
  pgm.createIndex('community_role_assignments', ['user_id', 'community_id'], { name: 'idx_role_assignments_user_community' });
  pgm.createIndex('community_role_assignments', 'role_id', { name: 'idx_role_assignments_role_id' });

  pgm.createTable('property_ownership', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    flat_id: { type: 'uuid', notNull: true, references: 'flats(id)', onDelete: 'RESTRICT' },
    user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    ownership_type: { type: 'varchar(25)', notNull: true },
    ownership_share: { type: 'numeric(5,4)', notNull: true },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' },
    start_date: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    end_date: { type: 'timestamptz' }
  });
  pgm.addConstraint('property_ownership', 'property_ownership_type_valid', "CHECK (ownership_type IN ('OWNER', 'CO_OWNER', 'TRUST', 'LEGAL_ENTITY'))");
  pgm.addConstraint('property_ownership', 'property_ownership_share_valid', 'CHECK (ownership_share > 0 AND ownership_share <= 1)');
  pgm.addConstraint('property_ownership', 'property_ownership_status_valid', "CHECK (status IN ('ACTIVE', 'INACTIVE', 'TRANSFERRED', 'REVOKED'))");
  pgm.addConstraint('property_ownership', 'property_ownership_end_after_start', 'CHECK (end_date IS NULL OR end_date >= start_date)');
  pgm.createIndex('property_ownership', ['community_id', 'flat_id'], { name: 'idx_property_ownership_flat' });
  pgm.createIndex('property_ownership', ['user_id', 'community_id'], { name: 'idx_property_ownership_user' });

  pgm.createTable('occupancy', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    flat_id: { type: 'uuid', notNull: true, references: 'flats(id)', onDelete: 'RESTRICT' },
    user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    occupancy_type: { type: 'varchar(25)', notNull: true },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' },
    start_date: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    end_date: { type: 'timestamptz' }
  });
  pgm.addConstraint('occupancy', 'occupancy_type_valid', "CHECK (occupancy_type IN ('OWNER', 'TENANT', 'FAMILY_MEMBER', 'STAFF'))");
  pgm.addConstraint('occupancy', 'occupancy_status_valid', "CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'EXPIRED'))");
  pgm.addConstraint('occupancy', 'occupancy_end_after_start', 'CHECK (end_date IS NULL OR end_date >= start_date)');
  pgm.createIndex('occupancy', ['community_id', 'flat_id'], { name: 'idx_occupancy_flat' });
  pgm.createIndex('occupancy', ['user_id', 'community_id'], { name: 'idx_occupancy_user' });

  pgm.createTable('notices', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    created_by_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    title: { type: 'varchar(255)', notNull: true },
    message: { type: 'text', notNull: true },
    status: { type: 'varchar(20)', notNull: true, default: 'DRAFT' },
    starts_at: { type: 'timestamptz' },
    ends_at: { type: 'timestamptz' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.addConstraint('notices', 'notices_status_valid', "CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))");
  pgm.addConstraint('notices', 'notices_end_after_start', 'CHECK (ends_at IS NULL OR starts_at IS NULL OR ends_at >= starts_at)');
  pgm.createIndex('notices', ['community_id', 'status'], { name: 'idx_notices_community_status' });

  pgm.createTable('maintenance_requests', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    flat_id: { type: 'uuid', references: 'flats(id)', onDelete: 'RESTRICT' },
    created_by_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    category: { type: 'varchar(80)', notNull: true },
    title: { type: 'varchar(255)', notNull: true },
    description: { type: 'text', notNull: true },
    priority: { type: 'varchar(20)', notNull: true, default: 'MEDIUM' },
    status: { type: 'varchar(25)', notNull: true, default: 'OPEN' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.addConstraint('maintenance_requests', 'maintenance_requests_priority_valid', "CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))");
  pgm.addConstraint('maintenance_requests', 'maintenance_requests_status_valid', "CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'))");
  pgm.createIndex('maintenance_requests', ['community_id', 'status'], { name: 'idx_maintenance_requests_community_status' });
  pgm.createIndex('maintenance_requests', 'created_by_user_id', { name: 'idx_maintenance_requests_created_by' });

  pgm.createTable('work_assignments', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    maintenance_request_id: { type: 'uuid', notNull: true, references: 'maintenance_requests(id)', onDelete: 'CASCADE' },
    assignee_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    status: { type: 'varchar(20)', notNull: true, default: 'ASSIGNED' },
    assigned_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    completed_at: { type: 'timestamptz' }
  });
  pgm.addConstraint('work_assignments', 'work_assignments_status_valid', "CHECK (status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))");
  pgm.addConstraint('work_assignments', 'work_assignments_completed_after_assigned', 'CHECK (completed_at IS NULL OR completed_at >= assigned_at)');
  pgm.createIndex('work_assignments', 'maintenance_request_id', { name: 'idx_work_assignments_request' });
  pgm.createIndex('work_assignments', 'assignee_user_id', { name: 'idx_work_assignments_assignee' });

  pgm.createTable('visitor_requests', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    flat_id: { type: 'uuid', references: 'flats(id)', onDelete: 'RESTRICT' },
    host_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    visitor_name: { type: 'varchar(255)', notNull: true },
    purpose: { type: 'varchar(255)', notNull: true },
    expected_arrival: { type: 'timestamptz', notNull: true },
    status: { type: 'varchar(25)', notNull: true, default: 'PENDING' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    decision_by_user_id: { type: 'uuid', references: 'users(id)' },
    decision_at: { type: 'timestamptz' },
    decision_reason: { type: 'text' }
  });
  pgm.addConstraint('visitor_requests', 'visitor_requests_status_valid', "CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))");
  pgm.addConstraint('visitor_requests', 'visitor_requests_decision_consistency', 'CHECK (decision_by_user_id IS NULL OR decision_at IS NOT NULL)');
  pgm.createIndex('visitor_requests', ['community_id', 'status'], { name: 'idx_visitors_community_status' });

  pgm.createTable('bills', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    flat_id: { type: 'uuid', references: 'flats(id)', onDelete: 'RESTRICT' },
    bill_type: { type: 'varchar(80)', notNull: true },
    amount: { type: 'numeric(18,2)', notNull: true },
    currency: { type: 'char(3)', notNull: true, default: 'INR' },
    period_start: { type: 'timestamptz', notNull: true },
    period_end: { type: 'timestamptz', notNull: true },
    due_at: { type: 'timestamptz', notNull: true },
    status: { type: 'varchar(25)', notNull: true, default: 'OPEN' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.addConstraint('bills', 'bills_amount_positive', 'CHECK (amount > 0)');
  pgm.addConstraint('bills', 'bills_status_valid', "CHECK (status IN ('OPEN', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'VOID'))");
  pgm.addConstraint('bills', 'bills_period_end_after_start', 'CHECK (period_end >= period_start)');
  pgm.addConstraint('bills', 'bills_due_at_after_start', 'CHECK (due_at >= period_start)');
  pgm.createIndex('bills', ['community_id', 'flat_id'], { name: 'idx_bills_community_flat' });
  pgm.createIndex('bills', 'due_at', { name: 'idx_bills_due_at' });

  pgm.createTable('payment_records', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    bill_id: { type: 'uuid', references: 'bills(id)', onDelete: 'RESTRICT' },
    payer_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    flat_id: { type: 'uuid', references: 'flats(id)', onDelete: 'RESTRICT' },
    amount: { type: 'numeric(18,2)', notNull: true },
    currency: { type: 'char(3)', notNull: true, default: 'INR' },
    payment_method: { type: 'varchar(40)' },
    reference: { type: 'varchar(255)' },
    status: { type: 'varchar(20)', notNull: true, default: 'PENDING' },
    visibility: { type: 'varchar(35)', notNull: true, default: 'PRIVATE_TO_OWNER' },
    evidence_ref: { type: 'varchar(255)' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    verified_by_user_id: { type: 'uuid', references: 'users(id)' },
    verified_at: { type: 'timestamptz' },
    rejected_by_user_id: { type: 'uuid', references: 'users(id)' },
    rejected_at: { type: 'timestamptz' },
    reversal_reason: { type: 'text' }
  });
  pgm.addConstraint('payment_records', 'payment_records_amount_positive', 'CHECK (amount > 0)');
  pgm.addConstraint('payment_records', 'payment_records_status_valid', "CHECK (status IN ('PENDING', 'PAID', 'REJECTED', 'PARTIALLY_PAID', 'REVERSED'))");
  pgm.addConstraint('payment_records', 'payment_records_visibility_valid', "CHECK (visibility IN ('PRIVATE_TO_OWNER', 'AUTHORIZED_HOUSEHOLD_ONLY', 'COMMUNITY_SUMMARY_ONLY', 'RESIDENT_VISIBLE', 'ADMIN_ONLY'))");
  pgm.addConstraint('payment_records', 'payment_records_paid_requires_verifier', "CHECK (status <> 'PAID' OR verified_by_user_id IS NOT NULL)");
  pgm.addConstraint('payment_records', 'payment_records_rejected_requires_rejector', "CHECK (status <> 'REJECTED' OR rejected_by_user_id IS NOT NULL)");
  pgm.addConstraint('payment_records', 'payment_records_verified_timestamp_valid', 'CHECK (verified_at IS NULL OR verified_by_user_id IS NOT NULL)');
  pgm.addConstraint('payment_records', 'payment_records_rejected_timestamp_valid', 'CHECK (rejected_at IS NULL OR rejected_by_user_id IS NOT NULL)');
  pgm.createIndex('payment_records', ['payer_user_id', 'community_id'], { name: 'idx_payment_records_payer' });
  pgm.createIndex('payment_records', ['community_id', 'status'], { name: 'idx_payment_records_status' });

  pgm.createTable('payment_verification_events', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    payment_id: { type: 'uuid', notNull: true, references: 'payment_records(id)', onDelete: 'CASCADE' },
    action: { type: 'varchar(30)', notNull: true },
    actor_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    reason: { type: 'text' },
    performed_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    correlation_id: { type: 'uuid' }
  });
  pgm.addConstraint('payment_verification_events', 'payment_verification_events_action_valid', "CHECK (action IN ('VERIFIED', 'REJECTED', 'REVERSED', 'ADJUSTED'))");
  pgm.createIndex('payment_verification_events', ['payment_id', 'performed_at'], { name: 'idx_payment_verification_events_payment' });

  pgm.createTable('audit_logs', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    actor_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    resource_type: { type: 'varchar(80)', notNull: true },
    resource_id: { type: 'varchar(100)', notNull: true },
    action: { type: 'varchar(80)', notNull: true },
    details: { type: 'jsonb' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.createIndex('audit_logs', ['community_id', 'created_at'], { name: 'idx_audit_logs_community_time' });

  pgm.createTable('file_refs', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    owner_user_id: { type: 'uuid', references: 'users(id)', onDelete: 'SET NULL' },
    storage_bucket: { type: 'varchar(255)', notNull: true },
    storage_path: { type: 'text', notNull: true },
    content_type: { type: 'varchar(120)' },
    size_bytes: { type: 'bigint' },
    checksum_sha256: { type: 'varchar(128)' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });
  pgm.addConstraint('file_refs', 'file_refs_storage_path_not_blank', "CHECK (storage_path <> '')");
  pgm.createIndex('file_refs', 'community_id', { name: 'idx_file_refs_community' });
  pgm.createIndex('file_refs', 'owner_user_id', { name: 'idx_file_refs_owner_user' });
};

export const down = (pgm) => {
  pgm.dropTable('file_refs');
  pgm.dropTable('audit_logs');
  pgm.dropTable('payment_verification_events');
  pgm.dropTable('payment_records');
  pgm.dropTable('bills');
  pgm.dropTable('visitor_requests');
  pgm.dropTable('work_assignments');
  pgm.dropTable('maintenance_requests');
  pgm.dropTable('notices');
  pgm.dropTable('occupancy');
  pgm.dropTable('property_ownership');
  pgm.dropTable('community_role_assignments');
  pgm.dropTable('role_permissions');
  pgm.dropTable('roles');
  pgm.dropTable('community_memberships');
  pgm.dropTable('flats');
  pgm.dropTable('buildings');
  pgm.dropTable('communities');
  pgm.dropTable('users');
};
