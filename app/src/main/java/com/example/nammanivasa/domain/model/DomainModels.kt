package com.example.nammanivasa.domain.model

import java.time.Instant
import java.util.UUID

private const val EMPTY_MESSAGE = "must not be blank"

enum class UserStatus {
    ACTIVE,
    INVITED,
    SUSPENDED,
    DELETED
}

enum class CommunityStatus {
    ACTIVE,
    INACTIVE,
    ARCHIVED
}

enum class MembershipStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    REVOKED,
    EXPIRED
}

enum class OwnershipStatus {
    ACTIVE,
    INACTIVE,
    TRANSFERRED,
    REVOKED
}

enum class OwnershipType {
    OWNER,
    CO_OWNER,
    TRUST,
    LEGAL_ENTITY
}

enum class OccupancyType {
    OWNER,
    TENANT,
    FAMILY_MEMBER,
    STAFF
}

enum class OccupancyStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    EXPIRED
}

enum class RoleAssignmentStatus {
    ACTIVE,
    REVOKED,
    EXPIRED
}

enum class RoleCode {
    OWNER,
    TENANT,
    AUTHORIZED_HOUSEHOLD_MEMBER,
    COMMUNITY_ADMINISTRATOR,
    COMMITTEE_MEMBER,
    TREASURER_FINANCE_MANAGER,
    MAINTENANCE_MANAGER,
    SECURITY_GUARD,
    FACILITY_MANAGER
}

enum class PermissionCategory {
    PROFILE,
    COMMUNITY,
    FINANCE,
    MAINTENANCE,
    SECURITY,
    ASSET,
    ADMINISTRATION,
    AI
}

enum class PermissionCode {
    READ_OWN_PROFILE,
    READ_OWN_PROPERTY,
    READ_COMMUNITY_NOTICES,
    READ_COMMUNITY_DIRECTORY,
    READ_FINANCIAL_SUMMARY,
    READ_PERSONAL_FINANCIAL_RECORDS,
    CREATE_MAINTENANCE_REQUEST,
    UPDATE_OWN_PROFILE,
    MANAGE_MEMBERSHIP,
    MANAGE_COMMUNITY_ROLES,
    MANAGE_NOTICES,
    PUBLISH_NOTICE,
    MANAGE_BILLS,
    APPROVE_PAYMENT,
    REVERSE_PAYMENT,
    APPROVE_VISITOR,
    MANAGE_ASSETS,
    MANAGE_CONTRACTS,
    ASSIGN_WORK,
    VIEW_SECURITY_LOGS,
    MANAGE_SECURITY_ACCESS,
    ACCESS_AUDIT_LOGS,
    EXECUTE_AI_ACTION,
    APPROVE_ADMIN_ACTION
}

enum class PaymentVisibility {
    PRIVATE_TO_OWNER,
    AUTHORIZED_HOUSEHOLD_ONLY,
    COMMUNITY_SUMMARY_ONLY,
    RESIDENT_VISIBLE,
    ADMIN_ONLY
}

enum class PaymentStatus {
    PENDING,
    PAID,
    REJECTED,
    PARTIALLY_PAID,
    REVERSED
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val firebaseUid: String,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt
) {
    init {
        require(firebaseUid.isNotBlank()) { "firebaseUid $EMPTY_MESSAGE" }
        require(fullName.isNotBlank()) { "fullName $EMPTY_MESSAGE" }
        require(email == null || email.isNotBlank()) { "email must be blank or non-empty" }
        require(phone == null || phone.isNotBlank()) { "phone must be blank or non-empty" }
    }
}

data class Community(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val slug: String,
    val status: CommunityStatus = CommunityStatus.ACTIVE,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt
) {
    init {
        require(name.isNotBlank()) { "name $EMPTY_MESSAGE" }
        require(slug.isNotBlank()) { "slug $EMPTY_MESSAGE" }
    }
}

