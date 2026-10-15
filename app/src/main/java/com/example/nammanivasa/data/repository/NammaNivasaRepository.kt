package com.example.nammanivasa.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Builds an ISO-8601 UTC timestamp for a moment [daysAhead] days from now at the
 * given local clock time. Used only to give the in-memory mock notices realistic,
 * relative schedules; this is fixture data, not verified backend data.
 */
private fun fixtureIso(daysAhead: Int, hourOfDay: Int, minute: Int = 0): String {
    val moment = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, daysAhead)
    }
    val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return utcFormat.format(moment.time)
}

/**
 * Read methods return [RepoResult] so a completed-but-empty response can never be
 * confused with a failed one. Mutations keep their existing nullable/Boolean
 * results: the ViewModel already reports those as an explicit failure, so they do
 * not risk being presented as successful empty data.
 */
interface CommunityRepository {
    suspend fun dashboardTiles(): RepoResult<List<DashboardTile>>
    suspend fun getUserProfile(): RepoResult<UserProfile>
    suspend fun getCommunities(): RepoResult<List<CommunityMembershipInfo>>
    suspend fun getCommunity(communityId: String): RepoResult<CommunityInfo>

    /**
     * Discovers ACTIVE communities for the signed-in user, independent of
     * membership — this is how the join flow finds communities the user does
     * not belong to yet. An empty [query] returns the full (result-capped)
     * discoverable list. Selecting a result grants no membership or access.
     */
    suspend fun discoverCommunities(query: String): RepoResult<List<CommunitySearchItem>>
    suspend fun setActiveCommunity(communityId: String)
    suspend fun getActiveCommunityId(): String?
}

interface VisitorRepository {
    suspend fun visitors(): RepoResult<List<VisitorRequest>>
    suspend fun approveVisitor(visitorId: String): Boolean
    suspend fun rejectVisitor(visitorId: String): Boolean
    suspend fun createVisitorRequest(request: RepoCreateVisitorRequest): VisitorRequest?
    suspend fun cancelVisitor(visitorId: String): Boolean
}

data class RepoCreateVisitorRequest(
    val visitorName: String,
    val purpose: String,
    val expectedArrival: String? = null,
    val flatId: String? = null,
    val hostUserId: String? = null
)

interface NoticeRepository {
    suspend fun notices(): RepoResult<List<NoticeItem>>
    suspend fun createNotice(request: RepoCreateNoticeRequest): NoticeItem?
    suspend fun publishNotice(noticeId: String): NoticeItem?
}

data class RepoCreateNoticeRequest(
    val title: String,
    val message: String,
    val published: Boolean = false,
    val startsAt: String? = null,
    val endsAt: String? = null
)

interface MaintenanceRepository {
    suspend fun maintenanceRequests(): RepoResult<List<MaintenanceRequestItem>>
    suspend fun createMaintenanceRequest(request: RepoCreateMaintenanceRequestRequest): MaintenanceRequestItem?
}

data class RepoCreateMaintenanceRequestRequest(
    val title: String,
    val description: String,
    val category: String,
    val flatId: String? = null,
    val priority: String = "MEDIUM"
)

interface PaymentRepository {
    suspend fun bills(): RepoResult<List<BillItem>>
    suspend fun myPayments(): RepoResult<List<PaymentItem>>
    suspend fun createPayment(request: RepoCreatePaymentRequest): PaymentItem?
    suspend fun verifyPayment(paymentId: String, reason: String?): PaymentItem?
    suspend fun rejectPayment(paymentId: String, reason: String?): PaymentItem?
}

