import { Router } from 'express';
import { z } from 'zod';
import { query } from '../config/db.js';
import { env } from '../config/env.js';
import { authenticate, type AuthorizedRequest } from '../middleware/auth.js';
import { HttpError } from '../middleware/error.js';
import { authorizeAction, canAccessCommunity, type AuthenticatedUser } from '../lib/authorization.js';
import { getMembershipContext, listActiveUserCommunities, listUserMemberships, searchCommunities, createCommunity, findCommunityById } from '../repositories/communityRepository.js';
import { createMaintenanceRequest, listMaintenanceRequestsForCommunity } from '../repositories/maintenanceRepository.js';
import { createNotice, getNoticeById, listNoticesForCommunity, publishNotice } from '../repositories/noticesRepository.js';
import { logAuditEvent } from '../repositories/auditRepository.js';
import { createNotification } from '../repositories/notificationRepository.js';
import { findUserByIdentity } from '../repositories/userRepository.js';
import { approveVisitorRequest, cancelVisitorRequest, createVisitorRequest, getVisitorRequestById, listVisitorRequestsForCommunity, rejectVisitorRequest } from '../repositories/visitorRepository.js';
import {
  createBill,
  createPayment,
  getBillById,
  getOutstandingBalanceForBill,
  getPaymentById,
  listBillsForCommunity,
  listPaymentsForCommunity,
  listPaymentsForUser,
  rejectPayment,
  reversePayment,
  updateBillStatus,
  verifyPayment
} from '../repositories/financialRepository.js';
import { createFinancialAccessGrant, hasFinancialAccessGrant, listFinancialAccessGrantsForOwner, revokeFinancialAccessGrant } from '../repositories/financialAccessRepository.js';

const router = Router();

function getParam(value: string | string[] | undefined, fallback = ''): string {
  return Array.isArray(value) ? value[0] ?? fallback : value ?? fallback;
}

const PRIMARY_COMMUNITY_ID = '22222222-2222-4222-8222-222222222222';
const UNAUTHORIZED_COMMUNITY_ID = '44444444-4444-4444-8444-444444444444';

function isDatabaseConfigured(): boolean {
  return Boolean(process.env.DATABASE_URL || process.env.TEST_DATABASE_URL);
}

const notices = [
  { id: 'n-1', title: 'Water tank maintenance', message: 'Tank cleaning scheduled for Sunday.', communityId: PRIMARY_COMMUNITY_ID, status: 'PUBLISHED' },
  { id: 'n-2', title: 'Lift 2 inspection', message: 'Lift inspection after 4 pm.', communityId: PRIMARY_COMMUNITY_ID, status: 'PUBLISHED' }
];

const maintenanceRequests = [
  { id: 'mr-1', communityId: PRIMARY_COMMUNITY_ID, title: 'Leak at kitchen sink', status: 'OPEN', createdBy: '11111111-1111-4111-8111-111111111111' },
  { id: 'mr-2', communityId: PRIMARY_COMMUNITY_ID, title: 'Pool pump noise', status: 'IN_PROGRESS', createdBy: '22222222-2222-4222-8222-222222222222' }
];

const visitors = [
  { id: 'v-1', communityId: PRIMARY_COMMUNITY_ID, visitorName: 'Arihant Sharma', status: 'PENDING' },
  { id: 'v-2', communityId: PRIMARY_COMMUNITY_ID, visitorName: 'Meena Iyer', status: 'APPROVED' }
];

const bills: Array<{
  id: string;
  communityId: string;
  title: string;
  amount: string;
  status: string;
  dueAt?: string;
  periodStart?: string;
  periodEnd?: string;
  currency?: string;
  flatId?: string | null;
  createdAt?: string;
}> = [
  { id: 'b-1', communityId: PRIMARY_COMMUNITY_ID, title: 'Maintenance', amount: '4200.00', status: 'OPEN', dueAt: new Date().toISOString(), periodStart: new Date().toISOString(), periodEnd: new Date(Date.now() + 86400000).toISOString(), currency: 'INR' },
  { id: 'b-2', communityId: PRIMARY_COMMUNITY_ID, title: 'Water', amount: '980.00', status: 'PAID', dueAt: new Date().toISOString(), periodStart: new Date().toISOString(), periodEnd: new Date(Date.now() + 86400000).toISOString(), currency: 'INR' }
];

const payments: Array<{
  id: string;
  communityId: string;
  billId?: string | null;
  payerUserId: string;
  amount: string;
  status: string;
  visibility: string;
  flatId?: string | null;
  currency?: string;
  paymentMethod?: string | null;
  reference?: string | null;
  evidenceRef?: string | null;
  createdAt?: string;
  updatedAt?: string;
  verifiedByUserId?: string | null;
  verifiedAt?: string | null;
  rejectedByUserId?: string | null;
  rejectedAt?: string | null;
  reversalReason?: string | null;
}> = [
  { id: 'p-1', communityId: PRIMARY_COMMUNITY_ID, payerUserId: '11111111-1111-4111-8111-111111111111', amount: '4200.00', status: 'PENDING', visibility: 'PRIVATE_TO_OWNER', updatedAt: new Date().toISOString() },
  { id: 'p-2', communityId: PRIMARY_COMMUNITY_ID, payerUserId: '22222222-2222-4222-8222-222222222222', amount: '1500.00', status: 'PAID', visibility: 'PRIVATE_TO_OWNER', updatedAt: new Date().toISOString() }
];

const householdAccessGrants: Array<{
  id: string;
  communityId: string;
  ownerUserId: string;
  granteeUserId: string;
  grantedByUserId: string;
  status: 'ACTIVE' | 'REVOKED';
  reason?: string | null;
  notes?: string | null;
  grantedAt: string;
  revokedAt?: string | null;
}> = [];

function requireCommunityAccess(user: AuthenticatedUser, communityId: string): void {
  if (!canAccessCommunity(user, communityId)) {
    throw new HttpError(403, 'UNAUTHORIZED', 'User is not active in the requested community.');
  }
}

function buildDemoUserProfile(user: AuthenticatedUser) {
  return {
    id: user.id,
    fullName: user.email?.split('@')[0] ?? user.id,
    email: user.email ?? `${user.id}@example.com`,
    phone: null,
    status: 'ACTIVE',
    activeCommunities: Object.values(user.communities)
      .filter((membership) => membership.status === 'ACTIVE')
      .map((membership) => ({
        communityId: membership.communityId,
        membershipStatus: membership.status,
        roles: membership.roles
      }))
  };
}

