import { query } from '../config/db.js';

export type MaintenanceRequestRecord = {
  id: string;
  community_id: string;
  flat_id: string | null;
  created_by_user_id: string;
  category: string;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REJECTED';
  created_at: string;
  updated_at: string;
};

export async function listMaintenanceRequestsForCommunity(
  communityId: string,
  filters?: {
    userId?: string;
    includeAll?: boolean;
  }
): Promise<MaintenanceRequestRecord[]> {
  const params: unknown[] = [communityId];
  let sql = `SELECT id, community_id, flat_id, created_by_user_id, category, title, description, priority, status, created_at, updated_at
             FROM maintenance_requests
             WHERE community_id = $1`;

  if (!filters?.includeAll && filters?.userId) {
    sql += ` AND created_by_user_id = $2`;
    params.push(filters.userId);
  }

  sql += ` ORDER BY created_at DESC, id DESC`;

  const result = await query<MaintenanceRequestRecord>(sql, params);
  return result.rows;
}

export async function getMaintenanceRequestById(communityId: string, requestId: string): Promise<MaintenanceRequestRecord | null> {
  const result = await query<MaintenanceRequestRecord>(
    `SELECT id, community_id, flat_id, created_by_user_id, category, title, description, priority, status, created_at, updated_at
     FROM maintenance_requests
     WHERE community_id = $1 AND id = $2
     LIMIT 1`,
    [communityId, requestId]
  );

  return result.rows[0] ?? null;
}

export async function createMaintenanceRequest(input: {
  communityId: string;
  createdByUserId: string;
  category: string;
  title: string;
  description: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  flatId?: string | null;
}): Promise<MaintenanceRequestRecord> {
  const result = await query<MaintenanceRequestRecord>(
    `INSERT INTO maintenance_requests (
       community_id,
       flat_id,
       created_by_user_id,
       category,
       title,
       description,
       priority,
       status
     ) VALUES ($1, $2, $3, $4, $5, $6, $7, 'OPEN')
     RETURNING id, community_id, flat_id, created_by_user_id, category, title, description, priority, status, created_at, updated_at`,
    [
      input.communityId,
      input.flatId ?? null,
      input.createdByUserId,
      input.category,
      input.title,
      input.description,
      input.priority ?? 'MEDIUM'
    ]
  );

  return result.rows[0];
}
