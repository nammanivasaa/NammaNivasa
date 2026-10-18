export type MembershipStatus =
  | 'PENDING'
  | 'ACTIVE'
  | 'SUSPENDED'
  | 'REVOKED'
  | 'EXPIRED';

export type PermissionCode =
  | 'READ_OWN_PROFILE'
  | 'READ_OWN_PROPERTY'
  | 'READ_COMMUNITY_NOTICES'
  | 'READ_COMMUNITY_DIRECTORY'
  | 'READ_FINANCIAL_SUMMARY'
  | 'READ_PERSONAL_FINANCIAL_RECORDS'
  | 'CREATE_MAINTENANCE_REQUEST'
  | 'UPDATE_OWN_PROFILE'
  | 'MANAGE_MEMBERSHIP'
  | 'MANAGE_COMMUNITY_ROLES'
  | 'MANAGE_NOTICES'
  | 'PUBLISH_NOTICE'
  | 'MANAGE_BILLS'
  | 'APPROVE_PAYMENT'
  | 'REVERSE_PAYMENT'
  | 'APPROVE_VISITOR'
  | 'MANAGE_ASSETS'
  | 'MANAGE_CONTRACTS'
  | 'ASSIGN_WORK'
  | 'VIEW_SECURITY_LOGS'
  | 'MANAGE_SECURITY_ACCESS'
  | 'ACCESS_AUDIT_LOGS'
  | 'EXECUTE_AI_ACTION'
  | 'APPROVE_ADMIN_ACTION';

export const ROLE_PERMISSIONS: Record<string, PermissionCode[]> = {
  OWNER: ['READ_OWN_PROFILE', 'READ_OWN_PROPERTY', 'READ_COMMUNITY_NOTICES', 'READ_FINANCIAL_SUMMARY', 'READ_PERSONAL_FINANCIAL_RECORDS', 'CREATE_MAINTENANCE_REQUEST', 'UPDATE_OWN_PROFILE', 'APPROVE_VISITOR'],
  TENANT: ['READ_OWN_PROFILE', 'READ_OWN_PROPERTY', 'READ_COMMUNITY_NOTICES', 'CREATE_MAINTENANCE_REQUEST', 'READ_PERSONAL_FINANCIAL_RECORDS'],
  AUTHORIZED_HOUSEHOLD_MEMBER: ['READ_OWN_PROFILE', 'READ_COMMUNITY_NOTICES', 'CREATE_MAINTENANCE_REQUEST', 'UPDATE_OWN_PROFILE'],
  COMMUNITY_ADMINISTRATOR: ['MANAGE_MEMBERSHIP', 'MANAGE_COMMUNITY_ROLES', 'MANAGE_NOTICES', 'PUBLISH_NOTICE', 'READ_COMMUNITY_DIRECTORY', 'APPROVE_ADMIN_ACTION'],
  COMMITTEE_MEMBER: ['READ_COMMUNITY_NOTICES', 'READ_COMMUNITY_DIRECTORY', 'MANAGE_NOTICES', 'PUBLISH_NOTICE'],
  TREASURER_FINANCE_MANAGER: ['READ_FINANCIAL_SUMMARY', 'MANAGE_BILLS', 'APPROVE_PAYMENT', 'REVERSE_PAYMENT', 'ACCESS_AUDIT_LOGS', 'APPROVE_ADMIN_ACTION'],
  MAINTENANCE_MANAGER: ['CREATE_MAINTENANCE_REQUEST', 'ASSIGN_WORK', 'MANAGE_ASSETS', 'MANAGE_CONTRACTS', 'READ_COMMUNITY_NOTICES'],
  SECURITY_GUARD: ['APPROVE_VISITOR', 'VIEW_SECURITY_LOGS', 'MANAGE_SECURITY_ACCESS'],
  FACILITY_MANAGER: ['MANAGE_ASSETS', 'MANAGE_CONTRACTS', 'ASSIGN_WORK', 'READ_COMMUNITY_NOTICES']
};

export interface UserMembershipSnapshot {
  communityId: string;
  status: MembershipStatus;
  roles: string[];
}

export interface AuthenticatedUser {
  id: string;
  firebaseUid: string;
  email?: string;
  fullName?: string;
  communities: Record<string, UserMembershipSnapshot>;
}

export function isMembershipActive(status: MembershipStatus): boolean {
  return status === 'ACTIVE';
}

export function getEffectivePermissions(roles: string[]): Set<PermissionCode> {
  const permissions = new Set<PermissionCode>();
  for (const role of roles) {
    for (const permission of ROLE_PERMISSIONS[role] ?? []) {
      permissions.add(permission as PermissionCode);
    }
  }
  return permissions;
}

export function canAccessCommunity(user: AuthenticatedUser, communityId: string): boolean {
  const membership = user.communities[communityId];
  return Boolean(membership && isMembershipActive(membership.status));
}

export function hasPermission(user: AuthenticatedUser, communityId: string, permission: PermissionCode): boolean {
  const membership = user.communities[communityId];
  if (!membership || !isMembershipActive(membership.status)) {
    return false;
  }

  const permissions = getEffectivePermissions(membership.roles);
  return permissions.has(permission);
}

export function authorizeAction(
  user: AuthenticatedUser,
  communityId: string,
  action: PermissionCode
): boolean {
  return canAccessCommunity(user, communityId) && hasPermission(user, communityId, action);
}

export function isCommunityScopedResource(resourceCommunityId: string | undefined, requestedCommunityId: string): boolean {
  return resourceCommunityId === undefined || resourceCommunityId === requestedCommunityId;
}

export function computeFinancialAccess(
  viewerUserId: string,
  payerUserId: string,
  authorizedUserIds: string[],
  visibility: 'PRIVATE_TO_OWNER' | 'AUTHORIZED_HOUSEHOLD_ONLY' | 'COMMUNITY_SUMMARY_ONLY' | 'RESIDENT_VISIBLE' | 'ADMIN_ONLY',
  roles: string[]
): boolean {
  if (viewerUserId === payerUserId) {
    return true;
  }

  if (visibility === 'PRIVATE_TO_OWNER') {
    return false;
  }

  if (visibility === 'AUTHORIZED_HOUSEHOLD_ONLY') {
    return authorizedUserIds.includes(viewerUserId);
  }

  if (visibility === 'COMMUNITY_SUMMARY_ONLY') {
    return roles.some((role) => ['TREASURER_FINANCE_MANAGER', 'COMMUNITY_ADMINISTRATOR', 'COMMITTEE_MEMBER'].includes(role));
  }

  if (visibility === 'RESIDENT_VISIBLE') {
    return roles.some((role) => ['OWNER', 'TENANT', 'AUTHORIZED_HOUSEHOLD_MEMBER'].includes(role));
  }

  return roles.some((role) => ['TREASURER_FINANCE_MANAGER', 'COMMUNITY_ADMINISTRATOR', 'FACILITY_MANAGER'].includes(role));
}