data class Building(
    val id: String = UUID.randomUUID().toString(),
    val communityId: String,
    val name: String,
    val blockCode: String,
    val floors: Int,
    val status: CommunityStatus = CommunityStatus.ACTIVE
) {
    init {
        require(communityId.isNotBlank()) { "communityId $EMPTY_MESSAGE" }
        require(name.isNotBlank()) { "name $EMPTY_MESSAGE" }
        require(blockCode.isNotBlank()) { "blockCode $EMPTY_MESSAGE" }
        require(floors > 0) { "floors must be positive" }
    }
}

data class Flat(
    val id: String = UUID.randomUUID().toString(),
    val communityId: String,
    val buildingId: String,
    val flatNumber: String,
    val unitType: String,
    val status: CommunityStatus = CommunityStatus.ACTIVE,
    val areaSqFt: Double? = null
) {
    init {
        require(communityId.isNotBlank()) { "communityId $EMPTY_MESSAGE" }
        require(buildingId.isNotBlank()) { "buildingId $EMPTY_MESSAGE" }
        require(flatNumber.isNotBlank()) { "flatNumber $EMPTY_MESSAGE" }
        require(unitType.isNotBlank()) { "unitType $EMPTY_MESSAGE" }
        require(areaSqFt == null || areaSqFt > 0.0) { "areaSqFt must be positive when present" }
    }
}

data class CommunityMembership(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val communityId: String,
    val membershipStatus: MembershipStatus = MembershipStatus.ACTIVE,
    val roleIds: Set<String> = emptySet(),
    val approvedByUserId: String? = null,
    val joinedAt: Instant = Instant.now(),
    val endedAt: Instant? = null,
    val statusUpdatedAt: Instant = joinedAt,
    val statusReason: String? = null
) {
    init {
        require(userId.isNotBlank()) { "userId $EMPTY_MESSAGE" }
        require(communityId.isNotBlank()) { "communityId $EMPTY_MESSAGE" }
        require(roleIds.isNotEmpty()) { "roleIds must contain at least one role" }
        require(endedAt == null || !endedAt.isBefore(joinedAt)) { "endedAt must be after joinedAt" }
        require(!statusUpdatedAt.isBefore(joinedAt)) { "statusUpdatedAt must not be before joinedAt" }
        require(membershipStatus != MembershipStatus.REVOKED || endedAt != null || statusReason != null) {
            "revoked memberships must include an end date or a reason"
        }
        requireValidTransitionForInitialStatus()
    }

    private fun requireValidTransitionForInitialStatus() {
        if (membershipStatus == MembershipStatus.ACTIVE) {
            return
        }
        if (membershipStatus == MembershipStatus.PENDING) {
            return
        }
        if (membershipStatus == MembershipStatus.SUSPENDED) {
            return
        }
        if (membershipStatus == MembershipStatus.REVOKED) {
            return
        }
        if (membershipStatus == MembershipStatus.EXPIRED) {
            return
        }
    }

    fun isAccessAllowed(): Boolean = membershipStatus == MembershipStatus.ACTIVE

    fun isCommunityScopedTo(scopeCommunityId: String): Boolean = communityId == scopeCommunityId

    fun withStatus(nextStatus: MembershipStatus, reason: String? = null): CommunityMembership {
        require(canTransition(membershipStatus, nextStatus)) {
            "Invalid membership transition: ${membershipStatus} -> $nextStatus"
        }
        val now = Instant.now()
        return copy(
            membershipStatus = nextStatus,
            statusUpdatedAt = now,
            statusReason = reason ?: statusReason,
            endedAt = when (nextStatus) {
                MembershipStatus.REVOKED, MembershipStatus.EXPIRED -> endedAt ?: now
                else -> endedAt
            }
        )
    }

    companion object {
        fun canTransition(from: MembershipStatus, to: MembershipStatus): Boolean = when (from) {
            MembershipStatus.PENDING -> to in setOf(MembershipStatus.ACTIVE, MembershipStatus.REVOKED, MembershipStatus.EXPIRED, MembershipStatus.SUSPENDED)
            MembershipStatus.ACTIVE -> to in setOf(MembershipStatus.SUSPENDED, MembershipStatus.REVOKED, MembershipStatus.EXPIRED)
            MembershipStatus.SUSPENDED -> to in setOf(MembershipStatus.ACTIVE, MembershipStatus.REVOKED, MembershipStatus.EXPIRED)
            MembershipStatus.REVOKED -> false
            MembershipStatus.EXPIRED -> false
        }
    }
}

