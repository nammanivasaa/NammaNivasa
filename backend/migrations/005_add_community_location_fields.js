export const shorthands = undefined;

export const up = (pgm) => {
  pgm.addColumns('communities', {
    community_type: { type: 'varchar(100)' },
    state: { type: 'varchar(100)' },
    city: { type: 'varchar(100)' },
    area: { type: 'varchar(100)' },
    pin_code: { type: 'varchar(20)' },
    street_address: { type: 'text' }
  });
};

export const down = (pgm) => {
  pgm.dropColumns('communities', [
    'community_type',
    'state',
    'city',
    'area',
    'pin_code',
    'street_address'
  ]);
};
