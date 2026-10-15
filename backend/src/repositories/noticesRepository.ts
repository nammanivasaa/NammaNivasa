import { query } from '../config/db.js';

export type NoticeRecord = {
  id: string;
  community_id: string;
  created_by_user_id: string;
  title: string;
  message: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  starts_at: string | null;
  ends_at: string | null;
  created_at: string;
  updated_at: string;
};

export async function listNoticesForCommunity(communityId: string): Promise<NoticeRecord[]> {
  const result = await query<NoticeRecord>(
    `SELECT id, community_id, created_by_user_id, title, message, status, starts_at, ends_at, created_at, updated_at
     FROM notices
     WHERE community_id = $1
     ORDER BY created_at DESC, id DESC`,
    [communityId]
  );

  return result.rows;
}

export async function getNoticeById(communityId: string, noticeId: string): Promise<NoticeRecord | null> {
  const result = await query<NoticeRecord>(
    `SELECT id, community_id, created_by_user_id, title, message, status, starts_at, ends_at, created_at, updated_at
     FROM notices
     WHERE community_id = $1 AND id = $2
     LIMIT 1`,
    [communityId, noticeId]
  );

  return result.rows[0] ?? null;
}

export async function createNotice(input: {
  communityId: string;
  createdByUserId: string;
  title: string;
  message: string;
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  startsAt?: string | null;
  endsAt?: string | null;
}): Promise<NoticeRecord> {
  const result = await query<NoticeRecord>(
    `INSERT INTO notices (
       community_id,
       created_by_user_id,
       title,
       message,
       status,
       starts_at,
       ends_at
     ) VALUES ($1, $2, $3, $4, $5, $6, $7)
     RETURNING id, community_id, created_by_user_id, title, message, status, starts_at, ends_at, created_at, updated_at`,
    [
      input.communityId,
      input.createdByUserId,
      input.title,
      input.message,
      input.status ?? 'DRAFT',
      input.startsAt ?? null,
      input.endsAt ?? null
    ]
  );

  return result.rows[0];
}

export async function publishNotice(communityId: string, noticeId: string): Promise<NoticeRecord | null> {
  const result = await query<NoticeRecord>(
    `UPDATE notices
     SET status = 'PUBLISHED', updated_at = NOW()
     WHERE community_id = $1 AND id = $2
     RETURNING id, community_id, created_by_user_id, title, message, status, starts_at, ends_at, created_at, updated_at`,
    [communityId, noticeId]
  );

  return result.rows[0] ?? null;
}
