export const shorthands = undefined;

export const up = (pgm) => {
  pgm.createTable('household_financial_access', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    owner_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    grantee_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    granted_by_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' },
    reason: { type: 'text' },
    notes: { type: 'text' },
    granted_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    revoked_at: { type: 'timestamptz' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });

  pgm.addConstraint('household_financial_access', 'household_financial_access_status_valid', "CHECK (status IN ('ACTIVE', 'REVOKED'))");
  pgm.addConstraint('household_financial_access', 'household_financial_access_not_self_grant', 'CHECK (owner_user_id <> grantee_user_id)');
  pgm.addConstraint('household_financial_access', 'household_financial_access_revoked_after_granted', 'CHECK (revoked_at IS NULL OR revoked_at >= granted_at)');
  pgm.addConstraint('household_financial_access', 'household_financial_access_revoke_requires_status', "CHECK (status <> 'REVOKED' OR revoked_at IS NOT NULL)");
  pgm.createIndex('household_financial_access', ['community_id', 'owner_user_id'], { name: 'idx_household_financial_access_owner' });
  pgm.createIndex('household_financial_access', ['community_id', 'grantee_user_id'], { name: 'idx_household_financial_access_grantee' });
};

export const down = (pgm) => {
  pgm.dropTable('household_financial_access');
};