async function resolveUserProfile(user: AuthenticatedUser) {
  if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
    return buildDemoUserProfile(user);
  }

  const appUser = await findUserByIdentity(user.id, user.firebaseUid);
  if (!appUser) {
    throw new HttpError(404, 'NOT_FOUND', 'User profile not found.');
  }

  const memberships = await listActiveUserCommunities(appUser.id);
  return {
    id: appUser.id,
    fullName: appUser.full_name,
    email: appUser.email ?? undefined,
    phone: appUser.phone ?? undefined,
    status: appUser.status,
    activeCommunities: memberships.map((membership) => ({
      communityId: membership.communityId,
      membershipStatus: membership.membershipStatus,
      roles: membership.roles
    }))
  };
}

async function resolveUserCommunities(user: AuthenticatedUser) {
  if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
    return Object.values(user.communities).map((membership) => ({
      communityId: membership.communityId,
      membershipStatus: membership.status,
      roles: membership.roles
    }));
  }

  const appUser = await findUserByIdentity(user.id, user.firebaseUid);
  if (!appUser) {
    throw new HttpError(404, 'NOT_FOUND', 'User profile not found.');
  }

  const memberships = await listUserMemberships(appUser.id);
  return memberships.map((membership) => ({
    communityId: membership.communityId,
    membershipStatus: membership.membershipStatus,
    roles: membership.roles
  }));
}

router.get('/health', (_req, res) => {
  res.json({ ok: true, status: 'healthy', timestamp: new Date().toISOString() });
});

router.get('/api/v1/me', authenticate, async (req: AuthorizedRequest, res) => {
  const user = req.user;
  if (!user) {
    throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
  }

  const profile = await resolveUserProfile(user);
  res.json({ data: profile });
});

router.get('/api/v1/me/communities', authenticate, async (req: AuthorizedRequest, res) => {
  const user = req.user;
  if (!user) {
    throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
  }

  const memberships = await resolveUserCommunities(user);
  res.json({ data: memberships });
});

const communitySearchQuerySchema = z.object({
  q: z.string().trim().max(100).optional()
});

// Community discovery is intentionally separate from membership and
// administrative authorization: any authenticated user may *find* an ACTIVE
// community before requesting to join it. Seeing a community here grants no
// membership, roles, or access — every community-scoped route below still
// enforces requireCommunityAccess. Only authoritative communities columns
// (name) are searchable; the schema has no location or PIN code fields.
router.get('/api/v1/communities/discover', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const parsed = communitySearchQuerySchema.safeParse(req.query);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', 'Search query must be at most 100 characters.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      res.json({ data: [] });
      return;
    }

    const communities = await searchCommunities(parsed.data.q ?? '');
    res.json({
      data: communities.map((community) => ({
        id: community.id,
        name: community.name,
        status: community.status
      }))
    });
  } catch (error) {
    next(error);
  }
});

const createCommunitySchema = z.object({
  name: z.string().trim().min(1, 'Community name is required').max(255),
  societyType: z.string().trim().max(100).optional(),
  state: z.string().trim().min(1, 'State is required').max(100),
  city: z.string().trim().min(1, 'City is required').max(100),
  area: z.string().trim().min(1, 'Area is required').max(100),
  pinCode: z.string().trim().min(1, 'PIN code is required').max(20),
  streetAddress: z.string().trim().min(1, 'Street address is required'),
  buildingName: z.string().trim().max(100).optional(),
  floors: z.coerce.number().int().positive().optional(),
  flatNumber: z.string().trim().max(50).optional(),
  adminName: z.string().trim().max(255).optional(),
  adminEmail: z.string().trim().max(255).optional()
});

router.post('/api/v1/communities', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const parsed = createCommunitySchema.safeParse(req.body);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', parsed.error.issues[0]?.message ?? 'Invalid community payload.');
    }

    const payload = {
      ...parsed.data,
      adminName: parsed.data.adminName ?? user.fullName ?? 'Admin User',
      adminEmail: parsed.data.adminEmail ?? user.email ?? `${user.id}@example.com`
    };

    const created = await createCommunity(user.id, payload);
    res.status(201).json({ data: created });
  } catch (error) {
    next(error);
  }
});

router.get('/api/v1/communities/:communityId', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const community = await findCommunityById(communityId);
    if (!community) {
      throw new HttpError(404, 'NOT_FOUND', 'Community not found.');
    }

    res.json({
      data: {
        id: community.id,
        name: community.name,
        status: community.status,
        members: 1
      }
    });
  } catch (error) {
    next(error);
  }
});

router.get('/api/v1/communities/:communityId/notices', authenticate, (req: AuthorizedRequest, res) => {
  const user = req.user;
  const communityId = getParam(req.params.communityId);
  if (!user) {
    throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
  }
  requireCommunityAccess(user, communityId);

  if (!authorizeAction(user, communityId, 'READ_COMMUNITY_NOTICES')) {
    throw new HttpError(403, 'UNAUTHORIZED', 'User does not have notice-read permission.');
  }

  res.json({ data: notices.filter((notice) => notice.communityId === communityId) });
});

const noticeSchema = z.object({
  title: z.string().trim().min(1),
  message: z.string().trim().min(1),
  published: z.boolean().optional().default(false),
  startsAt: z.string().datetime({ offset: true }).optional(),
  endsAt: z.string().datetime({ offset: true }).optional()
});

async function resolveAuthenticatedMembership(reqUser: AuthenticatedUser | undefined, communityId: string): Promise<AuthenticatedUser> {
  if (!reqUser) {
    throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
  }

if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
  const membership = reqUser.communities[communityId];
  if (!membership || membership.status !== 'ACTIVE') {
    throw new HttpError(403, 'UNAUTHORIZED', 'User is not active in the requested community.');
  }

  return {
    ...reqUser,
    communities: {
      [communityId]: {
        communityId,
        status: membership.status,
        roles: membership.roles
      }
    }
  };
}

const appUser = await findUserByIdentity(reqUser.id, reqUser.firebaseUid);
if (!appUser) {
  throw new HttpError(404, 'NOT_FOUND', 'User profile not found.');
}

const membership = await getMembershipContext(appUser.id, communityId);
if (!membership || membership.membershipStatus !== 'ACTIVE') {
  throw new HttpError(403, 'UNAUTHORIZED', 'User is not active in the requested community.');
}

return {
  ...reqUser,
  id: appUser.id,
  firebaseUid: appUser.firebase_uid,
  email: appUser.email ?? reqUser.email,
  communities: {
    [communityId]: {
      communityId,
      status: membership.membershipStatus,
      roles: membership.roles
    }
  }
};
}

