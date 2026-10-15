import admin from 'firebase-admin';
import { env } from './env.js';

const privateKey = env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n');
const firebaseIsConfigured = Boolean(
  env.FIREBASE_PROJECT_ID &&
    env.FIREBASE_CLIENT_EMAIL &&
    privateKey
);

if (firebaseIsConfigured && !admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: env.FIREBASE_PROJECT_ID,
      clientEmail: env.FIREBASE_CLIENT_EMAIL,
      privateKey
    })
  });
}

export function isFirebaseConfigured(): boolean {
  return firebaseIsConfigured;
}

export async function verifyFirebaseToken(token: string) {
  if (!firebaseIsConfigured) {
    throw new Error('Firebase Admin is not configured.');
  }

  return admin.auth().verifyIdToken(token);
}
