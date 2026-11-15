package com.example.readingapp.ui.splash

import android.content.Context
import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.AuthUser
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MUser
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.testing.MainDispatcherRule
import com.example.readingapp.ui.destinations.DashboardScreenDestination
import com.example.readingapp.ui.destinations.LoginScreenDestination
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mockk<Context>().also { context ->
        every { context.getString(any()) } answers { "res:${firstArg<Int>()}" }
    }
    private val authRepository = FakeAuthRepository()
    private val fireRepository = mockk<FireRepository>()
    private val mainViewModel = MainViewModel()
    private val navigationEvents = mutableListOf<NavigationEvent?>()

    private val user = AuthUser(uid = "uid-1", displayName = "Jane Austen", email = "jane@example.com")
    private val profile = MUser(userId = "uid-1", displayName = "Jane Austen")

    private fun TestScope.start() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            mainViewModel.navigationFlow.collect { navigationEvents += it }
        }
        SplashViewModel(DependencyContextWrapper(context), fireRepository, authRepository)
            .setMainViewModel(mainViewModel)
    }

    @Test
    fun signedOut_goesToLoginAfterAShortSplash() = runTest {
        start()

        // Signed-out users no longer wait 5 seconds.
        advanceTimeBy(1001)

        assertEquals(
            listOf<NavigationEvent?>(NavigateTo(LoginScreenDestination, popCurrent = true)),
            navigationEvents
        )
    }

    @Test
    fun signedIn_withProfile_goesToDashboard() = runTest {
        authRepository.current = user
        authRepository.existingUserDocuments += "uid-1"
        coEvery { fireRepository.fetchUser("uid-1") } returns DataOrException(data = profile)

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
        coEvery { fireRepository.fetchUser("uid-1") } returns DataOrException(data = profile)

        start()
        advanceUntilIdle()

        assertEquals(listOf("uid-1" to "Jane Austen"), authRepository.createdUserDocuments)
        assertEquals(
            listOf<NavigationEvent?>(NavigateTo(DashboardScreenDestination, popCurrent = true)),
            navigationEvents
        )
    }

    @Test
    fun signedIn_profileCantBeLoaded_showsErrorDialogInsteadOfHanging() = runTest {
        authRepository.current = user
        authRepository.createDocumentError = IllegalStateException("offline")
        coEvery { fireRepository.fetchUser("uid-1") } returns DataOrException(data = null)

        start()
        advanceUntilIdle()

        assertTrue(navigationEvents.isEmpty())
        assertEquals("res:${R.string.login_error_message}", mainViewModel.dialogFlow.value?.message)
    }
}