async function resolveProtectedUser(reqUser: AuthenticatedUser | undefined, communityId: string): Promise<AuthenticatedUser> {
  return resolveAuthenticatedMembership(reqUser, communityId);
}

function formatMoney(value: unknown, fieldName: string): string {
  const amount = Number(value);
  if (!Number.isFinite(amount) || amount <= 0) {
    throw new HttpError(400, 'INVALID_INPUT', `${fieldName} must be a positive number.`);
  }

  return amount.toFixed(2);
}

function billResponseFromRow(row: {
  id: string;
  community_id: string;
  flat_id: string | null;
  bill_type: string;
  amount: string;
  currency: string;
  period_start: string;
  period_end: string;
  due_at: string;
  status: string;
  created_at: string;
}) {
  return {
    id: row.id,
    communityId: row.community_id,
    flatId: row.flat_id,
    billType: row.bill_type,
    amount: row.amount,
    currency: row.currency,
    periodStart: row.period_start,
    periodEnd: row.period_end,
    dueAt: row.due_at,
    status: row.status,
    createdAt: row.created_at
  };
}

function paymentResponseFromRow(row: {
  id: string;
  community_id: string;
  bill_id: string | null;
  payer_user_id: string;
  flat_id: string | null;
  amount: string;
  currency: string;
  payment_method: string | null;
  reference: string | null;
  status: string;
  visibility: string;
  evidence_ref: string | null;
  created_at: string;
  updated_at: string;
  verified_by_user_id: string | null;
  verified_at: string | null;
  rejected_by_user_id: string | null;
  rejected_at: string | null;
  reversal_reason: string | null;
}) {
  return {
    id: row.id,
    communityId: row.community_id,
    billId: row.bill_id,
    payerUserId: row.payer_user_id,
    flatId: row.flat_id,
    amount: row.amount,
    currency: row.currency,
    paymentMethod: row.payment_method,
    reference: row.reference,
    status: row.status,
    visibility: row.visibility,
    evidenceRef: row.evidence_ref,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
    verifiedByUserId: row.verified_by_user_id,
    verifiedAt: row.verified_at,
    rejectedByUserId: row.rejected_by_user_id,
    rejectedAt: row.rejected_at,
    reversalReason: row.reversal_reason
  };
}

function hasInMemoryGrant(communityId: string, ownerUserId: string, granteeUserId: string): boolean {
  return householdAccessGrants.some((grant) => grant.communityId === communityId && grant.ownerUserId === ownerUserId && grant.granteeUserId === granteeUserId && grant.status === 'ACTIVE');
}

async function canReadPaymentRecord(user: AuthenticatedUser, communityId: string, payment: { payer_user_id: string; visibility: string }): Promise<boolean> {
  if (user.id === payment.payer_user_id) {
    return true;
  }

  const summaryRead = authorizeAction(user, communityId, 'READ_FINANCIAL_SUMMARY') || authorizeAction(user, communityId, 'APPROVE_PAYMENT');
  if (summaryRead) {
    return true;
  }

  const activeRoles = user.communities[communityId]?.roles ?? [];
  if (payment.visibility === 'PRIVATE_TO_OWNER') {
    return false;
  }

  if (payment.visibility === 'AUTHORIZED_HOUSEHOLD_ONLY') {
    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      return hasInMemoryGrant(communityId, payment.payer_user_id, user.id);
    }
    return await hasFinancialAccessGrant(communityId, payment.payer_user_id, user.id);
  }

  if (payment.visibility === 'COMMUNITY_SUMMARY_ONLY') {
    return activeRoles.some((role) => ['TREASURER_FINANCE_MANAGER', 'COMMUNITY_ADMINISTRATOR', 'COMMITTEE_MEMBER'].includes(role));
  }

  if (payment.visibility === 'RESIDENT_VISIBLE') {
    return canAccessCommunity(user, communityId);
  }

  return activeRoles.some((role) => ['TREASURER_FINANCE_MANAGER', 'COMMUNITY_ADMINISTRATOR', 'FACILITY_MANAGER'].includes(role));
}

router.get('/api/v1/communities/:communityId/notices', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const communityId = getParam(req.params.communityId);
    const user = await resolveProtectedUser(req.user, communityId);

    if (!authorizeAction(user, communityId, 'READ_COMMUNITY_NOTICES')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have notice-read permission.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      res.json({
        data: notices
          .filter((notice) => notice.communityId === communityId)
          .map((notice) => ({
            id: notice.id,
            communityId: notice.communityId,
            createdByUserId: user.id,
            title: notice.title,
            message: notice.message,
            status: notice.status,
            startsAt: null,
            endsAt: null,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          }))
      });
      return;
    }

    const rows = await listNoticesForCommunity(communityId);
    res.json({
      data: rows.map((notice) => ({
        id: notice.id,
        communityId: notice.community_id,
        createdByUserId: notice.created_by_user_id,
        title: notice.title,
        message: notice.message,
        status: notice.status,
        startsAt: notice.starts_at,
        endsAt: notice.ends_at,
        createdAt: notice.created_at,
        updatedAt: notice.updated_at
      }))
    });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/notices', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const communityId = getParam(req.params.communityId);
    const user = await resolveProtectedUser(req.user, communityId);

    const parsed = noticeSchema.safeParse(req.body);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', 'Notice payload is invalid.');
    }

    const canManage = authorizeAction(user, communityId, 'MANAGE_NOTICES');
    const canPublish = authorizeAction(user, communityId, 'PUBLISH_NOTICE');
    if (parsed.data.published && !canPublish) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have permission to publish notices.');
    }
    if (!parsed.data.published && !canManage) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have permission to manage notices.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const created = {
        id: `n-${Date.now()}`,
        communityId,
        createdByUserId: user.id,
        title: parsed.data.title,
        message: parsed.data.message,
        status: parsed.data.published ? 'PUBLISHED' : 'DRAFT',
        startsAt: parsed.data.startsAt ?? null,
        endsAt: parsed.data.endsAt ?? null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      } as const;

      notices.push({
        id: created.id,
        title: created.title,
        message: created.message,
        communityId: created.communityId,
        status: created.status
      });

      res.status(201).json({
        data: {
          id: created.id,
          communityId: created.communityId,
          createdByUserId: created.createdByUserId,
          title: created.title,
          message: created.message,
          status: created.status,
          startsAt: created.startsAt,
          endsAt: created.endsAt,
          createdAt: created.createdAt,
          updatedAt: created.updatedAt
        }
      });
      return;
    }

    const created = await createNotice({
      communityId,
      createdByUserId: user.id,
      title: parsed.data.title,
      message: parsed.data.message,
      status: parsed.data.published ? 'PUBLISHED' : 'DRAFT',
      startsAt: parsed.data.startsAt ?? null,
      endsAt: parsed.data.endsAt ?? null
    });

    res.status(201).json({
      data: {
        id: created.id,
        communityId: created.community_id,
        createdByUserId: created.created_by_user_id,
        title: created.title,
        message: created.message,
        status: created.status,
        startsAt: created.starts_at,
        endsAt: created.ends_at,
        createdAt: created.created_at,
        updatedAt: created.updated_at
      }
    });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/notices/:noticeId/publish', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const communityId = getParam(req.params.communityId);
    const noticeId = getParam(req.params.noticeId);
    const user = await resolveProtectedUser(req.user, communityId);

    if (!authorizeAction(user, communityId, 'PUBLISH_NOTICE')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have permission to publish notices.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const notice = notices.find((item) => item.id === noticeId && item.communityId === communityId);
      if (!notice) {
        throw new HttpError(404, 'NOT_FOUND', 'Notice not found.');
      }

      notice.status = 'PUBLISHED';
      res.json({
        data: {
          id: notice.id,
          communityId: notice.communityId,
          createdByUserId: user.id,
          title: notice.title,
          message: notice.message,
          status: notice.status,
          startsAt: null,
          endsAt: null,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
      });
      return;
    }

    const notice = await getNoticeById(communityId, noticeId);
    if (!notice) {
      throw new HttpError(404, 'NOT_FOUND', 'Notice not found.');
    }

    const updated = await publishNotice(communityId, noticeId);
    if (!updated) {
      throw new HttpError(404, 'NOT_FOUND', 'Notice not found.');
    }

    res.json({
      data: {
        id: updated.id,
        communityId: updated.community_id,
        createdByUserId: updated.created_by_user_id,
        title: updated.title,
        message: updated.message,
        status: updated.status,
        startsAt: updated.starts_at,
        endsAt: updated.ends_at,
        createdAt: updated.created_at,
        updatedAt: updated.updated_at
      }
    });
  } catch (error) {
    next(error);
  }
});