data class PropertyOwnership(
    val id: String = UUID.randomUUID().toString(),
    val communityId: String,
    val flatId: String,
    val userId: String,
    val ownershipType: OwnershipType = OwnershipType.OWNER,
    val ownershipShare: Double = 1.0,
    val status: OwnershipStatus = OwnershipStatus.ACTIVE,
    val startDate: Instant = Instant.now(),
    val endDate: Instant? = null
) {
    init {
        require(communityId.isNotBlank()) { "communityId $EMPTY_MESSAGE" }
        require(flatId.isNotBlank()) { "flatId $EMPTY_MESSAGE" }
        require(userId.isNotBlank()) { "userId $EMPTY_MESSAGE" }
        require(ownershipShare > 0.0 && ownershipShare <= 1.0) { "ownershipShare must be between 0 and 1" }
        require(endDate == null || !endDate.isBefore(startDate)) { "endDate must be after startDate" }
        require(status != OwnershipStatus.REVOKED || endDate != null || status == OwnershipStatus.REVOKED) {
            "revoked ownership should have a valid end date"
        }
    }

    fun isActiveOn(date: Instant): Boolean = status == OwnershipStatus.ACTIVE &&
        !date.isBefore(startDate) &&
        (endDate == null || !date.isAfter(endDate))
}

data class Occupancy(
    val id: String = UUID.randomUUID().toString(),
    val communityId: String,
    val flatId: String,
    val userId: String,
    val occupancyType: OccupancyType = OccupancyType.OWNER,
    val status: OccupancyStatus = OccupancyStatus.ACTIVE,
    val startDate: Instant = Instant.now(),
    val endDate: Instant? = null
) {
    init {
        require(communityId.isNotBlank()) { "communityId $EMPTY_MESSAGE" }
        require(flatId.isNotBlank()) { "flatId $EMPTY_MESSAGE" }
        require(userId.isNotBlank()) { "userId $EMPTY_MESSAGE" }
        require(endDate == null || !endDate.isBefore(startDate)) { "endDate must be after startDate" }
    }

    fun isActiveOn(date: Instant): Boolean = status == OccupancyStatus.ACTIVE &&
        !date.isBefore(startDate) &&
        (endDate == null || !date.isAfter(endDate))
}

data class Role(
    val id: String = UUID.randomUUID().toString(),
    val communityId: String,
    val code: RoleCode,
    val label: String,
    val description: String,
    val permissions: Set<PermissionCode> = emptySet(),
    val isCustomizable: Boolean = true
) {
    init {
        require(communityId.isNotBlank()) { "communityId $EMPTY_MESSAGE" }
        require(label.isNotBlank()) { "label $EMPTY_MESSAGE" }
        require(description.isNotBlank()) { "description $EMPTY_MESSAGE" }
    }

    fun isInCommunity(scopeCommunityId: String): Boolean = communityId == scopeCommunityId
}

data class CommunityRoleAssignment(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val communityId: String,
    val roleId: String,
    val status: RoleAssignmentStatus = RoleAssignmentStatus.ACTIVE,
    val assignedAt: Instant = Instant.now(),
    val revokedAt: Instant? = null
) {
    init {
        require(userId.isNotBlank()) { "userId $EMPTY_MESSAGE" }
        require(communityId.isNotBlank()) { "communityId $EMPTY_MESSAGE" }
        require(roleId.isNotBlank()) { "roleId $EMPTY_MESSAGE" }
        require(revokedAt == null || !revokedAt.isBefore(assignedAt)) { "revokedAt must be after assignedAt" }
    }

    fun isActiveAt(date: Instant): Boolean = status == RoleAssignmentStatus.ACTIVE &&
        !date.isBefore(assignedAt) &&
        (revokedAt == null || !date.isAfter(revokedAt))

    fun isInCommunity(scopeCommunityId: String): Boolean = communityId == scopeCommunityId
}

