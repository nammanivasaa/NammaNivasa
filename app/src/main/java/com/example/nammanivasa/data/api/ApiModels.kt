package com.example.nammanivasa.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Common response wrapper ---
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "data") val data: T?,
    @Json(name = "error") val error: ApiError?
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code") val code: String,
    @Json(name = "message") val message: String
)

// --- Auth / User ---
@JsonClass(generateAdapter = true)
data class MeResponse(
    @Json(name = "id") val id: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "email") val email: String?,
    @Json(name = "phone") val phone: String?,
    @Json(name = "status") val status: String,
    @Json(name = "activeCommunities") val activeCommunities: List<CommunityMembershipInfo>
)

@JsonClass(generateAdapter = true)
data class CommunityMembershipInfo(
    @Json(name = "communityId") val communityId: String,
    @Json(name = "membershipStatus") val membershipStatus: String,
    @Json(name = "roles") val roles: List<String>
)

@JsonClass(generateAdapter = true)
data class CommunityDetailResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String,
    @Json(name = "members") val members: Int
)

// --- Community discovery ---
@JsonClass(generateAdapter = true)
data class CommunitySearchResult(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String
)

// --- Notices ---
@JsonClass(generateAdapter = true)
data class NoticeResponse(
    @Json(name = "id") val id: String,
    @Json(name = "communityId") val communityId: String,
    @Json(name = "createdByUserId") val createdByUserId: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "status") val status: String,
    @Json(name = "startsAt") val startsAt: String?,
    @Json(name = "endsAt") val endsAt: String?,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateNoticeRequest(
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "published") val published: Boolean = false,
    @Json(name = "startsAt") val startsAt: String? = null,
    @Json(name = "endsAt") val endsAt: String? = null
)

// --- Maintenance Requests ---
@JsonClass(generateAdapter = true)
data class MaintenanceRequestResponse(
    @Json(name = "id") val id: String,
    @Json(name = "communityId") val communityId: String,
    @Json(name = "flatId") val flatId: String?,
    @Json(name = "createdByUserId") val createdByUserId: String,
    @Json(name = "category") val category: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "priority") val priority: String,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateMaintenanceRequestRequest(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "category") val category: String,
    @Json(name = "flatId") val flatId: String? = null,
    @Json(name = "priority") val priority: String = "MEDIUM"
)

// --- Visitors ---
@JsonClass(generateAdapter = true)
data class VisitorRequestResponse(
    @Json(name = "id") val id: String,
    @Json(name = "communityId") val communityId: String,
    @Json(name = "flatId") val flatId: String?,
    @Json(name = "hostUserId") val hostUserId: String,
    @Json(name = "visitorName") val visitorName: String,
    @Json(name = "purpose") val purpose: String,
    @Json(name = "expectedArrival") val expectedArrival: String,
    @Json(name = "status") val status: String,
    @Json(name = "decisionByUserId") val decisionByUserId: String?,
    @Json(name = "decisionAt") val decisionAt: String?,
    @Json(name = "decisionReason") val decisionReason: String?,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateVisitorRequestRequest(
    @Json(name = "visitorName") val visitorName: String,
    @Json(name = "purpose") val purpose: String,
    @Json(name = "expectedArrival") val expectedArrival: String? = null,
    @Json(name = "flatId") val flatId: String? = null,
    @Json(name = "hostUserId") val hostUserId: String? = null
)

// --- Bills ---
@JsonClass(generateAdapter = true)
data class BillResponse(
    @Json(name = "id") val id: String,
    @Json(name = "communityId") val communityId: String,
    @Json(name = "flatId") val flatId: String?,
    @Json(name = "billType") val billType: String,
    @Json(name = "amount") val amount: String,
    @Json(name = "currency") val currency: String,
    @Json(name = "periodStart") val periodStart: String,
    @Json(name = "periodEnd") val periodEnd: String,
    @Json(name = "dueAt") val dueAt: String,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "outstandingAmount") val outstandingAmount: String? = null,
    @Json(name = "paidAmount") val paidAmount: String? = null,
    @Json(name = "totalAmount") val totalAmount: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateBillRequest(
    @Json(name = "billType") val billType: String,
    @Json(name = "amount") val amount: String,
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "dueAt") val dueAt: String,
    @Json(name = "periodStart") val periodStart: String,
    @Json(name = "periodEnd") val periodEnd: String,
    @Json(name = "flatId") val flatId: String? = null,
    @Json(name = "status") val status: String = "OPEN"
)

@JsonClass(generateAdapter = true)
data class UpdateBillRequest(
    @Json(name = "status") val status: String? = null,
    @Json(name = "dueAt") val dueAt: String? = null
)

// --- Payments ---
@JsonClass(generateAdapter = true)
data class PaymentResponse(
    @Json(name = "id") val id: String,
    @Json(name = "communityId") val communityId: String,
    @Json(name = "billId") val billId: String?,
    @Json(name = "payerUserId") val payerUserId: String,
    @Json(name = "flatId") val flatId: String?,
    @Json(name = "amount") val amount: String,
    @Json(name = "currency") val currency: String,
    @Json(name = "paymentMethod") val paymentMethod: String?,
    @Json(name = "reference") val reference: String?,
    @Json(name = "status") val status: String,
    @Json(name = "visibility") val visibility: String,
    @Json(name = "evidenceRef") val evidenceRef: String?,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String,
    @Json(name = "verifiedByUserId") val verifiedByUserId: String?,
    @Json(name = "verifiedAt") val verifiedAt: String?,
    @Json(name = "rejectedByUserId") val rejectedByUserId: String?,
    @Json(name = "rejectedAt") val rejectedAt: String?,
    @Json(name = "reversalReason") val reversalReason: String?
)

@JsonClass(generateAdapter = true)
data class CreatePaymentRequest(
    @Json(name = "billId") val billId: String? = null,
    @Json(name = "payerUserId") val payerUserId: String? = null,
    @Json(name = "flatId") val flatId: String? = null,
    @Json(name = "amount") val amount: String,
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "paymentMethod") val paymentMethod: String? = null,
    @Json(name = "reference") val reference: String? = null,
    @Json(name = "evidenceRef") val evidenceRef: String? = null,
    @Json(name = "visibility") val visibility: String = "PRIVATE_TO_OWNER"
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentRequest(
    @Json(name = "reason") val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class RejectPaymentRequest(
    @Json(name = "reason") val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class ReversePaymentRequest(
    @Json(name = "reason") val reason: String? = null
)

// --- Household Financial Access ---
@JsonClass(generateAdapter = true)
data class FinancialAccessGrantResponse(
    @Json(name = "id") val id: String,
    @Json(name = "communityId") val communityId: String,
    @Json(name = "ownerUserId") val ownerUserId: String,
    @Json(name = "granteeUserId") val granteeUserId: String,
    @Json(name = "grantedByUserId") val grantedByUserId: String,
    @Json(name = "status") val status: String,
    @Json(name = "reason") val reason: String?,
    @Json(name = "notes") val notes: String?,
    @Json(name = "grantedAt") val grantedAt: String,
    @Json(name = "revokedAt") val revokedAt: String?,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateFinancialAccessGrantRequest(
    @Json(name = "ownerUserId") val ownerUserId: String,
    @Json(name = "granteeUserId") val granteeUserId: String,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class RevokeFinancialAccessGrantRequest(
    @Json(name = "reason") val reason: String? = null
)