data class RepoCreatePaymentRequest(
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

interface FinancialAccessRepository {
    suspend fun financialAccessGrants(): RepoResult<List<FinancialAccessGrantItem>>
    suspend fun createFinancialAccessGrant(request: RepoCreateFinancialAccessGrantRequest): FinancialAccessGrantItem?
    suspend fun revokeFinancialAccessGrant(grantId: String): Boolean
}

data class RepoCreateFinancialAccessGrantRequest(
    val ownerUserId: String,
    val granteeUserId: String,
    val reason: String? = null,
    val notes: String? = null
)

interface ServiceRepository {
    suspend fun tickets(): RepoResult<List<ServiceTicket>>
    suspend fun emergencyContacts(): RepoResult<List<EmergencyContact>>
}

interface NammaNivasaRepository : CommunityRepository, VisitorRepository, NoticeRepository, MaintenanceRepository, PaymentRepository, FinancialAccessRepository, ServiceRepository

/**
 * Sample in-memory repository used by the resident prototype screens.
 * This is not production data and should not be treated as verified backend data.
 */
class FakeNammaNivasaRepository : NammaNivasaRepository {
    private val visitors = mutableListOf(
        VisitorRequest("Arihant Sharma", "Family Visit", "09:15 AM", "B-204", "NV-1042"),
        VisitorRequest("Meena Iyer", "Delivery", "10:40 AM", "C-118", "NV-1048"),
        VisitorRequest("Rohit Nair", "Guest", "11:15 AM", "A-705", "NV-1051")
    )

    private val notices = mutableListOf(
        NoticeItem(
            id = "n-1",
            communityId = "22222222-2222-4222-8222-222222222222",
            createdByUserId = "user-1",
            title = "Water tank maintenance",
            message = "Tank cleaning scheduled for Sunday.",
            status = "PUBLISHED",
            startsAt = fixtureIso(daysAhead = 3, hourOfDay = 9),
            endsAt = fixtureIso(daysAhead = 3, hourOfDay = 12),
            createdAt = "2024-01-15T10:00:00Z",
            updatedAt = "2024-01-15T10:00:00Z"
        ),
        NoticeItem(
            id = "n-2",
            communityId = "22222222-2222-4222-8222-222222222222",
            createdByUserId = "user-1",
            title = "Lift 2 inspection",
            message = "Lift inspection after 4 pm.",
            status = "PUBLISHED",
            startsAt = fixtureIso(daysAhead = 6, hourOfDay = 16),
            endsAt = fixtureIso(daysAhead = 6, hourOfDay = 17),
            createdAt = "2024-01-10T14:00:00Z",
            updatedAt = "2024-01-10T14:00:00Z"
        ),
        NoticeItem(
            id = "n-3",
            communityId = "22222222-2222-4222-8222-222222222222",
            createdByUserId = "user-1",
            title = "Residents' general meeting",
            message = "Monthly meeting to review open society matters.",
            status = "PUBLISHED",
            startsAt = fixtureIso(daysAhead = 10, hourOfDay = 18, minute = 30),
            endsAt = fixtureIso(daysAhead = 10, hourOfDay = 19, minute = 30),
            createdAt = "2024-01-08T09:00:00Z",
            updatedAt = "2024-01-08T09:00:00Z"
        )
    )

    private val maintenanceRequests = mutableListOf(
        MaintenanceRequestItem(
            id = "mr-1",
            communityId = "22222222-2222-4222-8222-222222222222",
            flatId = "flat-1",
            createdByUserId = "user-1",
            category = "PLUMBING",
            title = "Leak at kitchen sink",
            description = "Water keeps dripping on the cabinet",
            priority = "HIGH",
            status = "OPEN",
            createdAt = "2024-01-15T09:00:00Z",
            updatedAt = "2024-01-15T09:00:00Z"
        ),
        MaintenanceRequestItem(
            id = "mr-2",
            communityId = "22222222-2222-4222-8222-222222222222",
            flatId = "flat-2",
            createdByUserId = "user-2",
            category = "ELECTRICAL",
            title = "Pool pump noise",
            description = "Pool pump making loud noise",
            priority = "MEDIUM",
            status = "IN_PROGRESS",
            createdAt = "2024-01-14T10:00:00Z",
            updatedAt = "2024-01-14T10:00:00Z"
        )
    )

    private val payments = mutableListOf(
        PaymentItem(
            id = "p-1",
            communityId = "22222222-2222-4222-8222-222222222222",
            billId = "b-1",
            payerUserId = "user-1",
            flatId = "flat-1",
            amount = "4200.00",
            currency = "INR",
            paymentMethod = "UPI",
            reference = "UPI123456",
            status = "PENDING",
            visibility = "PRIVATE_TO_OWNER",
            evidenceRef = "evidence-1",
            createdAt = "2024-01-15T10:00:00Z",
            updatedAt = "2024-01-15T10:00:00Z",
            verifiedByUserId = null,
            verifiedAt = null,
            rejectedByUserId = null,
            rejectedAt = null,
            reversalReason = null
        ),
        PaymentItem(
            id = "p-2",
            communityId = "22222222-2222-4222-8222-222222222222",
            billId = "b-2",
            payerUserId = "user-2",
            flatId = "flat-2",
            amount = "1500.00",
            currency = "INR",
            paymentMethod = "CARD",
            reference = "CARD789012",
            status = "PAID",
            visibility = "PRIVATE_TO_OWNER",
            evidenceRef = "evidence-2",
            createdAt = "2024-01-14T10:00:00Z",
            updatedAt = "2024-01-14T10:00:00Z",
            verifiedByUserId = "admin-1",
            verifiedAt = "2024-01-14T11:00:00Z",
            rejectedByUserId = null,
            rejectedAt = null,
            reversalReason = null
        )
    )

    private val financialAccessGrants = mutableListOf(
        FinancialAccessGrantItem(
            id = "grant-1",
            communityId = "22222222-2222-4222-8222-222222222222",
            ownerUserId = "user-1",
            granteeUserId = "user-2",
            grantedByUserId = "user-1",
            status = "ACTIVE",
            reason = "Spouse access",
            notes = "For joint bill payments",
            grantedAt = "2024-01-01T00:00:00Z",
            revokedAt = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
    )

    private var activeCommunityId: String? = "22222222-2222-4222-8222-222222222222"

    override suspend fun dashboardTiles(): RepoResult<List<DashboardTile>> = RepoResult.Success(
        listOf(
            DashboardTile("Gate Approvals", "12", "Waiting", Color(0xFFF5E4EA), Icons.Default.Shield),
            DashboardTile("Bills Due", "₹18,240", "3 items", Color(0xFFF0D3DC), Icons.Default.AccountBalanceWallet),
            DashboardTile("Staff Today", "27", "On duty", Color(0xFFE9BEC9), Icons.Default.EmojiPeople),
            DashboardTile("SOS", "01", "Critical", Color(0xFFE2A9B7), Icons.Default.Warning),
            DashboardTile("Notices", "08", "New", Color(0xFFDA94A4), Icons.Default.Notifications),
            DashboardTile("Vehicles", "14", "Parked", Color(0xFFD27F92), Icons.Default.DirectionsCar)
        )
    )

    override suspend fun getUserProfile(): RepoResult<UserProfile> {
        return RepoResult.Success(
            UserProfile(
                id = "user-1",
                fullName = "Test Resident",
                email = "resident@example.com",
                phone = "+91-90000-00000",
                status = "ACTIVE",
                activeCommunities = emptyList(),
                // No photo in the mock profile: the Home hero shows a monogram instead.
                photoUrl = null,
                buildingName = "Tower B",
                flatNumber = "1004"
            )
        )
    }

    override suspend fun getCommunities(): RepoResult<List<CommunityMembershipInfo>> {
        return RepoResult.Success(emptyList())
    }

    override suspend fun getCommunity(communityId: String): RepoResult<CommunityInfo> {
        return RepoResult.Success(
            CommunityInfo(
                id = communityId,
                name = "Lakeview Residency",
                status = "ACTIVE",
                members = 120
            )
        )
    }

    override suspend fun discoverCommunities(query: String): RepoResult<List<CommunitySearchItem>> {
        // Demo-only discovery list mirroring the sample community served by
        // getCommunity above. Filtered by name so the search interaction behaves
        // like the backend; this is fixture data, not verified backend data.
        val communities = listOf(
            CommunitySearchItem(
                id = "22222222-2222-4222-8222-222222222222",
                name = "Lakeview Residency",
                status = "ACTIVE"
            )
        )
        val trimmed = query.trim()
        return RepoResult.Success(
            if (trimmed.isEmpty()) communities
            else communities.filter { it.name.contains(trimmed, ignoreCase = true) }
        )
    }

    override suspend fun setActiveCommunity(communityId: String) {
        activeCommunityId = communityId
    }

    override suspend fun getActiveCommunityId(): String? {
        return activeCommunityId
    }

    override suspend fun visitors(): RepoResult<List<VisitorRequest>> = RepoResult.Success(visitors.toList())

    override suspend fun approveVisitor(visitorId: String): Boolean {
        val index = visitors.indexOfFirst { it.passCode == visitorId }
        if (index == -1) return false
        val item = visitors[index]
        visitors[index] = item.copy(status = "Approved")
        return true
    }

    override suspend fun rejectVisitor(visitorId: String): Boolean {
        val index = visitors.indexOfFirst { it.passCode == visitorId }
        if (index == -1) return false
        val item = visitors[index]
        visitors[index] = item.copy(status = "Rejected")
        return true
    }

    override suspend fun createVisitorRequest(request: RepoCreateVisitorRequest): VisitorRequest? {
        val newVisitor = VisitorRequest(
            name = request.visitorName,
            purpose = request.purpose,
            time = request.expectedArrival?.let { formatTime(it) } ?: "TBD",
            apartment = request.flatId ?: "Unknown",
            passCode = "NV-${System.currentTimeMillis() % 10000}",
            status = "Pending"
        )
        visitors.add(newVisitor)
        return newVisitor
    }

    override suspend fun cancelVisitor(visitorId: String): Boolean {
        val index = visitors.indexOfFirst { it.passCode == visitorId }
        if (index == -1) return false
        val item = visitors[index]
        visitors[index] = item.copy(status = "Cancelled")
        return true
    }

    override suspend fun notices(): RepoResult<List<NoticeItem>> = RepoResult.Success(notices.toList())

    override suspend fun createNotice(request: RepoCreateNoticeRequest): NoticeItem? {
        val newNotice = NoticeItem(
            id = "n-${System.currentTimeMillis()}",
            communityId = activeCommunityId ?: "",
            createdByUserId = "user-1",
            title = request.title,
            message = request.message,
            status = if (request.published) "PUBLISHED" else "DRAFT",
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        notices.add(newNotice)
        return newNotice
    }

    override suspend fun publishNotice(noticeId: String): NoticeItem? {
        val index = notices.indexOfFirst { it.id == noticeId }
        if (index == -1) return null
        val notice = notices[index]
        val updated = notice.copy(status = "PUBLISHED", updatedAt = java.time.Instant.now().toString())
        notices[index] = updated
        return updated
    }

    override suspend fun maintenanceRequests(): RepoResult<List<MaintenanceRequestItem>> = RepoResult.Success(maintenanceRequests.toList())

    override suspend fun createMaintenanceRequest(request: RepoCreateMaintenanceRequestRequest): MaintenanceRequestItem? {
        val newRequest = MaintenanceRequestItem(
            id = "mr-${System.currentTimeMillis()}",
            communityId = activeCommunityId ?: "",
            flatId = request.flatId,
            createdByUserId = "user-1",
            category = request.category,
            title = request.title,
            description = request.description,
            priority = request.priority,
            status = "OPEN",
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        maintenanceRequests.add(newRequest)
        return newRequest
    }

    override suspend fun bills(): RepoResult<List<BillItem>> = RepoResult.Success(
        listOf(
            BillItem("b-1", "Maintenance", "₹4,200", "Due in 2 days", "Pending"),
            BillItem("b-2", "Water", "₹980", "Due today", "Urgent"),
            BillItem("b-3", "Power", "₹1,650", "Paid", "Paid")
        )
    )

    override suspend fun myPayments(): RepoResult<List<PaymentItem>> = RepoResult.Success(payments.toList())

    override suspend fun createPayment(request: RepoCreatePaymentRequest): PaymentItem? {
        val newPayment = PaymentItem(
            id = "p-${System.currentTimeMillis()}",
            communityId = activeCommunityId ?: "",
            billId = request.billId,
            payerUserId = request.payerUserId ?: "user-1",
            flatId = request.flatId,
            amount = request.amount,
            currency = request.currency,
            paymentMethod = request.paymentMethod,
            reference = request.reference,
            status = "PENDING",
            visibility = request.visibility,
            evidenceRef = request.evidenceRef,
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString(),
            verifiedByUserId = null,
            verifiedAt = null,
            rejectedByUserId = null,
            rejectedAt = null,
            reversalReason = null
        )
        payments.add(newPayment)
        return newPayment
    }

    override suspend fun verifyPayment(paymentId: String, reason: String?): PaymentItem? {
        val index = payments.indexOfFirst { it.id == paymentId }
        if (index == -1) return null
        val payment = payments[index]
        val updated = payment.copy(
            status = "PAID",
            verifiedByUserId = "admin-1",
            verifiedAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        payments[index] = updated
        return updated
    }

    override suspend fun rejectPayment(paymentId: String, reason: String?): PaymentItem? {
        val index = payments.indexOfFirst { it.id == paymentId }
        if (index == -1) return null
        val payment = payments[index]
        val updated = payment.copy(
            status = "REJECTED",
            rejectedByUserId = "admin-1",
            rejectedAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        payments[index] = updated
        return updated
    }

    override suspend fun financialAccessGrants(): RepoResult<List<FinancialAccessGrantItem>> = RepoResult.Success(financialAccessGrants.toList())

    override suspend fun createFinancialAccessGrant(request: RepoCreateFinancialAccessGrantRequest): FinancialAccessGrantItem? {
        val newGrant = FinancialAccessGrantItem(
            id = "grant-${System.currentTimeMillis()}",
            communityId = activeCommunityId ?: "",
            ownerUserId = request.ownerUserId,
            granteeUserId = request.granteeUserId,
            grantedByUserId = "user-1",
            status = "ACTIVE",
            reason = request.reason,
            notes = request.notes,
            grantedAt = java.time.Instant.now().toString(),
            revokedAt = null,
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        financialAccessGrants.add(newGrant)
        return newGrant
    }

    override suspend fun revokeFinancialAccessGrant(grantId: String): Boolean {
        val index = financialAccessGrants.indexOfFirst { it.id == grantId }
        if (index == -1) return false
        val grant = financialAccessGrants[index]
        financialAccessGrants[index] = grant.copy(status = "REVOKED", revokedAt = java.time.Instant.now().toString(), updatedAt = java.time.Instant.now().toString())
        return true
    }

    override suspend fun tickets(): RepoResult<List<ServiceTicket>> = RepoResult.Success(
        listOf(
            ServiceTicket("Lift 2 panel issue", "Maintenance", "In progress", "High"),
            ServiceTicket("Pool cleaning", "Amenities", "Open", "Medium"),
            ServiceTicket("Parking gate sensor", "Security", "Resolved", "Low")
        )
    )

    override suspend fun emergencyContacts(): RepoResult<List<EmergencyContact>> = RepoResult.Success(
        listOf(
            EmergencyContact("Security Desk", "Gate Control", "+91 99880 19234"),
            EmergencyContact("Resident Welfare", "Admin Desk", "+91 98450 66771"),
            EmergencyContact("Ambulance", "Medical", "+91 108"),
            EmergencyContact("Society Manager", "On call", "+91 98840 44229")
        )
    )

    private fun formatTime(isoString: String?): String {
        return isoString?.let {
            try {
                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    .format(java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.getDefault()).parse(it))
            } catch (e: Exception) {
                it
            }
        } ?: "TBD"
    }
}