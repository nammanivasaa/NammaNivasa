package com.example.nammanivasa.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.nammanivasa.data.api.ApiClient
import com.example.nammanivasa.data.api.CreateFinancialAccessGrantRequest
import com.example.nammanivasa.data.api.CreateMaintenanceRequestRequest
import com.example.nammanivasa.data.api.CreateNoticeRequest
import com.example.nammanivasa.data.api.CreatePaymentRequest
import com.example.nammanivasa.data.api.CreateVisitorRequestRequest
import com.example.nammanivasa.data.api.FinancialAccessGrantResponse
import com.example.nammanivasa.data.api.MaintenanceRequestResponse
import com.example.nammanivasa.data.api.NoticeResponse
import com.example.nammanivasa.data.api.PaymentResponse
import com.example.nammanivasa.data.api.RejectPaymentRequest
import com.example.nammanivasa.data.api.RevokeFinancialAccessGrantRequest
import com.example.nammanivasa.data.api.VerifyPaymentRequest
import com.example.nammanivasa.data.api.VisitorRequestResponse
import com.example.nammanivasa.data.api.BillResponse
import com.example.nammanivasa.data.model.BillItem
import com.example.nammanivasa.data.model.CommunityInfo
import com.example.nammanivasa.data.model.CommunityMembershipInfo
import com.example.nammanivasa.data.model.CommunitySearchItem
import com.example.nammanivasa.data.model.DashboardTile
import com.example.nammanivasa.data.model.EmergencyContact
import com.example.nammanivasa.data.model.FinancialAccessGrantItem
import com.example.nammanivasa.data.model.MaintenanceRequestItem
import com.example.nammanivasa.data.model.NoticeItem
import com.example.nammanivasa.data.model.PaymentItem
import com.example.nammanivasa.data.model.ServiceTicket
import com.example.nammanivasa.data.model.UserProfile
import com.example.nammanivasa.data.model.VisitorRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real repository implementation that calls the backend API.
 * This replaces the fake in-memory repository with actual network calls.
 */
class RealNammaNivasaRepository(private val context: Context) : NammaNivasaRepository {

