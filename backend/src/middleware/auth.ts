import type { NextFunction, Request, Response } from 'express';
import { env } from '../config/env.js';
import { isFirebaseConfigured, verifyFirebaseToken } from '../config/firebase.js';
import type { AuthenticatedUser } from '../lib/authorization.js';

export type AuthorizedRequest = Request & {
  user?: AuthenticatedUser;
};

function buildTestUser(userHeader: string | undefined): AuthenticatedUser {
  const id = userHeader ?? '11111111-1111-4111-8111-111111111111';
  const primaryCommunityId = '22222222-2222-4222-8222-222222222222';
  const isPrimaryAdminUser = id === '11111111-1111-4111-8111-111111111111' || id === 'user-001';
  const isTenantUser = id === 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa' || id === 'tenant-001';

  return {
    id,
    firebaseUid: `firebase-${id}`,
    email: `${id}@example.com`,
    communities: {
      [primaryCommunityId]: {
        communityId: primaryCommunityId,
        status: 'ACTIVE',
        roles: isPrimaryAdminUser
          ? ['OWNER', 'COMMUNITY_ADMINISTRATOR', 'TENANT', 'TREASURER_FINANCE_MANAGER']
          : isTenantUser ? ['TENANT'] : ['TENANT']
      }
    }
  };
}

export async function authenticate(req: AuthorizedRequest, res: Response, next: NextFunction): Promise<void> {
  const authHeader = req.headers.authorization;

  if (env.NODE_ENV === 'production' && env.AUTH_MODE === 'test') {
    res.status(500).json({
      error: { code: 'SERVER_ERROR', message: 'Test authentication is disabled in production.' }
    });
    return;
  }

  if (env.AUTH_MODE === 'test') {
    const testUserId = req.headers['x-test-user'];
    if (typeof testUserId === 'string') {
      req.user = buildTestUser(testUserId);
      return next();
    }

    if (authHeader && authHeader.startsWith('Bearer ')) {
      const token = authHeader.slice('Bearer '.length).trim();
      if (token === 'test-token') {
        req.user = buildTestUser('user-001');
        return next();
      }
    }

    res.status(401).json({ error: { code: 'UNAUTHENTICATED', message: 'Authentication required.' } });
    return;
  }

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    res.status(401).json({ error: { code: 'UNAUTHENTICATED', message: 'Authentication required.' } });
    return;
  }

  const token = authHeader.slice('Bearer '.length).trim();
  if (!token) {
    res.status(401).json({ error: { code: 'UNAUTHENTICATED', message: 'Authentication required.' } });
    return;
  }

  try {
    if (!isFirebaseConfigured()) {
      throw new Error('Firebase Admin is not configured.');
    }

    const decoded = await verifyFirebaseToken(token);
    req.user = {
      id: decoded.uid,
      firebaseUid: decoded.uid,
      email: decoded.email,
      communities: {}
    };
    return next();
  } catch (error) {
    res.status(401).json({
      error: {
        code: 'UNAUTHENTICATED',
        message: 'Invalid or expired Firebase token.'
      }
    });
    return;
  }
}
