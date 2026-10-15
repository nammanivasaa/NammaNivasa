import { beforeEach, describe, expect, it, vi } from 'vitest';

const { mockQuery } = vi.hoisted(() => ({
  mockQuery: vi.fn()
}));

vi.mock('../config/db.js', () => ({
  query: mockQuery
}));

describe('repository layer', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads an application user by Firebase UID', async () => {
    mockQuery.mockResolvedValueOnce({
      rows: [{ id: 'user-001', firebase_uid: 'firebase-user-001', full_name: 'Asha Nair', email: 'asha@example.com', phone: null, status: 'ACTIVE' }]
    });

    const { findUserByIdentity } = await import('../repositories/userRepository.js');
    const user = await findUserByIdentity(undefined, 'firebase-user-001');

    expect(mockQuery).toHaveBeenCalledWith(
      expect.stringContaining('FROM users'),
      ['firebase-user-001']
    );
    expect(user?.full_name).toBe('Asha Nair');
  });

  it('returns only active memberships for the authenticated user', async () => {
    mockQuery.mockResolvedValueOnce({
      rows: [
        { community_id: 'community-001', membership_status: 'ACTIVE', roles: ['OWNER'] },
        { community_id: 'community-002', membership_status: 'PENDING', roles: [] }
      ]
    });

    const { listActiveUserCommunities } = await import('../repositories/communityRepository.js');
    const memberships = await listActiveUserCommunities('user-001');

    expect(memberships).toEqual([
      { communityId: 'community-001', membershipStatus: 'ACTIVE', roles: ['OWNER'] }
    ]);
  });
});
