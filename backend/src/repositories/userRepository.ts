import { query } from '../config/db.js';

export type AppUserRecord = {
  id: string;
  firebase_uid: string;
  full_name: string;
  email: string | null;
  phone: string | null;
  status: string;
};

export async function findUserByFirebaseUid(firebaseUid: string): Promise<AppUserRecord | null> {
  const result = await query<AppUserRecord>(
    `SELECT id, firebase_uid, full_name, email, phone, status
     FROM users
     WHERE firebase_uid = $1
     LIMIT 1`,
    [firebaseUid]
  );

  return result.rows[0] ?? null;
}

export async function findUserById(userId: string): Promise<AppUserRecord | null> {
  const result = await query<AppUserRecord>(
    `SELECT id, firebase_uid, full_name, email, phone, status
     FROM users
     WHERE id = $1
     LIMIT 1`,
    [userId]
  );

  return result.rows[0] ?? null;
}

export async function findUserByIdentity(userId?: string, firebaseUid?: string): Promise<AppUserRecord | null> {
  if (firebaseUid) {
    const byFirebase = await findUserByFirebaseUid(firebaseUid);
    if (byFirebase) {
      return byFirebase;
    }
  }

  if (userId) {
    return findUserById(userId);
  }

  return null;
}