router.get('/api/v1/communities/:communityId/maintenance-requests', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const communityId = getParam(req.params.communityId);
    const user = await resolveProtectedUser(req.user, communityId);
    const canViewAll =
      authorizeAction(user, communityId, 'ASSIGN_WORK') ||
      authorizeAction(user, communityId, 'MANAGE_ASSETS') ||
      authorizeAction(user, communityId, 'MANAGE_CONTRACTS');

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const filtered = maintenanceRequests.filter((request) => request.communityId === communityId && (canViewAll || request.createdBy === user.id));
      res.json({
        data: filtered.map((request) => ({
          id: request.id,
          communityId: request.communityId,
          flatId: null,
          createdByUserId: request.createdBy,
          category: 'GENERAL',
          title: request.title,
          description: 'Maintenance request.',
          priority: 'MEDIUM',
          status: request.status,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }))
      });
      return;
    }

    const rows = await listMaintenanceRequestsForCommunity(communityId, {
      userId: canViewAll ? undefined : user.id,
      includeAll: canViewAll
    });

    res.json({
      data: rows.map((request) => ({
        id: request.id,
        communityId: request.community_id,
        flatId: request.flat_id,
        createdByUserId: request.created_by_user_id,
        category: request.category,
        title: request.title,
        description: request.description,
        priority: request.priority,
        status: request.status,
        createdAt: request.created_at,
        updatedAt: request.updated_at
      }))
    });
  } catch (error) {
    next(error);
  }
});

const maintenanceSchema = z.object({
  title: z.string().trim().min(1),
  description: z.string().trim().min(1),
  category: z.string().trim().min(1),
  flatId: z.string().uuid().optional(),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'URGENT']).default('MEDIUM')
});

router.post('/api/v1/communities/:communityId/maintenance-requests', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const communityId = getParam(req.params.communityId);
    const user = await resolveProtectedUser(req.user, communityId);

    if (!authorizeAction(user, communityId, 'CREATE_MAINTENANCE_REQUEST')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User is not allowed to create a maintenance request.');
    }

    const parsed = maintenanceSchema.safeParse(req.body);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', 'Maintenance request payload is invalid.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const created = {
        id: `mr-${Date.now()}`,
        communityId,
        createdBy: user.id,
        title: parsed.data.title,
        description: parsed.data.description,
        status: 'OPEN' as const
      };

      maintenanceRequests.push(created);
      res.status(201).json({
        data: {
          id: created.id,
          communityId: created.communityId,
          flatId: parsed.data.flatId ?? null,
          createdByUserId: created.createdBy,
          category: parsed.data.category,
          title: created.title,
          description: created.description,
          priority: parsed.data.priority,
          status: created.status,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
      });
      return;
    }

    const created = await createMaintenanceRequest({
      communityId,
      createdByUserId: user.id,
      category: parsed.data.category,
      title: parsed.data.title,
      description: parsed.data.description,
      priority: parsed.data.priority,
      flatId: parsed.data.flatId ?? null
    });

    res.status(201).json({
      data: {
        id: created.id,
        communityId: created.community_id,
        flatId: created.flat_id,
        createdByUserId: created.created_by_user_id,
        category: created.category,
        title: created.title,
        description: created.description,
        priority: created.priority,
        status: created.status,
        createdAt: created.created_at,
        updatedAt: created.updated_at
      }
    });
  } catch (error) {
    next(error);
  }
});

const visitorSchema = z.object({
  visitorName: z.string().trim().min(1),
  purpose: z.string().trim().min(1),
  expectedArrival: z.string().datetime({ offset: true }).optional(),
  flatId: z.string().uuid().optional(),
  hostUserId: z.string().uuid().optional()
});

