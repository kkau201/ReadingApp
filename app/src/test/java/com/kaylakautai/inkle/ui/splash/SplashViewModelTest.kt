package com.kaylakautai.inkle.ui.splash

import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.auth.FakeAuthRepository
import com.kaylakautai.inkle.common.DependencyContextWrapper
import com.kaylakautai.inkle.data.DataOrException
import com.kaylakautai.inkle.nav.NavigateTo
import com.kaylakautai.inkle.nav.NavigationEvent
import com.kaylakautai.inkle.repo.FireRepository
import com.kaylakautai.inkle.testing.BaseViewModelTest
import com.kaylakautai.inkle.testing.TestData
import com.kaylakautai.inkle.testing.TestData.UID
import com.kaylakautai.inkle.testing.fakeContext
import com.kaylakautai.inkle.testing.str
import com.kaylakautai.inkle.ui.destinations.DashboardScreenDestination
import com.kaylakautai.inkle.ui.destinations.LoginScreenDestination
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest : BaseViewModelTest() {

    private val authRepository = FakeAuthRepository()
    private val fireRepository = mockk<FireRepository>()

    private val user = TestData.authUser
    private val profile = TestData.profile

    private fun TestScope.start() {
        collectNavigation()
        SplashViewModel(DependencyContextWrapper(fakeContext()), fireRepository, authRepository)
            .setMainViewModel(mainViewModel)
    }

    @Test
    fun signedOut_goesToLoginAfterAShortSplash() = runTest {
        start()

        // Signed-out users no longer wait 5 seconds.
        advanceTimeBy(1001.milliseconds)

        assertEquals(
            listOf<NavigationEvent?>(NavigateTo(LoginScreenDestination, popCurrent = true)),
            navigationEvents
        )
    }

    @Test
    fun signedIn_withProfile_goesToDashboard() = runTest {
        authRepository.current = user
        authRepository.existingUserDocuments += UID
        coEvery { fireRepository.fetchUser(UID) } returns DataOrException(data = profile)

        start()
        advanceUntilIdle()

        assertTrue(authRepository.createdUserDocuments.isEmpty())
        assertEquals(
            listOf<NavigationEvent?>(NavigateTo(DashboardScreenDestination, popCurrent = true)),
            navigationEvents
        )
    }

    @Test
    fun signedIn_withoutProfile_createsItAndGoesToDashboard() = runTest {
        // e.g. the profile write failed during sign-up
        authRepository.current = user
        coEvery { fireRepository.fetchUser(UID) } returns DataOrException(data = profile)

        start()
        advanceUntilIdle()

        assertEquals(listOf(UID to TestData.DISPLAY_NAME), authRepository.createdUserDocuments)
        assertEquals(
            listOf<NavigationEvent?>(NavigateTo(DashboardScreenDestination, popCurrent = true)),
            navigationEvents
        )
    }

    @Test
    fun signedIn_profileCantBeLoaded_showsErrorDialogInsteadOfHanging() = runTest {
        authRepository.current = user
        authRepository.createDocumentError = IllegalStateException("offline")
        coEvery { fireRepository.fetchUser(UID) } returns DataOrException(data = null)

        start()
        advanceUntilIdle()

        assertTrue(navigationEvents.isEmpty())
        assertEquals(str(R.string.login_error_message), mainViewModel.dialogFlow.value?.message)
    }
}
