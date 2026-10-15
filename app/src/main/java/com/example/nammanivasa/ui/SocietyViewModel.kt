package com.example.nammanivasa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammanivasa.data.api.AuthManager
import com.example.nammanivasa.data.model.BillItem
import com.example.nammanivasa.data.model.CommunityInfo
import com.example.nammanivasa.data.model.CommunityMembershipInfo
import com.example.nammanivasa.data.model.DashboardTile
import com.example.nammanivasa.data.model.EmergencyContact
import com.example.nammanivasa.data.model.FinancialAccessGrantItem
import com.example.nammanivasa.data.model.MaintenanceRequestItem
import com.example.nammanivasa.data.model.NoticeItem
import com.example.nammanivasa.data.model.PaymentItem
import com.example.nammanivasa.data.model.ServiceTicket
import com.example.nammanivasa.data.model.UserProfile
import com.example.nammanivasa.data.model.VisitorRequest
import com.example.nammanivasa.data.repository.DemoDataCapable
import com.example.nammanivasa.data.repository.FakeNammaNivasaRepository
import com.example.nammanivasa.data.repository.HybridNammaNivasaRepository
import com.example.nammanivasa.data.repository.NammaNivasaRepository
import com.example.nammanivasa.data.repository.RealNammaNivasaRepository
import com.example.nammanivasa.data.repository.RepoError
import com.example.nammanivasa.data.repository.RepoResult
import com.example.nammanivasa.data.repository.RepoCreateFinancialAccessGrantRequest
import com.example.nammanivasa.data.repository.RepoCreateMaintenanceRequestRequest
import com.example.nammanivasa.data.repository.RepoCreateNoticeRequest
import com.example.nammanivasa.data.repository.RepoCreatePaymentRequest
import com.example.nammanivasa.data.repository.RepoCreateVisitorRequest
import com.example.nammanivasa.data.repository.dataOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lifecycle of one independently-loaded section of the screen.
 *
 * [hasLoaded] is the flag that separates a real empty result from "we do not know
 * yet": an empty list is only an empty state once the section has completed
 * successfully at least once.
 */
data class SectionStatus(
    val isLoading: Boolean = false,
    val error: RepoError? = null,
    val hasLoaded: Boolean = false
) {
    /** True only when a completed request confirmed there is genuinely no data. */
    val isEmpty: Boolean get() = hasLoaded && !isLoading && error == null
}

/**
 * A loaded collection plus its own load status, so one failing section never
 * hides or invalidates another. On a failed refresh the previous [data] is kept
 * and [status] carries the error.
 *
 * Declared covariant so sections of different element types can be gathered into
 * a single list (see [SocietyUiState.hasLoadedOnce]).
 */
data class ListSection<out T>(
    val data: List<T> = emptyList(),
    val status: SectionStatus = SectionStatus()
) {
    val isEmpty: Boolean get() = status.isEmpty && data.isEmpty()
}

data class SocietyUiState(
    val userProfile: UserProfile? = null,
    val communities: List<CommunityMembershipInfo> = emptyList(),
    val activeCommunityId: String? = null,
    val activeCommunity: CommunityInfo? = null,
    val dashboardTiles: ListSection<DashboardTile> = ListSection(),
    val visitors: ListSection<VisitorRequest> = ListSection(),
    val notices: ListSection<NoticeItem> = ListSection(),
    val bills: ListSection<BillItem> = ListSection(),
    val myPayments: ListSection<PaymentItem> = ListSection(),
    val maintenanceRequests: ListSection<MaintenanceRequestItem> = ListSection(),
    val financialAccessGrants: ListSection<FinancialAccessGrantItem> = ListSection(),
    val tickets: ListSection<ServiceTicket> = ListSection(),
    val emergencyContacts: ListSection<EmergencyContact> = ListSection(),
    /** True until the first profile/community resolution completes. */
    val isLoading: Boolean = false,
    /** Set when the profile or the community list itself could not be loaded. */
    val error: RepoError? = null,
    /** True when a refresh failed while previously-loaded data is still on screen. */
    val isStale: Boolean = false,
    /**
     * True when the repository is serving local demo data because the backend was
     * unreachable or had no account for this user. Screens badge it honestly.
     */
    val usingDemoData: Boolean = false,
    /**
     * True when the user has no active community selected. The UI shows a selection
     * prompt instead of tabs that would otherwise look inexplicably empty.
     */
    val needsCommunitySelection: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
) {
    /** True once any section has completed a successful load. */
    val hasLoadedOnce: Boolean
        get() = listOf(
            visitors, notices, bills, myPayments,
            maintenanceRequests, financialAccessGrants, tickets, emergencyContacts
        ).any { it.status.hasLoaded }

    /** True while the first load of any section is still in flight. */
    val isInitialLoading: Boolean
        get() = isLoading || listOf(
            visitors, notices, bills, myPayments,
            maintenanceRequests, financialAccessGrants, tickets, emergencyContacts
        ).any { it.status.isLoading }
}