    private val apiService = ApiClient.getApiService(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("NammaNivasaPrefs", Context.MODE_PRIVATE)
    private val ACTIVE_COMMUNITY_KEY = "active_community_id"

    /**
     * No dashboard-summary endpoint exists yet. This reports an explicit failure
     * rather than [RepoResult.Success] with a hardcoded, zeroed tile list: the
     * previous placeholder asserted backend counts ("0", "Waiting") the backend
     * never provided. The demo fallback in the hybrid layer still supplies
     * clearly-badged sample tiles when no backend is reachable.
     */
    override suspend fun dashboardTiles(): RepoResult<List<DashboardTile>> {
        return withContext(Dispatchers.IO) {
            RepoResult.Failure(RepoError.Server(501, "Dashboard summary is not available yet"))
        }
    }

    override suspend fun getUserProfile(): RepoResult<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMe()
                if (response.isSuccessful && response.body()?.data != null) {
                    val data = response.body()!!.data!!
                    RepoResult.Success(
                        UserProfile(
                            id = data.id,
                            fullName = data.fullName,
                            email = data.email,
                            phone = data.phone,
                            status = data.status,
                            activeCommunities = data.activeCommunities.map { membership ->
                                CommunityMembershipInfo(
                                    communityId = membership.communityId,
                                    membershipStatus = membership.membershipStatus,
                                    roles = membership.roles
                                )
                            }
                        )
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun getCommunities(): RepoResult<List<CommunityMembershipInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyCommunities()
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { membership ->
                            CommunityMembershipInfo(
                                communityId = membership.communityId,
                                membershipStatus = membership.membershipStatus,
                                roles = membership.roles
                            )
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun getCommunity(communityId: String): RepoResult<CommunityInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCommunity(communityId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val data = response.body()!!.data!!
                    RepoResult.Success(
                        CommunityInfo(
                            id = data.id,
                            name = data.name,
                            status = data.status,
                            members = data.members
                        )
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun discoverCommunities(query: String): RepoResult<List<CommunitySearchItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.discoverCommunities(query)
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { community ->
                            CommunitySearchItem(
                                id = community.id,
                                name = community.name,
                                status = community.status
                            )
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun setActiveCommunity(communityId: String) {
        prefs.edit().putString(ACTIVE_COMMUNITY_KEY, communityId).apply()
    }

    override suspend fun getActiveCommunityId(): String? {
        return prefs.getString(ACTIVE_COMMUNITY_KEY, null)
    }

    /**
     * Resolves the community to scope an API call to.
     *
     * This never substitutes a fallback community. When no active community has
     * been resolved, it throws [IllegalStateException] instead, so the call is
     * aborted rather than sent against an arbitrary community. The exception is
     * surfaced through the callers' existing error handling, which returns an
     * empty/failed result. Authorization and community membership remain the
     * server's decision; the server rejects any request whose community the
     * authenticated user does not belong to.
     */
    private suspend fun getPrimaryCommunityId(): String {
        return getActiveCommunityId()?.takeIf { it.isNotBlank() }
            ?: throw NoActiveCommunityException()
    }

    override suspend fun visitors(): RepoResult<List<VisitorRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.getVisitors(communityId)
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { apiVisitor ->
                            mapVisitorResponseToModel(apiVisitor)
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun approveVisitor(visitorId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.approveVisitor(communityId, visitorId)
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun rejectVisitor(visitorId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.rejectVisitor(communityId, visitorId)
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun createVisitorRequest(request: RepoCreateVisitorRequest): VisitorRequest? {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val apiRequest = CreateVisitorRequestRequest(
                    visitorName = request.visitorName,
                    purpose = request.purpose,
                    expectedArrival = request.expectedArrival,
                    flatId = request.flatId,
                    hostUserId = request.hostUserId
                )
                val response = apiService.createVisitorRequest(communityId, apiRequest)
                if (response.isSuccessful && response.body()?.data != null) {
                    mapVisitorResponseToModel(response.body()!!.data!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun cancelVisitor(visitorId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.cancelVisitor(communityId, visitorId)
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun notices(): RepoResult<List<NoticeItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.getNotices(communityId)
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { apiNotice ->
                            mapNoticeResponseToModel(apiNotice)
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun createNotice(request: RepoCreateNoticeRequest): NoticeItem? {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val apiRequest = CreateNoticeRequest(
                    title = request.title,
                    message = request.message,
                    published = request.published,
                    startsAt = request.startsAt,
                    endsAt = request.endsAt
                )
                val response = apiService.createNotice(communityId, apiRequest)
                if (response.isSuccessful && response.body()?.data != null) {
                    mapNoticeResponseToModel(response.body()!!.data!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun publishNotice(noticeId: String): NoticeItem? {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.publishNotice(communityId, noticeId)
                if (response.isSuccessful && response.body()?.data != null) {
                    mapNoticeResponseToModel(response.body()!!.data!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun maintenanceRequests(): RepoResult<List<MaintenanceRequestItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.getMaintenanceRequests(communityId)
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { apiTicket ->
                            mapMaintenanceResponseToModel(apiTicket)
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun createMaintenanceRequest(request: RepoCreateMaintenanceRequestRequest): MaintenanceRequestItem? {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val apiRequest = CreateMaintenanceRequestRequest(
                    title = request.title,
                    description = request.description,
                    category = request.category,
                    flatId = request.flatId,
                    priority = request.priority
                )
                val response = apiService.createMaintenanceRequest(communityId, apiRequest)
                if (response.isSuccessful && response.body()?.data != null) {
                    mapMaintenanceResponseToModel(response.body()!!.data!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Outstanding bills. A [RepoResult.Failure] here must never be read as "no dues
     * are outstanding" — callers surface the error instead of an empty ledger.
     */
    override suspend fun bills(): RepoResult<List<BillItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.getBills(communityId)
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { apiBill ->
                            mapBillResponseToModel(apiBill)
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun myPayments(): RepoResult<List<PaymentItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.getMyPayments(communityId)
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { apiPayment ->
                            mapPaymentResponseToModel(apiPayment)
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun createPayment(request: RepoCreatePaymentRequest): PaymentItem? {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val apiRequest = CreatePaymentRequest(
                    billId = request.billId,
                    payerUserId = request.payerUserId,
                    flatId = request.flatId,
                    amount = request.amount,
                    currency = request.currency,
                    paymentMethod = request.paymentMethod,
                    reference = request.reference,
                    evidenceRef = request.evidenceRef,
                    visibility = request.visibility
                )
                val response = apiService.createPayment(communityId, apiRequest)
                if (response.isSuccessful && response.body()?.data != null) {
                    mapPaymentResponseToModel(response.body()!!.data!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun verifyPayment(paymentId: String, reason: String?): PaymentItem? {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val apiRequest = VerifyPaymentRequest(reason = reason)
                val response = apiService.verifyPayment(communityId, paymentId, apiRequest)
                if (response.isSuccessful && response.body()?.data != null) {
                    mapPaymentResponseToModel(response.body()!!.data!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun rejectPayment(paymentId: String, reason: String?): PaymentItem? {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val apiRequest = RejectPaymentRequest(reason = reason)
                val response = apiService.rejectPayment(communityId, paymentId, apiRequest)
                if (response.isSuccessful && response.body()?.data != null) {
                    mapPaymentResponseToModel(response.body()!!.data!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun financialAccessGrants(): RepoResult<List<FinancialAccessGrantItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.getFinancialAccessGrants(communityId)
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { apiGrant ->
                            mapFinancialAccessGrantResponseToModel(apiGrant)
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    override suspend fun createFinancialAccessGrant(request: RepoCreateFinancialAccessGrantRequest): FinancialAccessGrantItem? {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val apiRequest = CreateFinancialAccessGrantRequest(
                    ownerUserId = request.ownerUserId,
                    granteeUserId = request.granteeUserId,
                    reason = request.reason,
                    notes = request.notes
                )
                val response = apiService.createFinancialAccessGrant(communityId, apiRequest)
                if (response.isSuccessful && response.body()?.data != null) {
                    mapFinancialAccessGrantResponseToModel(response.body()!!.data!!)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun revokeFinancialAccessGrant(grantId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val apiRequest = RevokeFinancialAccessGrantRequest(reason = null)
                val response = apiService.revokeFinancialAccessGrant(communityId, grantId, apiRequest)
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun tickets(): RepoResult<List<ServiceTicket>> {
        return withContext(Dispatchers.IO) {
            try {
                val communityId = getPrimaryCommunityId()
                val response = apiService.getMaintenanceRequests(communityId)
                if (response.isSuccessful && response.body()?.data != null) {
                    RepoResult.Success(
                        response.body()!!.data!!.map { apiTicket ->
                            mapMaintenanceResponseToServiceTicket(apiTicket)
                        }
                    )
                } else {
                    RepoResult.Failure(repoErrorFrom(response.code(), response.message()))
                }
            } catch (e: Exception) {
                RepoResult.Failure(repoErrorFrom(e))
            }
        }
    }

    /**
     * No emergency-contact endpoint exists yet, so this reports an explicit failure
     * rather than [RepoResult.Success] with an empty list. An empty list here would
     * assert to the user that the community has no emergency contacts, which is a
     * claim the backend has not made.
     */
    override suspend fun emergencyContacts(): RepoResult<List<EmergencyContact>> {
        return withContext(Dispatchers.IO) {
            RepoResult.Failure(RepoError.Server(501, "Emergency contacts are not available yet"))
        }
    }

    // --- Mapping functions ---

    private fun mapVisitorResponseToModel(api: VisitorRequestResponse): VisitorRequest {
        val status = when (api.status) {
            "APPROVED" -> "Approved"
            "REJECTED" -> "Rejected"
            "CANCELLED" -> "Cancelled"
            else -> "Pending"
        }
        val time = try {
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.getDefault()).parse(api.expectedArrival))
        } catch (e: Exception) {
            api.expectedArrival
        }
        return VisitorRequest(
            name = api.visitorName,
            purpose = api.purpose,
            time = time,
            apartment = api.flatId ?: "Unknown",
            passCode = api.id,
            status = status
        )
    }

    private fun mapNoticeResponseToModel(api: NoticeResponse): NoticeItem {
        return NoticeItem(
            id = api.id,
            communityId = api.communityId,
            createdByUserId = api.createdByUserId,
            title = api.title,
            message = api.message,
            status = api.status,
            startsAt = api.startsAt,
            endsAt = api.endsAt,
            createdAt = api.createdAt,
            updatedAt = api.updatedAt
        )
    }

    private fun mapMaintenanceResponseToModel(api: MaintenanceRequestResponse): MaintenanceRequestItem {
        return MaintenanceRequestItem(
            id = api.id,
            communityId = api.communityId,
            flatId = api.flatId,
            createdByUserId = api.createdByUserId,
            category = api.category,
            title = api.title,
            description = api.description,
            priority = api.priority,
            status = api.status,
            createdAt = api.createdAt,
            updatedAt = api.updatedAt
        )
    }

    private fun mapBillResponseToModel(api: BillResponse): BillItem {
        val status = when (api.status) {
            "PAID" -> "Paid"
            "PARTIALLY_PAID" -> "Partially Paid"
            "OVERDUE" -> "Overdue"
            "VOID" -> "Void"
            else -> "Pending"
        }
        val due = try {
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.getDefault()).parse(api.dueAt))
        } catch (e: Exception) {
            api.dueAt
        }
        return BillItem(
            id = api.id,
            title = api.billType,
            amount = "₹${api.amount}",
            due = due,
            status = status
        )
    }

    private fun mapPaymentResponseToModel(api: PaymentResponse): PaymentItem {
        return PaymentItem(
            id = api.id,
            communityId = api.communityId,
            billId = api.billId,
            payerUserId = api.payerUserId,
            flatId = api.flatId,
            amount = api.amount,
            currency = api.currency,
            paymentMethod = api.paymentMethod,
            reference = api.reference,
            status = api.status,
            visibility = api.visibility,
            evidenceRef = api.evidenceRef,
            createdAt = api.createdAt,
            updatedAt = api.updatedAt,
            verifiedByUserId = api.verifiedByUserId,
            verifiedAt = api.verifiedAt,
            rejectedByUserId = api.rejectedByUserId,
            rejectedAt = api.rejectedAt,
            reversalReason = api.reversalReason
        )
    }

    private fun mapFinancialAccessGrantResponseToModel(api: FinancialAccessGrantResponse): FinancialAccessGrantItem {
        return FinancialAccessGrantItem(
            id = api.id,
            communityId = api.communityId,
            ownerUserId = api.ownerUserId,
            granteeUserId = api.granteeUserId,
            grantedByUserId = api.grantedByUserId,
            status = api.status,
            reason = api.reason,
            notes = api.notes,
            grantedAt = api.grantedAt,
            revokedAt = api.revokedAt,
            createdAt = api.createdAt,
            updatedAt = api.updatedAt
        )
    }

    private fun mapMaintenanceResponseToServiceTicket(api: MaintenanceRequestResponse): ServiceTicket {
        return ServiceTicket(
            title = api.title,
            category = api.category,
            status = api.status.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() },
            priority = api.priority
        )
    }
}