data class Permission(
    val code: PermissionCode,
    val category: PermissionCategory,
    val label: String,
    val description: String,
    val sensitive: Boolean = false,
    val requiresApproval: Boolean = false,
    val supportsAudit: Boolean = true
) {
    init {
        require(label.isNotBlank()) { "label $EMPTY_MESSAGE" }
        require(description.isNotBlank()) { "description $EMPTY_MESSAGE" }
    }
}

object PermissionCatalog {
    fun all(): List<Permission> = listOf(
        Permission(PermissionCode.READ_OWN_PROFILE, PermissionCategory.PROFILE, "Read own profile", "View own resident profile", sensitive = false),
        Permission(PermissionCode.READ_OWN_PROPERTY, PermissionCategory.PROFILE, "Read property record", "Read own property and flat details", sensitive = false),
        Permission(PermissionCode.READ_COMMUNITY_NOTICES, PermissionCategory.COMMUNITY, "Read community notices", "View community notices and announcements", sensitive = false),
        Permission(PermissionCode.READ_COMMUNITY_DIRECTORY, PermissionCategory.COMMUNITY, "Read directory", "View resident contact and directory information", sensitive = true),
        Permission(PermissionCode.READ_FINANCIAL_SUMMARY, PermissionCategory.FINANCE, "Read financial summary", "View non-sensitive financial summary", sensitive = true),
        Permission(PermissionCode.READ_PERSONAL_FINANCIAL_RECORDS, PermissionCategory.FINANCE, "Read personal financial records", "Read own financial records and receipts", sensitive = true),
        Permission(PermissionCode.CREATE_MAINTENANCE_REQUEST, PermissionCategory.MAINTENANCE, "Create maintenance request", "Raise a new maintenance ticket", sensitive = false),
        Permission(PermissionCode.UPDATE_OWN_PROFILE, PermissionCategory.PROFILE, "Update own profile", "Edit own profile details", sensitive = false),
        Permission(PermissionCode.MANAGE_MEMBERSHIP, PermissionCategory.ADMINISTRATION, "Manage memberships", "Approve, revoke, or update community memberships", sensitive = true, requiresApproval = true),
        Permission(PermissionCode.MANAGE_COMMUNITY_ROLES, PermissionCategory.ADMINISTRATION, "Manage roles", "Manage role assignments and permissions", sensitive = true, requiresApproval = true),
        Permission(PermissionCode.MANAGE_NOTICES, PermissionCategory.COMMUNITY, "Manage notices", "Create or edit notices before publishing", sensitive = false),
        Permission(PermissionCode.PUBLISH_NOTICE, PermissionCategory.COMMUNITY, "Publish notice", "Publish a notice to the community", sensitive = false, requiresApproval = true),
        Permission(PermissionCode.MANAGE_BILLS, PermissionCategory.FINANCE, "Manage bills", "Create or update maintenance bills", sensitive = true, requiresApproval = true),
        Permission(PermissionCode.APPROVE_PAYMENT, PermissionCategory.FINANCE, "Approve payment", "Approve financial payment submissions", sensitive = true, requiresApproval = true),
        Permission(PermissionCode.REVERSE_PAYMENT, PermissionCategory.FINANCE, "Reverse payment", "Reverse a payment or resolve a dispute", sensitive = true, requiresApproval = true),
        Permission(PermissionCode.APPROVE_VISITOR, PermissionCategory.SECURITY, "Approve visitor", "Approve or deny visitor entry", sensitive = false),
        Permission(PermissionCode.MANAGE_ASSETS, PermissionCategory.ASSET, "Manage assets", "Create or update asset records", sensitive = false),
        Permission(PermissionCode.MANAGE_CONTRACTS, PermissionCategory.ASSET, "Manage contracts", "Manage AMC and service contracts", sensitive = true, requiresApproval = true),
        Permission(PermissionCode.ASSIGN_WORK, PermissionCategory.MAINTENANCE, "Assign work", "Assign work orders and maintenance tasks", sensitive = false),
        Permission(PermissionCode.VIEW_SECURITY_LOGS, PermissionCategory.SECURITY, "View security logs", "Read security and access logs", sensitive = true),
        Permission(PermissionCode.MANAGE_SECURITY_ACCESS, PermissionCategory.SECURITY, "Manage security access", "Grant or revoke visitor or gate access", sensitive = true, requiresApproval = true),
        Permission(PermissionCode.ACCESS_AUDIT_LOGS, PermissionCategory.ADMINISTRATION, "Access audit logs", "Read audit history for admin actions", sensitive = true),
        Permission(PermissionCode.EXECUTE_AI_ACTION, PermissionCategory.AI, "Execute AI action", "Run authorized AI actions from handled requests", sensitive = true, requiresApproval = true),
        Permission(PermissionCode.APPROVE_ADMIN_ACTION, PermissionCategory.ADMINISTRATION, "Approve admin action", "Approve sensitive admin actions", sensitive = true, requiresApproval = true)
    )