router.get('/api/v1/communities/:communityId/visitors', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const resolvedUser = user;
      requireCommunityAccess(resolvedUser, communityId);
      if (!authorizeAction(resolvedUser, communityId, 'APPROVE_VISITOR')) {
        throw new HttpError(403, 'UNAUTHORIZED', 'User is not allowed to view visitors.');
      }
      res.json({ data: visitors.filter((visitor) => visitor.communityId === communityId) });
      return;
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    if (!authorizeAction(resolvedUser, communityId, 'APPROVE_VISITOR')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User is not allowed to view visitors.');
    }

    const rows = await listVisitorRequestsForCommunity(communityId);
    res.json({
      data: rows.map((visitor) => ({
        id: visitor.id,
        communityId: visitor.community_id,
        flatId: visitor.flat_id,
        hostUserId: visitor.host_user_id,
        visitorName: visitor.visitor_name,
        purpose: visitor.purpose,
        expectedArrival: visitor.expected_arrival,
        status: visitor.status,
        decisionByUserId: visitor.decision_by_user_id,
        decisionAt: visitor.decision_at,
        decisionReason: visitor.decision_reason,
        createdAt: visitor.created_at,
        updatedAt: visitor.updated_at
      }))
    });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/visitors', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    const parsed = visitorSchema.safeParse(req.body);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', 'Visitor payload is invalid.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const created = {
        id: `v-${Date.now()}`,
        communityId,
        visitorName: parsed.data.visitorName,
        purpose: parsed.data.purpose,
        flatId: parsed.data.flatId ?? 'flat-001',
        hostUserId: parsed.data.hostUserId ?? user.id,
        status: 'PENDING'
      };
      visitors.push(created);
      res.status(201).json({ data: created });
      return;
    }

    const created = await createVisitorRequest({
      communityId,
      hostUserId: parsed.data.hostUserId ?? resolvedUser.id,
      visitorName: parsed.data.visitorName,
      purpose: parsed.data.purpose,
      expectedArrival: parsed.data.expectedArrival ?? new Date(Date.now() + 60 * 60 * 1000).toISOString(),
      flatId: parsed.data.flatId ?? null
    });

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'visitor_request',
      resourceId: created.id,
      action: 'CREATED',
      details: { visitorName: created.visitor_name, purpose: created.purpose }
    });

    res.status(201).json({
      data: {
        id: created.id,
        communityId: created.community_id,
        flatId: created.flat_id,
        hostUserId: created.host_user_id,
        visitorName: created.visitor_name,
        purpose: created.purpose,
        expectedArrival: created.expected_arrival,
        status: created.status,
        decisionByUserId: created.decision_by_user_id,
        decisionAt: created.decision_at,
        decisionReason: created.decision_reason,
        createdAt: created.created_at,
        updatedAt: created.updated_at
      }
    });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/visitors/:id/approve', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const visitorId = getParam(req.params.id);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    if (!authorizeAction(resolvedUser, communityId, 'APPROVE_VISITOR')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have visitor approval permission.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const item = visitors.find((visitor) => visitor.id === visitorId && visitor.communityId === communityId);
      if (!item) {
        throw new HttpError(404, 'NOT_FOUND', 'Visitor request not found.');
      }
      item.status = 'APPROVED';
      res.json({ data: item });
      return;
    }

    const item = await getVisitorRequestById(communityId, visitorId);
    if (!item) {
      throw new HttpError(404, 'NOT_FOUND', 'Visitor request not found.');
    }

    const updated = await approveVisitorRequest(communityId, visitorId, resolvedUser.id, 'Approved by community manager');
    if (!updated) {
      throw new HttpError(409, 'CONFLICT', 'Visitor request is no longer pending.');
    }

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'visitor_request',
      resourceId: updated.id,
      action: 'APPROVED',
      details: { decisionReason: updated.decision_reason }
    });

    await createNotification({
      communityId,
      recipientUserId: updated.host_user_id,
      channel: 'IN_APP',
      subject: 'Visitor approval',
      message: `Visitor request ${updated.visitor_name} has been approved.`,
      payload: { requestId: updated.id, status: updated.status }
    });

    res.json({
      data: {
        id: updated.id,
        communityId: updated.community_id,
        flatId: updated.flat_id,
        hostUserId: updated.host_user_id,
        visitorName: updated.visitor_name,
        purpose: updated.purpose,
        expectedArrival: updated.expected_arrival,
        status: updated.status,
        decisionByUserId: updated.decision_by_user_id,
        decisionAt: updated.decision_at,
        decisionReason: updated.decision_reason,
        createdAt: updated.created_at,
        updatedAt: updated.updated_at
      }
    });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/visitors/:id/reject', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const visitorId = getParam(req.params.id);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    if (!authorizeAction(resolvedUser, communityId, 'APPROVE_VISITOR')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have visitor approval permission.');
    }

    const existing = await getVisitorRequestById(communityId, visitorId);
    if (!existing) {
      throw new HttpError(404, 'NOT_FOUND', 'Visitor request not found.');
    }

    const updated = await rejectVisitorRequest(communityId, visitorId, resolvedUser.id, 'Rejected by community manager');
    if (!updated) {
      throw new HttpError(409, 'CONFLICT', 'Visitor request is no longer pending.');
    }

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'visitor_request',
      resourceId: updated.id,
      action: 'REJECTED',
      details: { decisionReason: updated.decision_reason }
    });

    res.json({
      data: {
        id: updated.id,
        communityId: updated.community_id,
        flatId: updated.flat_id,
        hostUserId: updated.host_user_id,
        visitorName: updated.visitor_name,
        purpose: updated.purpose,
        expectedArrival: updated.expected_arrival,
        status: updated.status,
        decisionByUserId: updated.decision_by_user_id,
        decisionAt: updated.decision_at,
        decisionReason: updated.decision_reason,
        createdAt: updated.created_at,
        updatedAt: updated.updated_at
      }
    });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/visitors/:id/cancel', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const visitorId = getParam(req.params.id);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    const current = await getVisitorRequestById(communityId, visitorId);
    if (!current) {
      throw new HttpError(404, 'NOT_FOUND', 'Visitor request not found.');
    }

    if (current.host_user_id !== resolvedUser.id) {
      throw new HttpError(403, 'UNAUTHORIZED', 'Only the request owner can cancel a visitor request.');
    }

    const updated = await cancelVisitorRequest(communityId, visitorId, resolvedUser.id);
    if (!updated) {
      throw new HttpError(409, 'CONFLICT', 'Visitor request is no longer pending.');
    }

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'visitor_request',
      resourceId: updated.id,
      action: 'CANCELLED',
      details: { status: updated.status }
    });

    res.json({ data: { id: updated.id, status: updated.status } });
  } catch (error) {
    next(error);
  }
});

const billSchema = z.object({
  billType: z.string().trim().min(1),
  amount: z.string().trim().min(1),
  currency: z.string().trim().length(3).optional(),
  dueAt: z.string().datetime({ offset: true }),
  periodStart: z.string().datetime({ offset: true }),
  periodEnd: z.string().datetime({ offset: true }),
  flatId: z.union([z.string().uuid(), z.null()]).optional(),
  status: z.enum(['OPEN', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'VOID']).optional()
});

