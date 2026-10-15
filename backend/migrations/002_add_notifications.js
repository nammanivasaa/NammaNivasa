export const shorthands = undefined;

export const up = (pgm) => {
  pgm.createTable('notifications', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    recipient_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    channel: { type: 'varchar(20)', notNull: true, default: 'IN_APP' },
    subject: { type: 'varchar(255)', notNull: true },
    message: { type: 'text', notNull: true },
    payload: { type: 'jsonb' },
    is_read: { type: 'boolean', notNull: true, default: false },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });

  pgm.addConstraint('notifications', 'notifications_channel_valid', "CHECK (channel IN ('IN_APP', 'EMAIL', 'PUSH'))");
  pgm.createIndex('notifications', ['recipient_user_id', 'is_read', 'created_at'], { name: 'idx_notifications_user_read_time' });
  pgm.createIndex('notifications', 'community_id', { name: 'idx_notifications_community' });
};

export const down = (pgm) => {
  pgm.dropTable('notifications');
};