    fun byCode(code: PermissionCode): Permission = all().first { it.code == code }
}

object RoleDefinitions {
    fun defaultPermissionsFor(code: RoleCode): Set<PermissionCode> = when (code) {
        RoleCode.OWNER -> setOf(
            PermissionCode.READ_OWN_PROFILE,
            PermissionCode.READ_OWN_PROPERTY,
            PermissionCode.READ_COMMUNITY_NOTICES,
            PermissionCode.READ_FINANCIAL_SUMMARY,
            PermissionCode.READ_PERSONAL_FINANCIAL_RECORDS,
            PermissionCode.CREATE_MAINTENANCE_REQUEST,
            PermissionCode.UPDATE_OWN_PROFILE,
            PermissionCode.APPROVE_VISITOR
        )
        RoleCode.TENANT -> setOf(
            PermissionCode.READ_OWN_PROFILE,
            PermissionCode.READ_OWN_PROPERTY,
            PermissionCode.READ_COMMUNITY_NOTICES,
            PermissionCode.CREATE_MAINTENANCE_REQUEST,
            PermissionCode.READ_PERSONAL_FINANCIAL_RECORDS
        )
        RoleCode.AUTHORIZED_HOUSEHOLD_MEMBER -> setOf(
            PermissionCode.READ_OWN_PROFILE,
            PermissionCode.READ_COMMUNITY_NOTICES,
            PermissionCode.CREATE_MAINTENANCE_REQUEST,
            PermissionCode.UPDATE_OWN_PROFILE
        )
        RoleCode.COMMUNITY_ADMINISTRATOR -> setOf(
            PermissionCode.MANAGE_MEMBERSHIP,
            PermissionCode.MANAGE_COMMUNITY_ROLES,
            PermissionCode.MANAGE_NOTICES,
            PermissionCode.PUBLISH_NOTICE,
            PermissionCode.READ_COMMUNITY_DIRECTORY,
            PermissionCode.APPROVE_ADMIN_ACTION
        )
        RoleCode.COMMITTEE_MEMBER -> setOf(
            PermissionCode.READ_COMMUNITY_NOTICES,
            PermissionCode.READ_COMMUNITY_DIRECTORY,
            PermissionCode.MANAGE_NOTICES,
            PermissionCode.PUBLISH_NOTICE
        )
        RoleCode.TREASURER_FINANCE_MANAGER -> setOf(
            PermissionCode.READ_FINANCIAL_SUMMARY,
            PermissionCode.MANAGE_BILLS,
            PermissionCode.APPROVE_PAYMENT,
            PermissionCode.REVERSE_PAYMENT,
            PermissionCode.ACCESS_AUDIT_LOGS,
            PermissionCode.APPROVE_ADMIN_ACTION
        )
        RoleCode.MAINTENANCE_MANAGER -> setOf(
            PermissionCode.CREATE_MAINTENANCE_REQUEST,
            PermissionCode.ASSIGN_WORK,
            PermissionCode.MANAGE_ASSETS,
            PermissionCode.MANAGE_CONTRACTS,
            PermissionCode.READ_COMMUNITY_NOTICES
        )
        RoleCode.SECURITY_GUARD -> setOf(
            PermissionCode.APPROVE_VISITOR,
            PermissionCode.VIEW_SECURITY_LOGS,
            PermissionCode.MANAGE_SECURITY_ACCESS
        )
        RoleCode.FACILITY_MANAGER -> setOf(
            PermissionCode.MANAGE_ASSETS,
            PermissionCode.MANAGE_CONTRACTS,
            PermissionCode.ASSIGN_WORK,
            PermissionCode.READ_COMMUNITY_NOTICES
        )
    }

