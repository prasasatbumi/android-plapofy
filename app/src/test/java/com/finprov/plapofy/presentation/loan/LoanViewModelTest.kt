package com.finprov.plapofy.presentation.loan

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.finprov.plapofy.domain.model.Branch
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.BranchRepository
import com.finprov.plapofy.domain.repository.LoanRepository
import com.finprov.plapofy.domain.repository.PlafondRepository
import com.finprov.plapofy.domain.repository.ProfileCompletenessResult
import com.finprov.plapofy.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoanViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var loanRepository: LoanRepository

    @Mock
    private lateinit var plafondRepository: PlafondRepository

    @Mock
    private lateinit var profileRepository: ProfileRepository

    @Mock
    private lateinit var branchRepository: BranchRepository

    @Mock
    private lateinit var creditLineRepository: com.finprov.plapofy.domain.repository.CreditLineRepository

    @Mock
    private lateinit var pendingLoanDao: com.finprov.plapofy.data.local.dao.PendingLoanDao

    @Mock
    private lateinit var plafondDao: com.finprov.plapofy.data.local.dao.PlafondDao

    private lateinit var viewModel: LoanViewModel

    private val testPlafond = Plafond(
        id = 1L,
        code = "KMG",
        name = "Kredit Multiguna",
        description = "Pinjaman cepat cair",
        minAmount = 1_000_000.0,
        maxAmount = 50_000_000.0,
        interests = listOf(
            com.finprov.plapofy.domain.model.ProductInterest(1L, 6, 12.0),
            com.finprov.plapofy.domain.model.ProductInterest(2L, 36, 12.0)
        )
    )

    private val testBranches = listOf(
        Branch(id = 1L, name = "Jakarta Pusat", code = "JKT01", location = "Jakarta"),
        Branch(id = 2L, name = "Bandung", code = "BDG01", location = "Bandung")
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock default behavior needed for initialization or flows
        whenever(pendingLoanDao.getPendingForDisplay()).thenReturn(kotlinx.coroutines.flow.flowOf(emptyList()))
        
        viewModel = LoanViewModel(
            loanRepository = loanRepository,
            plafondRepository = plafondRepository,
            profileRepository = profileRepository,
            branchRepository = branchRepository,
            creditLineRepository = creditLineRepository,
            pendingLoanDao = pendingLoanDao,
            plafondDao = plafondDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() {
        val state = viewModel.applyLoanState.value
        assertFalse(state.isLoading)
        assertNull(state.plafond)
        assertTrue(state.branches.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `loadPlafondForApplication should update state with plafond and branches`() = runTest {
        // Given
        whenever(branchRepository.getBranches()).thenReturn(Result.success(testBranches))
        whenever(plafondRepository.getPlafonds()).thenReturn(Result.success(listOf(testPlafond)))
        whenever(profileRepository.checkProfileComplete()).thenReturn(
            Result.success(ProfileCompletenessResult(isComplete = true, missingFields = emptyList()))
        )

        // When
        viewModel.loadPlafondForApplication(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.applyLoanState.value
        assertFalse(state.isLoading)
        assertNotNull(state.plafond)
        assertEquals("Kredit Multiguna", state.plafond?.name)
        assertEquals(2, state.branches.size)
    }

    @Test
    fun `loadPlafondForApplication should set error when plafond not found`() = runTest {
        // Given
        whenever(branchRepository.getBranches()).thenReturn(Result.success(testBranches))
        whenever(plafondRepository.getPlafonds()).thenReturn(Result.success(listOf(testPlafond)))

        // When - load non-existent plafond
        viewModel.loadPlafondForApplication(999L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.applyLoanState.value
        assertEquals("Produk tidak ditemukan", state.error)
    }

    @Test
    fun `submitLoan should validate amount below minimum`() = runTest {
        // Given - set up state with plafond
        whenever(branchRepository.getBranches()).thenReturn(Result.success(testBranches))
        whenever(plafondRepository.getPlafonds()).thenReturn(Result.success(listOf(testPlafond)))
        whenever(profileRepository.checkProfileComplete()).thenReturn(
            Result.success(ProfileCompletenessResult(isComplete = true, missingFields = emptyList()))
        )
        
        viewModel.loadPlafondForApplication(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - submit with amount below minimum
        viewModel.submitLoan(
            amount = 500_000.0, // below min of 1_000_000
            tenor = 12,
            purpose = "Test",
            branchId = 1L,
            latitude = 0.0,
            longitude = 0.0
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.applyLoanState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("antara"))
    }

    @Test
    fun `submitLoan should validate tenor out of range`() = runTest {
        // Given - set up state with plafond
        whenever(branchRepository.getBranches()).thenReturn(Result.success(testBranches))
        whenever(plafondRepository.getPlafonds()).thenReturn(Result.success(listOf(testPlafond)))
        whenever(profileRepository.checkProfileComplete()).thenReturn(
            Result.success(ProfileCompletenessResult(isComplete = true, missingFields = emptyList()))
        )
        
        viewModel.loadPlafondForApplication(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - submit with tenor above maximum
        viewModel.submitLoan(
            amount = 10_000_000.0,
            tenor = 48, // above max of 36
            purpose = "Test",
            branchId = 1L,
            latitude = 0.0,
            longitude = 0.0
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.applyLoanState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Tenor"))
    }

    @Test
    fun `clearError should reset error state`() {
        // Given - we need to trigger an error first by accessing internal state
        viewModel.clearError()

        // Then
        val state = viewModel.applyLoanState.value
        assertNull(state.error)
    }

    @Test
    fun `resetApplyState should return to initial state`() {
        // When
        viewModel.resetApplyState()

        // Then
        val state = viewModel.applyLoanState.value
        assertFalse(state.isLoading)
        assertNull(state.plafond)
        assertTrue(state.branches.isEmpty())
        assertFalse(state.submitSuccess)
    }

    @Test
    fun `myLoansState initial state should be empty`() {
        val state = viewModel.myLoansState.value
        assertFalse(state.isLoading)
        assertTrue(state.loans.isEmpty())
        assertNull(state.error)
    }
}