router.get('/api/v1/communities/:communityId/bills', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const rows = bills.filter((bill) => bill.communityId === communityId);
      res.json({ data: rows.map((bill) => ({ ...bill, billType: bill.title, amount: bill.amount, dueAt: new Date().toISOString(), periodStart: new Date().toISOString(), periodEnd: new Date(Date.now() + 86400000).toISOString(), currency: 'INR' })) });
      return;
    }

    const rows = await listBillsForCommunity(communityId);
    const data = await Promise.all(rows.map(async (row) => {
      const balance = await getOutstandingBalanceForBill(row.id);
      return {
        ...billResponseFromRow(row),
        outstandingAmount: balance.outstandingAmount,
        paidAmount: balance.paidAmount,
        totalAmount: balance.totalAmount
      };
    }));

    res.json({ data });
  } catch (error) {
    next(error);
  }
});

router.get('/api/v1/communities/:communityId/bills/:billId', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const billId = getParam(req.params.billId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const row = bills.find((bill) => bill.id === billId && bill.communityId === communityId);
      if (!row) {
        throw new HttpError(404, 'NOT_FOUND', 'Bill not found.');
      }
      res.json({ data: { ...row, billType: row.title, amount: row.amount, dueAt: new Date().toISOString(), periodStart: new Date().toISOString(), periodEnd: new Date(Date.now() + 86400000).toISOString(), currency: 'INR' } });
      return;
    }

    const row = await getBillById(communityId, billId);
    if (!row) {
      throw new HttpError(404, 'NOT_FOUND', 'Bill not found.');
    }

    const balance = await getOutstandingBalanceForBill(row.id);
    res.json({
      data: {
        ...billResponseFromRow(row),
        outstandingAmount: balance.outstandingAmount,
        paidAmount: balance.paidAmount,
        totalAmount: balance.totalAmount
      }
    });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/bills', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);
    if (!authorizeAction(resolvedUser, communityId, 'MANAGE_BILLS') && !authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have bill-management permission.');
    }

    const parsed = billSchema.safeParse(req.body);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', 'Bill payload is invalid.');
    }

    const amount = formatMoney(parsed.data.amount, 'amount');
    if (new Date(parsed.data.periodEnd).getTime() < new Date(parsed.data.periodStart).getTime()) {
      throw new HttpError(400, 'INVALID_INPUT', 'The bill period end cannot be before the period start.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const created = {
        id: `b-${Date.now()}`,
        communityId,
        title: parsed.data.billType,
        amount,
        status: parsed.data.status ?? 'OPEN'
      };
      bills.push(created);
      res.status(201).json({ data: { ...created, billType: created.title, dueAt: parsed.data.dueAt, periodStart: parsed.data.periodStart, periodEnd: parsed.data.periodEnd, currency: parsed.data.currency ?? 'INR', flatId: parsed.data.flatId ?? null, createdAt: new Date().toISOString() } });
      return;
    }

    const created = await createBill({
      communityId,
      billType: parsed.data.billType,
      amount,
      currency: parsed.data.currency ?? 'INR',
      dueAt: parsed.data.dueAt,
      periodStart: parsed.data.periodStart,
      periodEnd: parsed.data.periodEnd,
      flatId: parsed.data.flatId ?? null,
      status: parsed.data.status ?? 'OPEN'
    });

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'bill',
      resourceId: created.id,
      action: 'BILL_CREATED',
      details: { billType: created.bill_type, amount: created.amount, dueAt: created.due_at }
    });

    res.status(201).json({ data: billResponseFromRow(created) });
  } catch (error) {
    next(error);
  }
});

router.patch('/api/v1/communities/:communityId/bills/:billId', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const billId = getParam(req.params.billId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);
    if (!authorizeAction(resolvedUser, communityId, 'MANAGE_BILLS') && !authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have bill-management permission.');
    }

    const parsed = z.object({ status: z.enum(['OPEN', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'VOID']).optional(), dueAt: z.string().datetime({ offset: true }).optional() }).safeParse(req.body);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', 'Bill update payload is invalid.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const current = bills.find((bill) => bill.id === billId && bill.communityId === communityId);
      if (!current) {
        throw new HttpError(404, 'NOT_FOUND', 'Bill not found.');
      }
      current.status = parsed.data.status ?? current.status;
      if (parsed.data.dueAt) {
        current.dueAt = parsed.data.dueAt;
      }
      res.json({ data: { ...current, billType: current.title, amount: current.amount, currency: 'INR', periodStart: new Date().toISOString(), periodEnd: new Date(Date.now() + 86400000).toISOString() } });
      return;
    }

    const updated = await updateBillStatus(communityId, billId, parsed.data.status ?? 'OPEN', parsed.data.dueAt ?? null);
    if (!updated) {
      throw new HttpError(404, 'NOT_FOUND', 'Bill not found.');
    }

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'bill',
      resourceId: updated.id,
      action: 'BILL_UPDATED',
      details: { status: updated.status, dueAt: updated.due_at }
    });

    res.json({ data: billResponseFromRow(updated) });
  } catch (error) {
    next(error);
  }
});

router.get('/api/v1/communities/:communityId/payments/me', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      res.json({ data: payments.filter((payment) => payment.communityId === communityId && payment.payerUserId === resolvedUser.id) });
      return;
    }

    const rows = await listPaymentsForUser(resolvedUser.id, communityId);
    res.json({ data: rows.map((row) => paymentResponseFromRow(row)) });
  } catch (error) {
    next(error);
  }
});

router.get('/api/v1/communities/:communityId/payments/:paymentId', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const paymentId = getParam(req.params.paymentId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const row = payments.find((payment) => payment.id === paymentId && payment.communityId === communityId);
      if (!row) {
        throw new HttpError(404, 'NOT_FOUND', 'Payment record not found.');
      }
      if (!(await canReadPaymentRecord(resolvedUser, communityId, { payer_user_id: row.payerUserId, visibility: row.visibility }))) {
        throw new HttpError(403, 'UNAUTHORIZED', 'User is not authorized to view this financial record.');
      }
      res.json({ data: row });
      return;
    }

    const row = await getPaymentById(communityId, paymentId);
    if (!row) {
      throw new HttpError(404, 'NOT_FOUND', 'Payment record not found.');
    }

    const allowed = await canReadPaymentRecord(resolvedUser, communityId, { payer_user_id: row.payer_user_id, visibility: row.visibility });
    if (!allowed) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User is not authorized to view this financial record.');
    }

    res.json({ data: paymentResponseFromRow(row) });
  } catch (error) {
    next(error);
  }
});

