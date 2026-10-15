package com.example.nammanivasa.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class VisitorRequest(
    val name: String,
    val purpose: String,
    val time: String,
    val apartment: String,
    val passCode: String,
    val status: String = "Pending"
)

data class BillItem(
    val id: String,
    val title: String,
    val amount: String,
    val due: String,
    val status: String
)

data class ServiceTicket(
    val title: String,
    val category: String,
    val status: String,
    val priority: String
)

data class EmergencyContact(
    val name: String,
    val role: String,
    val phone: String
)

data class DashboardTile(
    val title: String,
    val count: String,
    val subtitle: String,
    val color: Color,
    val icon: ImageVector
)

// --- New models for backend integration ---

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val status: String,
    val activeCommunities: List<CommunityMembershipInfo>,
    /**
     * Optional profile photo URL carried for future image loading. The UI has no
     * image-loading library wired up yet, so it always shows a typographic
     * monogram instead — never a fabricated photo — when null (or until the
     * field is actually rendered).
     */
    val photoUrl: String? = null,
    /** Building / tower label for the resident's unit, when known. */
    val buildingName: String? = null,
    /** Flat / unit number for the resident's unit, when known. */
    val flatNumber: String? = null
)

data class CommunityMembershipInfo(
    val communityId: String,
    val membershipStatus: String,
    val roles: List<String>
)

data class CommunityInfo(
    val id: String,
    val name: String,
    val status: String,
    val members: Int
)

/**
 * A discoverable community record from `/api/v1/communities/discover`.
 *
 * Deliberately carries no member / tower / flat / location fields: the backend
 * `communities` table has no such authoritative columns, and discovery results
 * must never present fabricated counts or addresses.
 */
data class CommunitySearchItem(
    val id: String,
    val name: String,
    val status: String
)

data class NoticeItem(
    val id: String,
    val communityId: String,
    val createdByUserId: String,
    val title: String,
    val message: String,
    val status: String,
    val startsAt: String?,
    val endsAt: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateNoticeRequest(
    val title: String,
    val message: String,
    val published: Boolean = false,
    val startsAt: String? = null,
    val endsAt: String? = null
)

data class MaintenanceRequestItem(
    val id: String,
    val communityId: String,
    val flatId: String?,
    val createdByUserId: String,
    val category: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class CreateMaintenanceRequestRequest(
    val title: String,
    val description: String,
    val category: String,
    val flatId: String? = null,
    val priority: String = "MEDIUM"
)

data class PaymentItem(
    val id: String,
    val communityId: String,
    val billId: String?,
    val payerUserId: String,
    val flatId: String?,
    val amount: String,
    val currency: String,
    val paymentMethod: String?,
    val reference: String?,
    val status: String,
    val visibility: String,
    val evidenceRef: String?,
    val createdAt: String,
    val updatedAt: String,
    val verifiedByUserId: String?,
    val verifiedAt: String?,
    val rejectedByUserId: String?,
    val rejectedAt: String?,
    val reversalReason: String?
)

data class CreatePaymentRequest(
    val billId: String? = null,
    val payerUserId: String? = null,
    val flatId: String? = null,
    val amount: String,
    val currency: String = "INR",
    val paymentMethod: String? = null,
    val reference: String? = null,
    val evidenceRef: String? = null,
    val visibility: String = "PRIVATE_TO_OWNER"
)

data class FinancialAccessGrantItem(
    val id: String,
    val communityId: String,
    val ownerUserId: String,
    val granteeUserId: String,
    val grantedByUserId: String,
    val status: String,
    val reason: String?,
    val notes: String?,
    val grantedAt: String,
    val revokedAt: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateFinancialAccessGrantRequest(
    val ownerUserId: String,
    val granteeUserId: String,
    val reason: String? = null,
    val notes: String? = null
)