class SocietyViewModel(
    private val repository: NammaNivasaRepository = FakeNammaNivasaRepository(),
    /**
     * Awaited before the very first load. A backend-backed repository needs a
     * session token by then; the default no-op keeps the fake-only prototype and
     * existing unit tests unchanged.
     */
    private val authReady: suspend () -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocietyUiState())
    val uiState: StateFlow<SocietyUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            authReady()
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, needsCommunitySelection = false)
            val userProfileResult = repository.getUserProfile()
            val communitiesResult = repository.getCommunities()

            // Profile and community list are prerequisites for every other section,
            // so a failure here is surfaced as a screen-level error.
            val fatalError = (userProfileResult as? RepoResult.Failure)?.error
                ?: (communitiesResult as? RepoResult.Failure)?.error
            val activeCommunityId = repository.getActiveCommunityId()

            _uiState.value = _uiState.value.copy(
                userProfile = userProfileResult.dataOrNull,
                communities = communitiesResult.dataOrNull ?: emptyList(),
                activeCommunityId = activeCommunityId,
                isLoading = false,
                error = fatalError
            )
            syncDemoDataFlag()

            when {
                fatalError != null -> Unit
                activeCommunityId != null -> loadCommunityData(activeCommunityId)
                // No community chosen: prompt instead of showing unexplained empty tabs.
                else -> _uiState.value = _uiState.value.copy(needsCommunitySelection = true)
            }
        }
    }

    fun selectCommunity(communityId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                activeCommunityId = communityId,
                error = null,
                needsCommunitySelection = false
            )
            when (val community = repository.getCommunity(communityId)) {
                is RepoResult.Success -> _uiState.value = _uiState.value.copy(activeCommunity = community.data)
                is RepoResult.Failure -> _uiState.value = _uiState.value.copy(error = community.error)
            }
            repository.setActiveCommunity(communityId)
            loadCommunityData(communityId)
        }
    }

    /**
     * Loads each community-scoped section independently: one failing request no
     * longer aborts the rest, and a failed section keeps whatever it held before.
     */
    private fun loadCommunityData(communityId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(needsCommunitySelection = false)
            val s = _uiState.value
            _uiState.value = s.copy(
                visitors = s.visitors.loading(),
                notices = s.notices.loading(),
                bills = s.bills.loading(),
                myPayments = s.myPayments.loading(),
                maintenanceRequests = s.maintenanceRequests.loading(),
                financialAccessGrants = s.financialAccessGrants.loading(),
                tickets = s.tickets.loading(),
                emergencyContacts = s.emergencyContacts.loading()
            )

            // Each section loads and folds independently and concurrently, so a slow
            // or failing request cannot delay or discard the others. MutableStateFlow
            // .update is atomic, so concurrent folds cannot lose each other's writes.
            coroutineScope {
                launch { val r = repository.visitors(); _uiState.update { it.copy(visitors = it.visitors.fold(r)) } }
                launch { val r = repository.notices(); _uiState.update { it.copy(notices = it.notices.fold(r)) } }
                launch { val r = repository.bills(); _uiState.update { it.copy(bills = it.bills.fold(r)) } }
                launch { val r = repository.myPayments(); _uiState.update { it.copy(myPayments = it.myPayments.fold(r)) } }
                launch { val r = repository.maintenanceRequests(); _uiState.update { it.copy(maintenanceRequests = it.maintenanceRequests.fold(r)) } }
                launch { val r = repository.financialAccessGrants(); _uiState.update { it.copy(financialAccessGrants = it.financialAccessGrants.fold(r)) } }
                launch { val r = repository.tickets(); _uiState.update { it.copy(tickets = it.tickets.fold(r)) } }
                launch { val r = repository.emergencyContacts(); _uiState.update { it.copy(emergencyContacts = it.emergencyContacts.fold(r)) } }
            }
            syncDemoDataFlag()
        }
    }

    /**
     * Folds a read outcome into a section: success replaces the data and clears the
     * error; failure keeps whatever was already there and records the error. Nothing
     * is emptied, so a failed read can never masquerade as "no data".
     */
    private fun <T> ListSection<T>.fold(result: RepoResult<List<T>>): ListSection<T> =
        when (result) {
            is RepoResult.Success ->
                ListSection(result.data, SectionStatus(hasLoaded = true))
            is RepoResult.Failure ->
                copy(status = status.copy(isLoading = false, error = result.error))
        }

    private fun <T> ListSection<T>.loading() = copy(status = status.copy(isLoading = true))

    fun refresh() {
        viewModelScope.launch {
            val hadData = _uiState.value.hasLoadedOnce
            _uiState.value = _uiState.value.copy(isStale = false)
            val userProfileResult = repository.getUserProfile()
            val communitiesResult = repository.getCommunities()
            val fatalError = (userProfileResult as? RepoResult.Failure)?.error
                ?: (communitiesResult as? RepoResult.Failure)?.error

            _uiState.value = _uiState.value.copy(
                userProfile = userProfileResult.dataOrNull ?: _uiState.value.userProfile,
                communities = communitiesResult.dataOrNull ?: _uiState.value.communities,
                activeCommunityId = repository.getActiveCommunityId(),
                error = fatalError,
                // Sections keep their data; isStale marks it as no longer confirmed.
                isStale = fatalError != null && hadData
            )

            if (fatalError == null) {
                val id = _uiState.value.activeCommunityId
                if (id != null) loadCommunityData(id)
                else _uiState.value = _uiState.value.copy(needsCommunitySelection = true)
            }
            syncDemoDataFlag()
        }
    }

    // Visitor actions
    fun approveVisitor(visitorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = repository.approveVisitor(visitorId)
                if (success) {
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = RepoError.Unexpected("Failed to approve visitor")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = RepoError.Unexpected(e.message ?: "Failed to approve visitor")
                )
            }
        }
    }

    fun rejectVisitor(visitorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = repository.rejectVisitor(visitorId)
                if (success) {
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = RepoError.Unexpected("Failed to reject visitor")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = RepoError.Unexpected(e.message ?: "Failed to reject visitor")
                )
            }
        }
    }

    fun createVisitorRequest(request: RepoCreateVisitorRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val result = repository.createVisitorRequest(request)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to create visitor request")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to create visitor request")
            }
        }
    }

    fun cancelVisitor(visitorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = repository.cancelVisitor(visitorId)
                if (success) {
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = RepoError.Unexpected("Failed to cancel visitor request")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = RepoError.Unexpected(e.message ?: "Failed to cancel visitor request")
                )
            }
        }
    }

    // Notice actions
    fun createNotice(request: RepoCreateNoticeRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val result = repository.createNotice(request)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to create notice")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to create notice")
            }
        }
    }

    fun publishNotice(noticeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val result = repository.publishNotice(noticeId)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to publish notice")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to publish notice")
            }
        }
    }

    // Maintenance actions
    fun createMaintenanceRequest(request: RepoCreateMaintenanceRequestRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val result = repository.createMaintenanceRequest(request)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to create maintenance request")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to create maintenance request")
            }
        }
    }

    // Payment actions
    fun createPayment(request: RepoCreatePaymentRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val result = repository.createPayment(request)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to create payment")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to create payment")
            }
        }
    }

    fun verifyPayment(paymentId: String, reason: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val result = repository.verifyPayment(paymentId, reason)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to verify payment")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to verify payment")
            }
        }
    }

    fun rejectPayment(paymentId: String, reason: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val result = repository.rejectPayment(paymentId, reason)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to reject payment")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to reject payment")
            }
        }
    }

    // Financial Access actions
    fun createFinancialAccessGrant(request: RepoCreateFinancialAccessGrantRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val result = repository.createFinancialAccessGrant(request)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to create financial access grant")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to create financial access grant")
            }
        }
    }

    fun revokeFinancialAccessGrant(grantId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                val success = repository.revokeFinancialAccessGrant(grantId)
                if (success) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = "Failed to revoke financial access grant")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to revoke financial access grant")
            }
        }
    }

    /**
     * Mirrors the repository's demo-data flag into the UI state so screens can
     * badge fabricated data honestly. A no-op for repositories without the
     * [DemoDataCapable] capability (e.g. the fake used in unit tests).
     */
    private fun syncDemoDataFlag() {
        val demo = (repository as? DemoDataCapable)?.usingDemoData == true
        if (_uiState.value.usingDemoData != demo) {
            _uiState.value = _uiState.value.copy(usingDemoData = demo)
        }
    }

    companion object {
        /**
         * Production factory: talks to the backend whenever it is reachable and the
         * session is valid, and falls back to local demo data (clearly badged in the
         * UI) otherwise, so the app stays usable on an emulator with no backend.
         */
        fun createDefault(context: android.content.Context): SocietyViewModel {
            val appContext = context.applicationContext
            return SocietyViewModel(
                repository = HybridNammaNivasaRepository(
                    remote = RealNammaNivasaRepository(appContext),
                    local = FakeNammaNivasaRepository()
                ),
                authReady = { AuthManager.ensureSignedIn() }
            )
        }
    }
}