const paymentSchema = z.object({
  billId: z.union([z.string().uuid(), z.string().min(1)]).nullable().optional(),
  payerUserId: z.union([z.string().uuid(), z.string().min(1)]).optional(),
  flatId: z.union([z.string().uuid(), z.null()]).optional(),
  amount: z.string().trim().min(1),
  currency: z.string().trim().length(3).optional(),
  paymentMethod: z.string().trim().min(1).optional(),
  reference: z.string().trim().min(1).optional(),
  evidenceRef: z.string().trim().min(1).optional(),
  visibility: z.enum(['PRIVATE_TO_OWNER', 'AUTHORIZED_HOUSEHOLD_ONLY', 'COMMUNITY_SUMMARY_ONLY', 'RESIDENT_VISIBLE', 'ADMIN_ONLY']).default('PRIVATE_TO_OWNER')
});

router.post('/api/v1/communities/:communityId/payments', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    const parsed = paymentSchema.safeParse(req.body);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', 'Payment payload is invalid.');
    }

    const payerUserId = parsed.data.payerUserId ?? resolvedUser.id;
    if (payerUserId !== resolvedUser.id && !authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'Only the payer or a finance approver may create this payment record.');
    }

    const amount = formatMoney(parsed.data.amount, 'amount');

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      if (parsed.data.billId) {
        const bill = bills.find((item) => item.id === parsed.data.billId && item.communityId === communityId);
        if (!bill) {
          throw new HttpError(404, 'NOT_FOUND', 'Bill not found.');
        }
      }

      const payment = {
        id: `p-${Date.now()}`,
        communityId,
        billId: parsed.data.billId ?? null,
        payerUserId,
        flatId: parsed.data.flatId ?? null,
        amount,
        currency: parsed.data.currency ?? 'INR',
        paymentMethod: parsed.data.paymentMethod ?? null,
        reference: parsed.data.reference ?? null,
        status: 'PENDING',
        visibility: parsed.data.visibility,
        evidenceRef: parsed.data.evidenceRef ?? null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        verifiedByUserId: null,
        verifiedAt: null,
        rejectedByUserId: null,
        rejectedAt: null,
        reversalReason: null
      };
      payments.push(payment);
      res.status(201).json({ data: payment });
      return;
    }

    if (parsed.data.billId) {
      const bill = await getBillById(communityId, parsed.data.billId);
      if (!bill) {
        throw new HttpError(404, 'NOT_FOUND', 'Bill not found.');
      }
    }

    const created = await createPayment({
      communityId,
      billId: parsed.data.billId ?? null,
      payerUserId,
      flatId: parsed.data.flatId ?? null,
      amount,
      currency: parsed.data.currency ?? 'INR',
      paymentMethod: parsed.data.paymentMethod ?? null,
      reference: parsed.data.reference ?? null,
      evidenceRef: parsed.data.evidenceRef ?? null,
      visibility: parsed.data.visibility
    });

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'payment',
      resourceId: created.id,
      action: 'PAYMENT_SUBMITTED',
      details: { amount: created.amount, payerUserId: created.payer_user_id, visibility: created.visibility }
    });

    res.status(201).json({ data: paymentResponseFromRow(created) });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/payments/:paymentId/verify', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const paymentId = getParam(req.params.paymentId);
    const reason = typeof req.body?.reason === 'string' ? req.body.reason : undefined;
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);
    if (!authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have finance approval permission.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const item = payments.find((payment) => payment.id === paymentId && payment.communityId === communityId);
      if (!item) {
        throw new HttpError(404, 'NOT_FOUND', 'Payment record not found.');
      }
      item.status = 'PAID';
      item.updatedAt = new Date().toISOString();
      res.json({ data: item });
      return;
    }

    const updated = await verifyPayment({ communityId, paymentId, verifiedByUserId: resolvedUser.id, reason });
    if (!updated) {
      throw new HttpError(404, 'NOT_FOUND', 'Payment record not found.');
    }

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'payment',
      resourceId: updated.id,
      action: 'PAYMENT_VERIFIED',
      details: { amount: updated.amount, status: updated.status }
    });

    const payerId = updated.payer_user_id;
    await createNotification({
      communityId,
      recipientUserId: payerId,
      channel: 'IN_APP',
      subject: 'Payment verified',
      message: 'Your payment has been verified and credited to the bill.',
      payload: { paymentId: updated.id, status: updated.status }
    });

    res.json({ data: paymentResponseFromRow(updated) });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/payments/:paymentId/reject', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const paymentId = getParam(req.params.paymentId);
    const reason = typeof req.body?.reason === 'string' ? req.body.reason : undefined;
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);
    if (!authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have finance approval permission.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const item = payments.find((payment) => payment.id === paymentId && payment.communityId === communityId);
      if (!item) {
        throw new HttpError(404, 'NOT_FOUND', 'Payment record not found.');
      }
      item.status = 'REJECTED';
      item.updatedAt = new Date().toISOString();
      res.json({ data: item });
      return;
    }

    const updated = await rejectPayment({ communityId, paymentId, rejectedByUserId: resolvedUser.id, reason });
    if (!updated) {
      throw new HttpError(404, 'NOT_FOUND', 'Payment record not found.');
    }

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'payment',
      resourceId: updated.id,
      action: 'PAYMENT_REJECTED',
      details: { amount: updated.amount, reason: reason ?? null }
    });

    res.json({ data: paymentResponseFromRow(updated) });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/payments/:paymentId/reverse', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const paymentId = getParam(req.params.paymentId);
    const reason = typeof req.body?.reason === 'string' ? req.body.reason : undefined;
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);
    if (!authorizeAction(resolvedUser, communityId, 'REVERSE_PAYMENT')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User does not have payment reversal permission.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const item = payments.find((payment) => payment.id === paymentId && payment.communityId === communityId);
      if (!item) {
        throw new HttpError(404, 'NOT_FOUND', 'Payment record not found.');
      }
      item.status = 'REVERSED';
      item.updatedAt = new Date().toISOString();
      res.json({ data: item });
      return;
    }

    const updated = await reversePayment({ communityId, paymentId, reversedByUserId: resolvedUser.id, reason });
    if (!updated) {
      throw new HttpError(404, 'NOT_FOUND', 'Payment record not found.');
    }

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'payment',
      resourceId: updated.id,
      action: 'PAYMENT_REVERSED',
      details: { amount: updated.amount, reason: reason ?? null }
    });

    res.json({ data: paymentResponseFromRow(updated) });
  } catch (error) {
    next(error);
  }
});

