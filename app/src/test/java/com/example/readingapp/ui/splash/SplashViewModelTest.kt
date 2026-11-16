package com.example.readingapp.ui.splash

import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.data.DataOrException
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.testing.MainDispatcherRule
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.UID
import com.example.readingapp.testing.collectNavigation
import com.example.readingapp.testing.fakeContext
import com.example.readingapp.testing.str
import com.example.readingapp.ui.destinations.DashboardScreenDestination
import com.example.readingapp.ui.destinations.LoginScreenDestination
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val fireRepository = mockk<FireRepository>()
    private val mainViewModel = MainViewModel()
    private val navigationEvents = mutableListOf<NavigationEvent?>()

    private val user = TestData.authUser
    private val profile = TestData.profile

    private fun TestScope.start() {
        collectNavigation(mainViewModel, into = navigationEvents)
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
