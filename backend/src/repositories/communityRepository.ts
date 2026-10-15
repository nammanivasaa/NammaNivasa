import { query } from '../config/db.js';
import type { MembershipStatus } from '../lib/authorization.js';

export type CommunityMembershipSummary = {
  communityId: string;
  membershipStatus: MembershipStatus;
  roles: string[];
};

export async function listUserMemberships(userId: string): Promise<CommunityMembershipSummary[]> {
  const result = await query<{ community_id: string; membership_status: MembershipStatus; roles: string[] }>(
    `SELECT
       cm.community_id,
       cm.membership_status,
       COALESCE(
         array_agg(DISTINCT r.code ORDER BY r.code)
           FILTER (WHERE r.code IS NOT NULL),
         ARRAY[]::varchar[]
       ) AS roles
     FROM community_memberships cm
     LEFT JOIN community_role_assignments cra
       ON cra.user_id = cm.user_id
      AND cra.community_id = cm.community_id
      AND cra.status = 'ACTIVE'
     LEFT JOIN roles r
       ON r.id = cra.role_id
     WHERE cm.user_id = $1
     GROUP BY cm.community_id, cm.membership_status
     ORDER BY cm.community_id`,
    [userId]
  );

  return result.rows.map((row) => ({
    communityId: row.community_id,
    membershipStatus: row.membership_status,
    roles: Array.isArray(row.roles) ? row.roles : []
  }));
}

export async function listActiveUserCommunities(userId: string): Promise<CommunityMembershipSummary[]> {
  return (await listUserMemberships(userId)).filter((membership) => membership.membershipStatus === 'ACTIVE');
}

export async function getMembershipStatus(userId: string, communityId: string): Promise<MembershipStatus | null> {
  const result = await query<{ membership_status: MembershipStatus }>(
    `SELECT membership_status
     FROM community_memberships
     WHERE user_id = $1 AND community_id = $2
     LIMIT 1`,
    [userId, communityId]
  );

  return result.rows[0]?.membership_status ?? null;
}

export async function getMembershipContext(userId: string, communityId: string): Promise<CommunityMembershipSummary | null> {
  const memberships = await listUserMemberships(userId);
  return memberships.find((membership) => membership.communityId === communityId) ?? null;
}

export type CommunityMembershipRecord = {
  userId: string;
  communityId: string;
  membershipStatus: MembershipStatus;
  statusReason: string | null;
  joinedAt: string;
  statusUpdatedAt: string;
  roles: string[];
};

export async function listCommunityMemberships(communityId: string): Promise<CommunityMembershipRecord[]> {
  const result = await query<{
    user_id: string;
    community_id: string;
    membership_status: MembershipStatus;
    status_reason: string | null;
    joined_at: string;
    status_updated_at: string;
    roles: string[];
  }>(
    `SELECT
       cm.user_id,
       cm.community_id,
       cm.membership_status,
       cm.status_reason,
       cm.joined_at,
       cm.status_updated_at,
       COALESCE(
         array_agg(DISTINCT r.code ORDER BY r.code)
           FILTER (WHERE r.code IS NOT NULL),
         ARRAY[]::varchar[]
       ) AS roles
     FROM community_memberships cm
     LEFT JOIN community_role_assignments cra
       ON cra.user_id = cm.user_id
      AND cra.community_id = cm.community_id
      AND cra.status = 'ACTIVE'
     LEFT JOIN roles r
       ON r.id = cra.role_id
     WHERE cm.community_id = $1
     GROUP BY cm.user_id, cm.community_id, cm.membership_status, cm.status_reason, cm.joined_at, cm.status_updated_at
     ORDER BY cm.user_id`,
    [communityId]
  );

  return result.rows.map((row) => ({
    userId: row.user_id,
    communityId: row.community_id,
    membershipStatus: row.membership_status,
    statusReason: row.status_reason,
    joinedAt: row.joined_at,
    statusUpdatedAt: row.status_updated_at,
    roles: Array.isArray(row.roles) ? row.roles : []
  }));
}

export async function applyCommunityMembership(userId: string, communityId: string): Promise<CommunityMembershipRecord | null> {
  const existing = await getMembershipContext(userId, communityId);
  if (existing) {
    return {
      userId,
      communityId,
      membershipStatus: existing.membershipStatus,
      statusReason: null,
      joinedAt: new Date().toISOString(),
      statusUpdatedAt: new Date().toISOString(),
      roles: existing.roles
    };
  }

  const result = await query<{ user_id: string; community_id: string; membership_status: MembershipStatus; status_reason: string | null; joined_at: string; status_updated_at: string; roles: string[] }>(
    `INSERT INTO community_memberships (user_id, community_id, membership_status, approved_by_user_id, joined_at, status_updated_at, status_reason)
     VALUES ($1, $2, 'PENDING', NULL, NOW(), NOW(), NULL)
     ON CONFLICT (user_id, community_id) DO NOTHING
     RETURNING user_id, community_id, membership_status, status_reason, joined_at, status_updated_at, NULL::varchar[] AS roles`,
    [userId, communityId]
  );

  const row = result.rows[0];
  if (!row) {
    return null;
  }

  return {
    userId: row.user_id,
    communityId: row.community_id,
    membershipStatus: row.membership_status,
    statusReason: row.status_reason,
    joinedAt: row.joined_at,
    statusUpdatedAt: row.status_updated_at,
    roles: Array.isArray(row.roles) ? row.roles : []
  };
}

