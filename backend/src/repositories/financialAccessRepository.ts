import { query } from '../config/db.js';

export type HouseholdFinancialAccessRecord = {
  id: string;
  community_id: string;
  owner_user_id: string;
  grantee_user_id: string;
  granted_by_user_id: string;
  status: 'ACTIVE' | 'REVOKED';
  reason: string | null;
  notes: string | null;
  granted_at: string;
  revoked_at: string | null;
  created_at: string;
  updated_at: string;
};

export async function createFinancialAccessGrant(input: {
  communityId: string;
  ownerUserId: string;
  granteeUserId: string;
  grantedByUserId: string;
  reason?: string | null;
  notes?: string | null;
}): Promise<HouseholdFinancialAccessRecord> {
  const result = await query<HouseholdFinancialAccessRecord>(
    `INSERT INTO household_financial_access (community_id, owner_user_id, grantee_user_id, granted_by_user_id, status, reason, notes)
     VALUES ($1, $2, $3, $4, 'ACTIVE', $5, $6)
     RETURNING id, community_id, owner_user_id, grantee_user_id, granted_by_user_id, status, reason, notes, granted_at, revoked_at, created_at, updated_at`,
    [
      input.communityId,
      input.ownerUserId,
      input.granteeUserId,
      input.grantedByUserId,
      input.reason ?? null,
      input.notes ?? null
    ]
  );

  return result.rows[0];
}

export async function listFinancialAccessGrantsForOwner(communityId: string, ownerUserId: string): Promise<HouseholdFinancialAccessRecord[]> {
  const result = await query<HouseholdFinancialAccessRecord>(
    `SELECT id, community_id, owner_user_id, grantee_user_id, granted_by_user_id, status, reason, notes, granted_at, revoked_at, created_at, updated_at
     FROM household_financial_access
     WHERE community_id = $1 AND owner_user_id = $2
     ORDER BY granted_at DESC, id DESC`,
    [communityId, ownerUserId]
  );

  return result.rows;
}

export async function hasFinancialAccessGrant(communityId: string, ownerUserId: string, granteeUserId: string): Promise<boolean> {
  const result = await query<{ exists: boolean }>(
    `SELECT EXISTS (
       SELECT 1 FROM household_financial_access
       WHERE community_id = $1
         AND owner_user_id = $2
         AND grantee_user_id = $3
         AND status = 'ACTIVE'
    ) AS exists`,
    [communityId, ownerUserId, granteeUserId]
  );

  return Boolean(result.rows[0]?.exists);
}

export async function revokeFinancialAccessGrant(input: {
  communityId: string;
  grantId: string;
  revokedByUserId: string;
  reason?: string | null;
}): Promise<HouseholdFinancialAccessRecord | null> {
  const result = await query<HouseholdFinancialAccessRecord>(
    `UPDATE household_financial_access
     SET status = 'REVOKED', revoked_at = NOW(), updated_at = NOW(), reason = COALESCE($4, reason)
     WHERE community_id = $1 AND id = $2 AND status = 'ACTIVE'
     RETURNING id, community_id, owner_user_id, grantee_user_id, granted_by_user_id, status, reason, notes, granted_at, revoked_at, created_at, updated_at`,
    [input.communityId, input.grantId, input.revokedByUserId, input.reason ?? null]
  );

  return result.rows[0] ?? null;
}
