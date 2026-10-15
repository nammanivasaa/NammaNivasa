package com.example.nammanivasa.data.repository

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

/**
 * Exposes whether a repository is currently serving local demo data instead of
 * verified backend data, so the UI can badge it honestly rather than presenting
 * fabricated content as real.
 */
interface DemoDataCapable {
    val usingDemoData: Boolean
}

/**
 * Backend-first repository with a local demo fallback.
 *
 * The app targets a real backend, but must stay usable on an emulator with no
 * backend running and before a backend account has been provisioned for the
 * signed-in user. This repository tries [remote] for every operation and falls
 * back to [local] (the in-memory demo store) when the backend cannot serve it:
 *
 *  - **Reads** fall back on any failure — offline, 401/403, server error — because
 *    a rejected or missing account is the expected state for a fresh install. The
 *    fallback never masks emptiness as failure or vice versa: within each source a
 *    completed read is still [RepoResult.Success] (possibly an empty list) and a
 *    failed read is still [RepoResult.Failure].
 *  - **Mutations** are attempted against the backend first; when the backend
 *    cannot complete the write, it is applied to the local demo store so the user's
 *    flow still finishes.
 *
 * Any fallback latches [usingDemoData] to true for the lifetime of this instance,
 * which the UI surfaces as a persistent "demo data" banner. The flag is never
 * silently cleared: once the app has shown demo data, it says so until restarted.
 *
 * [getActiveCommunityId] deliberately delegates only to [remote]. Returning the
 * demo store's community while the backend has none would scope later backend
 * reads to a community the user does not belong to; instead a fresh install asks
 * for a selection once (populated from the demo list) and the choice is persisted
 * in both stores via [setActiveCommunity].
 */
class HybridNammaNivasaRepository(
    private val remote: NammaNivasaRepository,
    private val local: NammaNivasaRepository
) : NammaNivasaRepository, DemoDataCapable {

    @Volatile
    override var usingDemoData: Boolean = false
        private set

    // --- Reads: remote first, demo fallback on any failure -------------------

    override suspend fun dashboardTiles(): RepoResult<List<DashboardTile>> =
        remote.dashboardTiles().orDemo { local.dashboardTiles() }

    override suspend fun getUserProfile(): RepoResult<UserProfile> =
        remote.getUserProfile().orDemo { local.getUserProfile() }

    override suspend fun getCommunities(): RepoResult<List<CommunityMembershipInfo>> =
        remote.getCommunities().orDemo { local.getCommunities() }

    override suspend fun getCommunity(communityId: String): RepoResult<CommunityInfo> =
        remote.getCommunity(communityId).orDemo { local.getCommunity(communityId) }

    override suspend fun discoverCommunities(query: String): RepoResult<List<CommunitySearchItem>> =
        remote.discoverCommunities(query).orDemo { local.discoverCommunities(query) }

    override suspend fun visitors(): RepoResult<List<VisitorRequest>> =
        remote.visitors().orDemo { local.visitors() }

    override suspend fun notices(): RepoResult<List<NoticeItem>> =
        remote.notices().orDemo { local.notices() }

    override suspend fun maintenanceRequests(): RepoResult<List<MaintenanceRequestItem>> =
        remote.maintenanceRequests().orDemo { local.maintenanceRequests() }

    override suspend fun bills(): RepoResult<List<BillItem>> =
        remote.bills().orDemo { local.bills() }

    override suspend fun myPayments(): RepoResult<List<PaymentItem>> =
        remote.myPayments().orDemo { local.myPayments() }

    override suspend fun financialAccessGrants(): RepoResult<List<FinancialAccessGrantItem>> =
        remote.financialAccessGrants().orDemo { local.financialAccessGrants() }

    override suspend fun tickets(): RepoResult<List<ServiceTicket>> =
        remote.tickets().orDemo { local.tickets() }

    override suspend fun emergencyContacts(): RepoResult<List<EmergencyContact>> =
        remote.emergencyContacts().orDemo { local.emergencyContacts() }

    // --- Community selection: persisted in both stores -----------------------

    override suspend fun setActiveCommunity(communityId: String) {
        remote.setActiveCommunity(communityId)
        local.setActiveCommunity(communityId)
    }

    override suspend fun getActiveCommunityId(): String? = remote.getActiveCommunityId()

    // --- Mutations: remote first, local demo write as fallback ---------------

    override suspend fun approveVisitor(visitorId: String): Boolean =
        remote.approveVisitor(visitorId).orDemoWrite { local.approveVisitor(visitorId) }

    override suspend fun rejectVisitor(visitorId: String): Boolean =
        remote.rejectVisitor(visitorId).orDemoWrite { local.rejectVisitor(visitorId) }

    override suspend fun createVisitorRequest(request: RepoCreateVisitorRequest): VisitorRequest? =
        remote.createVisitorRequest(request).orDemoWrite { local.createVisitorRequest(request) }

    override suspend fun cancelVisitor(visitorId: String): Boolean =
        remote.cancelVisitor(visitorId).orDemoWrite { local.cancelVisitor(visitorId) }

    override suspend fun createNotice(request: RepoCreateNoticeRequest): NoticeItem? =
        remote.createNotice(request).orDemoWrite { local.createNotice(request) }

    override suspend fun publishNotice(noticeId: String): NoticeItem? =
        remote.publishNotice(noticeId).orDemoWrite { local.publishNotice(noticeId) }

    override suspend fun createMaintenanceRequest(request: RepoCreateMaintenanceRequestRequest): MaintenanceRequestItem? =
        remote.createMaintenanceRequest(request).orDemoWrite { local.createMaintenanceRequest(request) }

    override suspend fun createPayment(request: RepoCreatePaymentRequest): PaymentItem? =
        remote.createPayment(request).orDemoWrite { local.createPayment(request) }

    override suspend fun verifyPayment(paymentId: String, reason: String?): PaymentItem? =
        remote.verifyPayment(paymentId, reason).orDemoWrite { local.verifyPayment(paymentId, reason) }

    override suspend fun rejectPayment(paymentId: String, reason: String?): PaymentItem? =
        remote.rejectPayment(paymentId, reason).orDemoWrite { local.rejectPayment(paymentId, reason) }

    override suspend fun createFinancialAccessGrant(request: RepoCreateFinancialAccessGrantRequest): FinancialAccessGrantItem? =
        remote.createFinancialAccessGrant(request).orDemoWrite { local.createFinancialAccessGrant(request) }

    override suspend fun revokeFinancialAccessGrant(grantId: String): Boolean =
        remote.revokeFinancialAccessGrant(grantId).orDemoWrite { local.revokeFinancialAccessGrant(grantId) }

    // --- Fallback plumbing ---------------------------------------------------

    /** Falls back to demo data on any read failure and latches the demo flag. */
    private suspend fun <T> RepoResult<T>.orDemo(fallback: suspend () -> RepoResult<T>): RepoResult<T> =
        when (this) {
            is RepoResult.Success -> this
            is RepoResult.Failure -> {
                usingDemoData = true
                fallback()
            }
        }

    /**
     * Falls back to the local demo store when the backend did not complete the
     * write (null for object results) and latches the demo flag, because the
     * user's change only exists locally from here on.
     */
    private suspend fun <T : Any> T?.orDemoWrite(fallback: suspend () -> T?): T? = this ?: run {
        usingDemoData = true
        fallback()
    }

    /** Boolean-result variant of [orDemoWrite]: false means "not completed". */
    private suspend fun Boolean.orDemoWrite(fallback: suspend () -> Boolean): Boolean = if (this) {
        true
    } else {
        usingDemoData = true
        fallback()
    }
}

