import { query } from '../config/db.js';

export type VisitorRequestRecord = {
  id: string;
  community_id: string;
  flat_id: string | null;
  host_user_id: string;
  visitor_name: string;
  purpose: string;
  expected_arrival: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  created_at: string;
  updated_at: string;
  decision_by_user_id: string | null;
  decision_at: string | null;
  decision_reason: string | null;
};

export async function listVisitorRequestsForCommunity(communityId: string): Promise<VisitorRequestRecord[]> {
  const result = await query<VisitorRequestRecord>(
    `SELECT id, community_id, flat_id, host_user_id, visitor_name, purpose, expected_arrival, status,
            created_at, updated_at, decision_by_user_id, decision_at, decision_reason
     FROM visitor_requests
     WHERE community_id = $1
     ORDER BY created_at DESC, id DESC`,
    [communityId]
  );

  return result.rows;
}

export async function listVisitorRequestsForUser(userId: string): Promise<VisitorRequestRecord[]> {
  const result = await query<VisitorRequestRecord>(
    `SELECT id, community_id, flat_id, host_user_id, visitor_name, purpose, expected_arrival, status,
            created_at, updated_at, decision_by_user_id, decision_at, decision_reason
     FROM visitor_requests
     WHERE host_user_id = $1
     ORDER BY created_at DESC, id DESC`,
    [userId]
  );

  return result.rows;
}

export async function getVisitorRequestById(communityId: string, requestId: string): Promise<VisitorRequestRecord | null> {
  const result = await query<VisitorRequestRecord>(
    `SELECT id, community_id, flat_id, host_user_id, visitor_name, purpose, expected_arrival, status,
            created_at, updated_at, decision_by_user_id, decision_at, decision_reason
     FROM visitor_requests
     WHERE community_id = $1 AND id = $2
     LIMIT 1`,
    [communityId, requestId]
  );

  return result.rows[0] ?? null;
}

export async function createVisitorRequest(input: {
  communityId: string;
  hostUserId: string;
  visitorName: string;
  purpose: string;
  expectedArrival: string;
  flatId?: string | null;
}): Promise<VisitorRequestRecord> {
  const result = await query<VisitorRequestRecord>(
    `INSERT INTO visitor_requests (
       community_id,
       flat_id,
       host_user_id,
       visitor_name,
       purpose,
       expected_arrival,
       status,
       created_at,
       updated_at
     ) VALUES ($1, $2, $3, $4, $5, $6, 'PENDING', NOW(), NOW())
     RETURNING id, community_id, flat_id, host_user_id, visitor_name, purpose, expected_arrival, status,
               created_at, updated_at, decision_by_user_id, decision_at, decision_reason`,
    [input.communityId, input.flatId ?? null, input.hostUserId, input.visitorName, input.purpose, input.expectedArrival]
  );

  return result.rows[0];
}

export async function approveVisitorRequest(
  communityId: string,
  requestId: string,
  decisionByUserId: string,
  decisionReason?: string | null
): Promise<VisitorRequestRecord | null> {
  const result = await query<VisitorRequestRecord>(
    `UPDATE visitor_requests
     SET status = 'APPROVED',
         decision_by_user_id = $3,
         decision_at = NOW(),
         decision_reason = $4,
         updated_at = NOW()
     WHERE community_id = $1 AND id = $2 AND status = 'PENDING'
     RETURNING id, community_id, flat_id, host_user_id, visitor_name, purpose, expected_arrival, status,
               created_at, updated_at, decision_by_user_id, decision_at, decision_reason`,
    [communityId, requestId, decisionByUserId, decisionReason ?? null]
  );

  return result.rows[0] ?? null;
}

export async function rejectVisitorRequest(
  communityId: string,
  requestId: string,
  decisionByUserId: string,
  decisionReason?: string | null
): Promise<VisitorRequestRecord | null> {
  const result = await query<VisitorRequestRecord>(
    `UPDATE visitor_requests
     SET status = 'REJECTED',
         decision_by_user_id = $3,
         decision_at = NOW(),
         decision_reason = $4,
         updated_at = NOW()
     WHERE community_id = $1 AND id = $2 AND status = 'PENDING'
     RETURNING id, community_id, flat_id, host_user_id, visitor_name, purpose, expected_arrival, status,
               created_at, updated_at, decision_by_user_id, decision_at, decision_reason`,
    [communityId, requestId, decisionByUserId, decisionReason ?? null]
  );

  return result.rows[0] ?? null;
}

export async function cancelVisitorRequest(
  communityId: string,
  requestId: string,
  userId: string
): Promise<VisitorRequestRecord | null> {
  const result = await query<VisitorRequestRecord>(
    `UPDATE visitor_requests
     SET status = 'CANCELLED',
         decision_by_user_id = $3,
         decision_at = NOW(),
         decision_reason = 'Cancelled by requester',
         updated_at = NOW()
     WHERE community_id = $1 AND id = $2 AND host_user_id = $3 AND status = 'PENDING'
     RETURNING id, community_id, flat_id, host_user_id, visitor_name, purpose, expected_arrival, status,
               created_at, updated_at, decision_by_user_id, decision_at, decision_reason`,
    [communityId, requestId, userId]
  );

  return result.rows[0] ?? null;
}
