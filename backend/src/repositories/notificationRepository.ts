import { query } from '../config/db.js';

export type NotificationRecord = {
  id: string;
  community_id: string;
  recipient_user_id: string;
  channel: 'IN_APP' | 'EMAIL' | 'PUSH';
  subject: string;
  message: string;
  payload: Record<string, unknown> | null;
  is_read: boolean;
  created_at: string;
};

export async function createNotification(input: {
  communityId: string;
  recipientUserId: string;
  channel?: 'IN_APP' | 'EMAIL' | 'PUSH';
  subject: string;
  message: string;
  payload?: Record<string, unknown> | null;
}): Promise<NotificationRecord> {
  const result = await query<NotificationRecord>(
    `INSERT INTO notifications (community_id, recipient_user_id, channel, subject, message, payload, is_read)
     VALUES ($1, $2, $3, $4, $5, $6::jsonb, false)
     RETURNING id, community_id, recipient_user_id, channel, subject, message, payload, is_read, created_at`,
    [
      input.communityId,
      input.recipientUserId,
      input.channel ?? 'IN_APP',
      input.subject,
      input.message,
      input.payload ?? null
    ]
  );

  return result.rows[0];
}

export async function listNotificationsForUser(userId: string, communityId?: string): Promise<NotificationRecord[]> {
  const result = await query<NotificationRecord>(
    `SELECT id, community_id, recipient_user_id, channel, subject, message, payload, is_read, created_at
     FROM notifications
     WHERE recipient_user_id = $1
       AND ($2::uuid IS NULL OR community_id = $2)
     ORDER BY created_at DESC, id DESC
     LIMIT 50`,
    [userId, communityId ?? null]
  );

  return result.rows;
}
