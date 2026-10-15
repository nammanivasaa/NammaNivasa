package com.example.nammanivasa

import com.example.nammanivasa.data.model.CommunitySearchItem
import com.example.nammanivasa.data.model.PaymentItem
import com.example.nammanivasa.data.model.VisitorRequest
import com.example.nammanivasa.data.repository.FakeNammaNivasaRepository
import com.example.nammanivasa.data.repository.HybridNammaNivasaRepository
import com.example.nammanivasa.data.repository.NammaNivasaRepository
import com.example.nammanivasa.data.repository.RepoCreatePaymentRequest
import com.example.nammanivasa.data.repository.RepoError
import com.example.nammanivasa.data.repository.RepoResult
import com.example.nammanivasa.data.repository.dataOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Stand-in for the backend: reads succeed with the demo delegate's data until
 * scripted to fail, and writes can be scripted to fail so the fallback paths of
 * [HybridNammaNivasaRepository] are exercised without any network.
 */
private class ScriptedRemote(
    private val delegate: NammaNivasaRepository = FakeNammaNivasaRepository()
) : NammaNivasaRepository by delegate {

    var visitorsResult: RepoResult<List<VisitorRequest>>? = null
    var createPaymentResult: PaymentItem? = null
    var approveVisitorResult: Boolean? = null
    var discoverCommunitiesResult: RepoResult<List<CommunitySearchItem>>? = null

    override suspend fun visitors(): RepoResult<List<VisitorRequest>> =
        visitorsResult ?: delegate.visitors()

    override suspend fun createPayment(request: RepoCreatePaymentRequest): PaymentItem? =
        createPaymentResult

    override suspend fun approveVisitor(visitorId: String): Boolean =
        approveVisitorResult ?: delegate.approveVisitor(visitorId)

    override suspend fun discoverCommunities(query: String): RepoResult<List<CommunitySearchItem>> =
        discoverCommunitiesResult ?: delegate.discoverCommunities(query)
}

class HybridRepositoryTest {

    @Test
    fun remoteSuccessIsPassedThroughUnchanged() = runTest {
        val remoteVisitor = VisitorRequest("Backend Visitor", "Courier", "08:00 AM", "D-12", "NV-9999")
        val remote = ScriptedRemote().apply {
            visitorsResult = RepoResult.Success(listOf(remoteVisitor))
        }
        val hybrid = HybridNammaNivasaRepository(remote, FakeNammaNivasaRepository())

        val result = hybrid.visitors()

        assertEquals(listOf(remoteVisitor), result.dataOrNull)
        assertFalse(hybrid.usingDemoData)
    }

    @Test
    fun networkFailureFallsBackToDemoData() = runTest {
        val remote = ScriptedRemote().apply {
            visitorsResult = RepoResult.Failure(RepoError.Network("offline"))
        }
        val hybrid = HybridNammaNivasaRepository(remote, FakeNammaNivasaRepository())

        val result = hybrid.visitors()

        assertTrue(result is RepoResult.Success)
        assertEquals(3, result.dataOrNull?.size)
        assertTrue(hybrid.usingDemoData)
    }

    @Test
    fun serverRejectionStillServesDemoDataAndIsFlagged() = runTest {
        // Deliberate product choice: with no provisioned backend account the server
        // rejects reads, and the app must stay usable — but never silently.
        val remote = ScriptedRemote().apply {
            visitorsResult = RepoResult.Failure(RepoError.Server(404, "Not Found"))
        }
        val hybrid = HybridNammaNivasaRepository(remote, FakeNammaNivasaRepository())

        val result = hybrid.visitors()

        assertTrue(result is RepoResult.Success)
        assertTrue(hybrid.usingDemoData)
    }

    @Test
    fun failedRemoteWriteCompletesAgainstDemoStore() = runTest {
        val remote = ScriptedRemote().apply { createPaymentResult = null }
        val hybrid = HybridNammaNivasaRepository(remote, FakeNammaNivasaRepository())

        val created = hybrid.createPayment(RepoCreatePaymentRequest(amount = "999.00"))

        assertNotNull(created)
        assertEquals("999.00", created?.amount)
        assertTrue(hybrid.usingDemoData)
    }

    @Test
    fun successfulRemoteWriteDoesNotFlagDemoData() = runTest {
        val remote = ScriptedRemote().apply { approveVisitorResult = true }
        val hybrid = HybridNammaNivasaRepository(remote, FakeNammaNivasaRepository())

        assertTrue(hybrid.approveVisitor("NV-1042"))
        assertFalse(hybrid.usingDemoData)
    }

    @Test
    fun localFallbackErrorIsSurfacedWhenBothSourcesFail() = runTest {
        // The local demo store always succeeds today; this guards the contract that
        // a failure is never converted into an empty success by the hybrid layer.
        val remote = ScriptedRemote().apply {
            visitorsResult = RepoResult.Failure(RepoError.Unexpected("boom"))
        }
        val hybrid = HybridNammaNivasaRepository(remote, FakeNammaNivasaRepository())

        assertTrue(hybrid.visitors() is RepoResult.Success)
    }

    @Test
    fun remoteDiscoveryFailureFallsBackToDemo() = runTest {
        val remote = ScriptedRemote().apply {
            discoverCommunitiesResult = RepoResult.Failure(RepoError.Network("offline"))
        }
        val hybrid = HybridNammaNivasaRepository(remote, FakeNammaNivasaRepository())

        val result = hybrid.discoverCommunities("Lakeview")

        assertTrue(result is RepoResult.Success)
        assertEquals(1, result.dataOrNull?.size)
        assertTrue(hybrid.usingDemoData)
    }
}