export async function assignCommunityRole(
  userId: string,
  communityId: string,
  roleCode: string,
  assignedByUserId?: string
): Promise<{ userId: string; communityId: string; roleCode: string; assignedAt: string } | null> {
  const roleResult = await query<{ id: string }>(
    `SELECT id FROM roles WHERE community_id = $1 AND code = $2 LIMIT 1`,
    [communityId, roleCode]
  );

  const role = roleResult.rows[0];
  if (!role) {
    return null;
  }

  const result = await query<{ user_id: string; community_id: string; role_id: string; assigned_at: string }>(
    `INSERT INTO community_role_assignments (user_id, community_id, role_id, status, assigned_at)
     VALUES ($1, $2, $3, 'ACTIVE', NOW())
     RETURNING user_id, community_id, role_id, assigned_at`,
    [userId, communityId, role.id]
  );

  const row = result.rows[0];
  if (!row) {
    return null;
  }

  return {
    userId: row.user_id,
    communityId: row.community_id,
    roleCode,
    assignedAt: row.assigned_at
  };
}

export type CommunityRecord = {
  id: string;
  name: string;
  slug: string;
  status: 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
  created_at: string;
  updated_at: string;
};

export async function findCommunityById(communityId: string): Promise<CommunityRecord | null> {
  const result = await query<CommunityRecord>(
    `SELECT id, name, slug, status, created_at, updated_at
     FROM communities
     WHERE id = $1
     LIMIT 1`,
    [communityId]
  );

  return result.rows[0] ?? null;
}

export async function listCommunitiesForUser(userId: string): Promise<CommunityRecord[]> {
  const result = await query<CommunityRecord>(
    `SELECT c.id, c.name, c.slug, c.status, c.created_at, c.updated_at
     FROM communities c
     INNER JOIN community_memberships cm ON cm.community_id = c.id
     WHERE cm.user_id = $1
     GROUP BY c.id, c.name, c.slug, c.status, c.created_at, c.updated_at
     ORDER BY c.name ASC, c.id ASC`,
    [userId]
  );

  return result.rows;
}

export const COMMUNITY_SEARCH_RESULT_LIMIT = 50;

/** Escapes LIKE/ILIKE wildcards so user input is matched literally. */
function escapeLikeTerm(value: string): string {
  return value.replace(/[\\%_]/g, (match) => `\\${match}`);
}

/**
 * Discovers ACTIVE communities by name, independent of membership. Only
 * authoritative `communities` columns are searched — the schema has no
 * location or PIN code fields, so none are fabricated here. Results are
 * capped so a broad term cannot return an unbounded list.
 */
export async function searchCommunities(
  term: string,
  limit = COMMUNITY_SEARCH_RESULT_LIMIT
): Promise<CommunityRecord[]> {
  const trimmed = term.trim();
  const pattern = trimmed === '' ? null : `%${escapeLikeTerm(trimmed)}%`;

  const result = await query<CommunityRecord>(
    `SELECT id, name, slug, status, created_at, updated_at
     FROM communities
     WHERE status = 'ACTIVE'
       AND ($1::text IS NULL OR name ILIKE $1)
     ORDER BY name ASC, id ASC
     LIMIT $2`,
    [pattern, limit]
  );

  return result.rows;
}

export async function listMembershipsForCommunity(communityId: string): Promise<CommunityMembershipSummary[]> {
  const result = await query<{ community_id: string; membership_status: MembershipStatus; roles: string[] }>(
    `SELECT
       cm.user_id,
       cm.community_id,
       cm.membership_status,
       COALESCE(
         array_agg(DISTINCT r.code ORDER BY r.code)
           FILTER (WHERE r.code IS NOT NULL),
         ARRAY[]::varchar[]
       ) AS roles
     FROM community_memberships cm
     LEFT JOIN community_role_assignments cra
       ON cra.user_id = cm.user_id
      AND cra.community_id = cm.community_id
      AND cra.status = 'ACTIVE'
     LEFT JOIN roles r
       ON r.id = cra.role_id
     WHERE cm.community_id = $1
     GROUP BY cm.user_id, cm.community_id, cm.membership_status
     ORDER BY cm.user_id`,
    [communityId]
  );

  return result.rows.map((row) => ({
    communityId: row.community_id,
    membershipStatus: row.membership_status,
    roles: Array.isArray(row.roles) ? row.roles : []
  }));
}

export async function updateMembershipStatus(
  userId: string,
  communityId: string,
  membershipStatus: MembershipStatus,
  reason?: string | null
): Promise<CommunityMembershipSummary | null> {
  const result = await query<{ community_id: string; membership_status: MembershipStatus; roles: string[] }>(
    `UPDATE community_memberships
     SET membership_status = $3,
         ended_at = CASE WHEN $3 IN ('REVOKED', 'EXPIRED') THEN NOW() ELSE ended_at END,
         status_updated_at = NOW(),
         status_reason = $4
     WHERE user_id = $1 AND community_id = $2
     RETURNING community_id, membership_status, NULL::varchar[] AS roles`,
    [userId, communityId, membershipStatus, reason ?? null]
  );

  const row = result.rows[0];
  if (!row) {
    return null;
  }

  const roles = await listUserMemberships(userId);
  const membership = roles.find((entry) => entry.communityId === communityId) ?? {
    communityId,
    membershipStatus,
    roles: []
  };

  return {
    communityId: membership.communityId,
    membershipStatus: membership.membershipStatus,
    roles: membership.roles
  };
}