    fun defaultRole(code: RoleCode, communityId: String): Role = Role(
        communityId = communityId,
        code = code,
        label = code.name.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() },
        description = "Default role definition for ${code.name.lowercase().replace('_', ' ')}.",
        permissions = defaultPermissionsFor(code)
    )
}

data class PaymentRecord(
    val id: String = UUID.randomUUID().toString(),
    val communityId: String,
    val payerUserId: String,
    val flatId: String? = null,
    val amount: Double,
    val status: PaymentStatus = PaymentStatus.PAID,
    val visibility: PaymentVisibility = PaymentVisibility.PRIVATE_TO_OWNER,
    val explicitlyAuthorizedUserIds: Set<String> = emptySet(),
    val financeApproverUserId: String? = null
) {
    init {
        require(communityId.isNotBlank()) { "communityId $EMPTY_MESSAGE" }
        require(payerUserId.isNotBlank()) { "payerUserId $EMPTY_MESSAGE" }
        require(amount >= 0.0) { "amount must be non-negative" }
        require(flatId == null || flatId.isNotBlank()) { "flatId must be blank or non-empty" }
        require(
            financeApproverUserId == null || financeApproverUserId.isNotBlank()
        ) { "financeApproverUserId must be blank or non-empty" }
    }

    fun canBeViewedBy(
        viewerUserId: String,
        viewerCommunityId: String,
        viewerRoles: Set<RoleCode>
    ): Boolean {
        if (viewerCommunityId != communityId) {
            return false
        }
        if (viewerUserId == payerUserId) {
            return true
        }

        val hasFinanceAccess = viewerRoles.any {
            it == RoleCode.TREASURER_FINANCE_MANAGER ||
                it == RoleCode.COMMUNITY_ADMINISTRATOR ||
                it == RoleCode.COMMITTEE_MEMBER
        }
        val hasResidentAccess = viewerRoles.any {
            it == RoleCode.OWNER || it == RoleCode.TENANT || it == RoleCode.AUTHORIZED_HOUSEHOLD_MEMBER
        }
        val hasAdminAccess = viewerRoles.any {
            it == RoleCode.TREASURER_FINANCE_MANAGER ||
                it == RoleCode.COMMUNITY_ADMINISTRATOR ||
                it == RoleCode.FACILITY_MANAGER
        }

        return when (visibility) {
            PaymentVisibility.PRIVATE_TO_OWNER -> false
            PaymentVisibility.AUTHORIZED_HOUSEHOLD_ONLY -> viewerUserId in explicitlyAuthorizedUserIds
            PaymentVisibility.COMMUNITY_SUMMARY_ONLY -> hasFinanceAccess
            PaymentVisibility.RESIDENT_VISIBLE -> hasResidentAccess
            PaymentVisibility.ADMIN_ONLY -> hasAdminAccess
        }
    }
}
