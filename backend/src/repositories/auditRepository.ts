import { query } from '../config/db.js';

export type AuditLogRecord = {
  id: string;
  community_id: string;
  actor_user_id: string;
  resource_type: string;
  resource_id: string;
  action: string;
  details: Record<string, unknown> | null;
  created_at: string;
};

export async function logAuditEvent(input: {
  communityId: string;
  actorUserId: string;
  resourceType: string;
  resourceId: string;
  action: string;
  details?: Record<string, unknown> | null;
}): Promise<AuditLogRecord> {
  const result = await query<AuditLogRecord>(
    `INSERT INTO audit_logs (community_id, actor_user_id, resource_type, resource_id, action, details)
     VALUES ($1, $2, $3, $4, $5, $6::jsonb)
     RETURNING id, community_id, actor_user_id, resource_type, resource_id, action, details, created_at`,
    [input.communityId, input.actorUserId, input.resourceType, input.resourceId, input.action, input.details ?? null]
  );

  return result.rows[0];
}

export async function listAuditLogsForCommunity(communityId: string, limit = 50): Promise<AuditLogRecord[]> {
  const result = await query<AuditLogRecord>(
    `SELECT id, community_id, actor_user_id, resource_type, resource_id, action, details, created_at
     FROM audit_logs
     WHERE community_id = $1
     ORDER BY created_at DESC, id DESC
     LIMIT $2`,
    [communityId, limit]
  );

  return result.rows;
}