const householdAccessSchema = z.object({
  ownerUserId: z.string().uuid(),
  granteeUserId: z.string().uuid(),
  reason: z.string().trim().min(1).optional(),
  notes: z.string().trim().min(1).optional()
});

router.get('/api/v1/communities/:communityId/household-financial-access', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);
    if (!authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT') && !authorizeAction(resolvedUser, communityId, 'READ_FINANCIAL_SUMMARY')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'User is not allowed to manage household financial access.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const rows = householdAccessGrants.filter((grant) => grant.communityId === communityId && grant.ownerUserId === resolvedUser.id);
      res.json({ data: rows.map((grant) => ({ id: grant.id, communityId: grant.communityId, ownerUserId: grant.ownerUserId, granteeUserId: grant.granteeUserId, status: grant.status, reason: grant.reason, notes: grant.notes, grantedAt: grant.grantedAt, revokedAt: grant.revokedAt })) });
      return;
    }

    const rows = await listFinancialAccessGrantsForOwner(communityId, resolvedUser.id);
    res.json({ data: rows.map((row) => ({ id: row.id, communityId: row.community_id, ownerUserId: row.owner_user_id, granteeUserId: row.grantee_user_id, status: row.status, reason: row.reason, notes: row.notes, grantedAt: row.granted_at, revokedAt: row.revoked_at })) });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/household-financial-access', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    const parsed = householdAccessSchema.safeParse(req.body);
    if (!parsed.success) {
      throw new HttpError(400, 'INVALID_INPUT', 'Household financial access payload is invalid.');
    }

    if (parsed.data.ownerUserId !== resolvedUser.id && !authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'Only the owner or a finance approver may grant household access.');
    }

    if (parsed.data.ownerUserId === parsed.data.granteeUserId) {
      throw new HttpError(400, 'INVALID_INPUT', 'A user cannot grant access to themselves.');
    }

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const grant = {
        id: `grant-${Date.now()}`,
        communityId,
        ownerUserId: parsed.data.ownerUserId,
        granteeUserId: parsed.data.granteeUserId,
        grantedByUserId: resolvedUser.id,
        status: 'ACTIVE' as const,
        reason: parsed.data.reason ?? null,
        notes: parsed.data.notes ?? null,
        grantedAt: new Date().toISOString(),
        revokedAt: null
      };
      householdAccessGrants.push(grant);
      res.status(201).json({ data: { id: grant.id, communityId: grant.communityId, ownerUserId: grant.ownerUserId, granteeUserId: grant.granteeUserId, status: grant.status, reason: grant.reason, notes: grant.notes, grantedAt: grant.grantedAt, revokedAt: grant.revokedAt } });
      return;
    }

    const grant = await createFinancialAccessGrant({
      communityId,
      ownerUserId: parsed.data.ownerUserId,
      granteeUserId: parsed.data.granteeUserId,
      grantedByUserId: resolvedUser.id,
      reason: parsed.data.reason ?? null,
      notes: parsed.data.notes ?? null
    });

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'household_financial_access',
      resourceId: grant.id,
      action: 'GRANT_CREATED',
      details: { ownerUserId: grant.owner_user_id, granteeUserId: grant.grantee_user_id }
    });

    res.status(201).json({
      data: {
        id: grant.id,
        communityId: grant.community_id,
        ownerUserId: grant.owner_user_id,
        granteeUserId: grant.grantee_user_id,
        status: grant.status,
        reason: grant.reason,
        notes: grant.notes,
        grantedAt: grant.granted_at,
        revokedAt: grant.revoked_at
      }
    });
  } catch (error) {
    next(error);
  }
});

router.post('/api/v1/communities/:communityId/household-financial-access/:grantId/revoke', authenticate, async (req: AuthorizedRequest, res, next) => {
  try {
    const user = req.user;
    const communityId = getParam(req.params.communityId);
    const grantId = getParam(req.params.grantId);
    if (!user) {
      throw new HttpError(401, 'UNAUTHENTICATED', 'Authentication required.');
    }

    const resolvedUser = await resolveProtectedUser(user, communityId);
    requireCommunityAccess(resolvedUser, communityId);

    if (env.AUTH_MODE === 'test' && !isDatabaseConfigured()) {
      const current = householdAccessGrants.find((grant) => grant.id === grantId && grant.communityId === communityId);
      if (!current) {
        throw new HttpError(404, 'NOT_FOUND', 'Household access grant not found.');
      }
      if (current.ownerUserId !== resolvedUser.id && !authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT')) {
        throw new HttpError(403, 'UNAUTHORIZED', 'Only the owner or a finance approver may revoke household financial access.');
      }
      current.status = 'REVOKED';
      current.revokedAt = new Date().toISOString();
      res.json({ data: { id: current.id, status: current.status, revokedAt: current.revokedAt } });
      return;
    }

    const grant = await query<{ id: string; owner_user_id: string; grantee_user_id: string; community_id: string; status: string }>(
      `SELECT id, community_id, owner_user_id, grantee_user_id, status FROM household_financial_access WHERE community_id = $1 AND id = $2 LIMIT 1`,
      [communityId, grantId]
    );

    const current = grant.rows[0];
    if (!current) {
      throw new HttpError(404, 'NOT_FOUND', 'Household access grant not found.');
    }

    if (current.owner_user_id !== resolvedUser.id && !authorizeAction(resolvedUser, communityId, 'APPROVE_PAYMENT')) {
      throw new HttpError(403, 'UNAUTHORIZED', 'Only the owner or a finance approver may revoke household financial access.');
    }

    const revoked = await revokeFinancialAccessGrant({ communityId, grantId, revokedByUserId: resolvedUser.id, reason: 'Revoked by authorized staff' });
    if (!revoked) {
      throw new HttpError(404, 'NOT_FOUND', 'Household access grant not found.');
    }

    await logAuditEvent({
      communityId,
      actorUserId: resolvedUser.id,
      resourceType: 'household_financial_access',
      resourceId: revoked.id,
      action: 'GRANT_REVOKED',
      details: { ownerUserId: revoked.owner_user_id, granteeUserId: revoked.grantee_user_id }
    });

    res.json({ data: { id: revoked.id, status: revoked.status, revokedAt: revoked.revoked_at } });
  } catch (error) {
    next(error);
  }
});

export { router as apiRouter };
