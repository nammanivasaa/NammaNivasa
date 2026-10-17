export const shorthands = undefined;

export const up = (pgm) => {
  pgm.createTable('polls', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    community_id: { type: 'uuid', notNull: true, references: 'communities(id)', onDelete: 'RESTRICT' },
    created_by_user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    title: { type: 'varchar(255)', notNull: true },
    description: { type: 'text', notNull: true },
    status: { type: 'varchar(20)', notNull: true, default: 'ACTIVE' },
    expires_at: { type: 'timestamptz' },
    created_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') },
    updated_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });

  pgm.addConstraint('polls', 'polls_status_valid', "CHECK (status IN ('ACTIVE', 'CLOSED', 'ARCHIVED'))");
  pgm.createIndex('polls', ['community_id', 'status'], { name: 'idx_polls_community_status' });

  pgm.createTable('poll_options', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    poll_id: { type: 'uuid', notNull: true, references: 'polls(id)', onDelete: 'CASCADE' },
    option_text: { type: 'varchar(255)', notNull: true },
    vote_count: { type: 'integer', notNull: true, default: 0 }
  });
  pgm.createIndex('poll_options', 'poll_id', { name: 'idx_poll_options_poll_id' });

  pgm.createTable('poll_votes', {
    id: { type: 'uuid', primaryKey: true, default: pgm.func('gen_random_uuid()') },
    poll_id: { type: 'uuid', notNull: true, references: 'polls(id)', onDelete: 'CASCADE' },
    option_id: { type: 'uuid', notNull: true, references: 'poll_options(id)', onDelete: 'CASCADE' },
    user_id: { type: 'uuid', notNull: true, references: 'users(id)', onDelete: 'RESTRICT' },
    voted_at: { type: 'timestamptz', notNull: true, default: pgm.func('now()') }
  });

  pgm.addConstraint('poll_votes', 'poll_votes_user_poll_unique', 'UNIQUE (poll_id, user_id)');
  pgm.createIndex('poll_votes', ['poll_id', 'user_id'], { name: 'idx_poll_votes_poll_user' });
};

export const down = (pgm) => {
  pgm.dropTable('poll_votes');
  pgm.dropTable('poll_options');
  pgm.dropTable('polls');
};
