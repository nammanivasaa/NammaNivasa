package com.example.nammanivasa.domain

import com.example.nammanivasa.domain.model.Community
import com.example.nammanivasa.domain.model.CommunityMembership
import com.example.nammanivasa.domain.model.CommunityRoleAssignment
import com.example.nammanivasa.domain.model.MembershipStatus
import com.example.nammanivasa.domain.model.Occupancy
import com.example.nammanivasa.domain.model.OccupancyStatus
import com.example.nammanivasa.domain.model.OccupancyType
import com.example.nammanivasa.domain.model.PaymentRecord
import com.example.nammanivasa.domain.model.PaymentStatus
import com.example.nammanivasa.domain.model.PaymentVisibility
import com.example.nammanivasa.domain.model.PermissionCode
import com.example.nammanivasa.domain.model.PermissionCatalog
import com.example.nammanivasa.domain.model.PropertyOwnership
import com.example.nammanivasa.domain.model.OwnershipStatus
import com.example.nammanivasa.domain.model.RoleCode
import com.example.nammanivasa.domain.model.RoleDefinitions
import com.example.nammanivasa.domain.model.User
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelTest {
    @Test
    fun userValidationRejectsEmptyValues() {
        try {
            User(firebaseUid = "", fullName = "Asha")
            throw AssertionError("Expected IllegalArgumentException for blank firebaseUid")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("firebaseUid"))
        }
    }

    @Test
    fun communityMembershipRequiresRoleAssignment() {
        try {
            CommunityMembership(
                userId = "user-1",
                communityId = "community-1",
                roleIds = emptySet(),
                membershipStatus = MembershipStatus.ACTIVE
            )
            throw AssertionError("Expected IllegalArgumentException for empty roleIds")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("roleIds"))
        }
    }

    @Test
    fun membershipTransitionsAreValidated() {
        val membership = CommunityMembership(
            userId = "user-1",
            communityId = "community-1",
            roleIds = setOf("owner-role"),
            membershipStatus = MembershipStatus.PENDING
        )

        val active = membership.withStatus(MembershipStatus.ACTIVE)
        assertEquals(MembershipStatus.ACTIVE, active.membershipStatus)

        val suspended = active.withStatus(MembershipStatus.SUSPENDED)
        assertEquals(MembershipStatus.SUSPENDED, suspended.membershipStatus)

        val revoked = suspended.withStatus(MembershipStatus.REVOKED, "revoked by admin")
        assertEquals(MembershipStatus.REVOKED, revoked.membershipStatus)

        try {
            revoked.withStatus(MembershipStatus.ACTIVE)
            throw AssertionError("Expected invalid transition to be rejected")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Invalid membership transition"))
        }
    }

    @Test
    fun revokedOrSuspendedMembershipDoesNotGrantAccess() {
        val activeMembership = CommunityMembership(
            userId = "user-1",
            communityId = "community-1",
            roleIds = setOf("admin-role"),
            membershipStatus = MembershipStatus.ACTIVE
        )
        assertTrue(activeMembership.isAccessAllowed())

        val suspendedMembership = activeMembership.withStatus(MembershipStatus.SUSPENDED, "temporary hold")
        assertFalse(suspendedMembership.isAccessAllowed())

        val revokedMembership = activeMembership.withStatus(MembershipStatus.REVOKED, "revoked")
        assertFalse(revokedMembership.isAccessAllowed())
    }

    @Test
    fun ownershipAndOccupancyRemainSeparateConcepts() {
        val ownership = PropertyOwnership(
            communityId = "community-1",
            flatId = "flat-12",
            userId = "owner-1",
            ownershipType = com.example.nammanivasa.domain.model.OwnershipType.OWNER,
            ownershipShare = 0.5,
            status = OwnershipStatus.ACTIVE,
            startDate = Instant.parse("2024-01-01T00:00:00Z")
        )

        val occupancy = Occupancy(
            communityId = "community-1",
            flatId = "flat-12",
            userId = "tenant-1",
            occupancyType = OccupancyType.TENANT,
            status = OccupancyStatus.ACTIVE,
            startDate = Instant.parse("2024-02-01T00:00:00Z")
        )

        assertTrue(ownership.isActiveOn(Instant.parse("2024-06-01T00:00:00Z")))
        assertTrue(occupancy.isActiveOn(Instant.parse("2024-06-01T00:00:00Z")))
        assertFalse(ownership.userId == occupancy.userId)
    }

    @Test
    fun multipleOwnersAndOccupantsAreSupportedWhereApplicable() {
        val ownerA = PropertyOwnership(
            communityId = "community-1",
            flatId = "flat-34",
            userId = "owner-1",
            ownershipType = com.example.nammanivasa.domain.model.OwnershipType.OWNER,
            ownershipShare = 0.6
        )

        val ownerB = PropertyOwnership(
            communityId = "community-1",
            flatId = "flat-34",
            userId = "owner-2",
            ownershipType = com.example.nammanivasa.domain.model.OwnershipType.CO_OWNER,
            ownershipShare = 0.4
        )

        val occupantA = Occupancy(
            communityId = "community-1",
            flatId = "flat-34",
            userId = "user-10",
            occupancyType = OccupancyType.FAMILY_MEMBER,
            status = OccupancyStatus.ACTIVE
        )

        val occupantB = Occupancy(
            communityId = "community-1",
            flatId = "flat-34",
            userId = "user-11",
            occupancyType = OccupancyType.TENANT,
            status = OccupancyStatus.ACTIVE
        )

        assertTrue(ownerA.ownershipShare + ownerB.ownershipShare <= 1.0)
        assertTrue(occupantA.isActiveOn(Instant.now()))
        assertTrue(occupantB.isActiveOn(Instant.now()))
    }

    @Test
    fun roleAssignmentsAreScopedByCommunity() {
        val a = CommunityRoleAssignment(
            userId = "user-1",
            communityId = "community-a",
            roleId = "role-owner-a",
            status = com.example.nammanivasa.domain.model.RoleAssignmentStatus.ACTIVE
        )

        val b = CommunityRoleAssignment(
            userId = "user-1",
            communityId = "community-b",
            roleId = "role-admin-b",
            status = com.example.nammanivasa.domain.model.RoleAssignmentStatus.ACTIVE
        )

        assertTrue(a.isInCommunity("community-a"))
        assertTrue(b.isInCommunity("community-b"))
        assertFalse(a.isInCommunity("community-b"))
        assertFalse(b.isInCommunity("community-a"))
    }

    @Test
    fun communityScopedRolesDoNotCrossCommunityPermissionInheritance() {
        val roleA = RoleDefinitions.defaultRole(RoleCode.OWNER, "community-a")
        val roleB = RoleDefinitions.defaultRole(RoleCode.COMMUNITY_ADMINISTRATOR, "community-b")

        assertTrue(roleA.isInCommunity("community-a"))
        assertTrue(roleB.isInCommunity("community-b"))
        assertFalse(roleA.isInCommunity("community-b"))
        assertFalse(roleB.isInCommunity("community-a"))

        assertTrue(roleA.permissions.contains(PermissionCode.READ_OWN_PROPERTY))
        assertTrue(roleB.permissions.contains(PermissionCode.MANAGE_MEMBERSHIP))
        assertFalse(roleA.permissions.contains(PermissionCode.MANAGE_MEMBERSHIP))
        assertFalse(roleB.permissions.contains(PermissionCode.READ_OWN_PROPERTY))
    }

    @Test
    fun financialAccessBoundariesAreExplicit() {
        val privateRecord = PaymentRecord(
            communityId = "community-1",
            payerUserId = "owner-1",
            flatId = "flat-1",
            amount = 2400.0,
            status = PaymentStatus.PAID,
            visibility = PaymentVisibility.PRIVATE_TO_OWNER,
            explicitlyAuthorizedUserIds = setOf("member-1")
        )

        assertTrue(privateRecord.canBeViewedBy("owner-1", "community-1", setOf(RoleCode.OWNER)))
        assertFalse(privateRecord.canBeViewedBy("tenant-1", "community-1", setOf(RoleCode.TENANT)))

        val authorizedHouseholdRecord = PaymentRecord(
            communityId = "community-1",
            payerUserId = "owner-1",
            flatId = "flat-1",
            amount = 1200.0,
            status = PaymentStatus.PAID,
            visibility = PaymentVisibility.AUTHORIZED_HOUSEHOLD_ONLY,
            explicitlyAuthorizedUserIds = setOf("member-1")
        )

        assertTrue(authorizedHouseholdRecord.canBeViewedBy("member-1", "community-1", setOf(RoleCode.AUTHORIZED_HOUSEHOLD_MEMBER)))
        assertFalse(authorizedHouseholdRecord.canBeViewedBy("member-2", "community-1", setOf(RoleCode.AUTHORIZED_HOUSEHOLD_MEMBER)))

        val communitySummary = PaymentRecord(
            communityId = "community-1",
            payerUserId = "owner-1",
            flatId = "flat-1",
            amount = 5000.0,
            status = PaymentStatus.PAID,
            visibility = PaymentVisibility.COMMUNITY_SUMMARY_ONLY,
            explicitlyAuthorizedUserIds = emptySet()
        )

        assertTrue(communitySummary.canBeViewedBy("admin-1", "community-1", setOf(RoleCode.COMMUNITY_ADMINISTRATOR)))
        assertFalse(communitySummary.canBeViewedBy("tenant-1", "community-1", setOf(RoleCode.TENANT)))
    }

    @Test
    fun invalidIdentifiersAndLifecycleDatesAreRejected() {
        try {
            CommunityMembership(
                userId = "",
                communityId = "community-1",
                roleIds = setOf("owner-role"),
                membershipStatus = MembershipStatus.ACTIVE
            )
            throw AssertionError("Expected invalid userId to be rejected")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("userId"))
        }

        try {
            PropertyOwnership(
                communityId = "community-1",
                flatId = "flat-1",
                userId = "owner-1",
                ownershipShare = 1.5
            )
            throw AssertionError("Expected invalid ownershipShare to be rejected")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("ownershipShare"))
        }

        val invalidStart = Instant.parse("2024-03-01T00:00:00Z")
        val invalidEnd = Instant.parse("2024-02-01T00:00:00Z")

        try {
            Occupancy(
                communityId = "community-1",
                flatId = "flat-1",
                userId = "user-1",
                occupancyType = OccupancyType.TENANT,
                status = OccupancyStatus.ACTIVE,
                startDate = invalidStart,
                endDate = invalidEnd
            )
            throw AssertionError("Expected endDate validation to reject invalid ranges")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("endDate"))
        }
    }

    @Test
    fun permissionCatalogPreservesPhase2ABehavior() {
        val permission = PermissionCatalog.byCode(PermissionCode.MANAGE_BILLS)
        assertEquals(PermissionCode.MANAGE_BILLS, permission.code)
        assertTrue(permission.requiresApproval)
        assertTrue(permission.sensitive)

        val rolePermissions = RoleDefinitions.defaultPermissionsFor(RoleCode.SECURITY_GUARD)
        assertTrue(rolePermissions.contains(PermissionCode.APPROVE_VISITOR))
        assertTrue(rolePermissions.contains(PermissionCode.VIEW_SECURITY_LOGS))
    }

    @Test
    fun communityCanBeCreatedWithValidIdentifiers() {
        val community = Community(name = "Lakeview Residency", slug = "lakeview")
        assertEquals("lakeview", community.slug)
    }
}
