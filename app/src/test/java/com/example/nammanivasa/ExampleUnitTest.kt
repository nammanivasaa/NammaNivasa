package com.example.nammanivasa

import com.example.nammanivasa.data.repository.CommunityRepository
import com.example.nammanivasa.data.repository.FakeNammaNivasaRepository
import com.example.nammanivasa.data.repository.PaymentRepository
import com.example.nammanivasa.data.repository.RepoResult
import com.example.nammanivasa.data.repository.ServiceRepository
import com.example.nammanivasa.data.repository.VisitorRepository
import com.example.nammanivasa.data.repository.dataOrNull
import com.example.nammanivasa.ui.SocietyViewModel
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun repositoryProvidesResidentDataSets() = runTest {
        val repository = FakeNammaNivasaRepository()

        // Reads now report success/failure explicitly; a successful empty result is
        // distinct from a failure.
        assertEquals(3, repository.visitors().dataOrNull?.size)
        assertEquals(3, repository.bills().dataOrNull?.size)
        assertEquals(3, repository.tickets().dataOrNull?.size)
        assertEquals(4, repository.emergencyContacts().dataOrNull?.size)
        assertTrue(repository.dashboardTiles().dataOrNull?.isNotEmpty() == true)
    }

    @Test
    fun fakeRepositoryNeverReportsFailure() = runTest {
        val repository = FakeNammaNivasaRepository()

        listOf(
            repository.visitors(),
            repository.bills(),
            repository.tickets(),
            repository.emergencyContacts(),
            repository.notices(),
            repository.maintenanceRequests(),
            repository.myPayments(),
            repository.financialAccessGrants()
        ).forEach { result ->
            assertTrue(result is RepoResult.Success)
        }
    }

    @Test
    fun repositoryImplementsSeparateDataBoundaries() {
        val repository = FakeNammaNivasaRepository()

        assertTrue(repository is CommunityRepository)
        assertTrue(repository is VisitorRepository)
        assertTrue(repository is PaymentRepository)
        assertTrue(repository is ServiceRepository)
    }

    @Test
    fun approveVisitorUpdatesStatus() = runTest {
        val repository = FakeNammaNivasaRepository()
        repository.approveVisitor("NV-1042")

        assertEquals("Approved", repository.visitors().dataOrNull?.get(0)?.status)
    }

    @Test
    fun invalidVisitorIndexDoesNotMutateRepositoryState() = runTest {
        val repository = FakeNammaNivasaRepository()

        assertFalse(repository.approveVisitor("invalid"))
        assertFalse(repository.rejectVisitor("invalid"))
        assertEquals(3, repository.visitors().dataOrNull?.size)
        assertEquals("Arihant Sharma", repository.visitors().dataOrNull?.get(0)?.name)
    }
}