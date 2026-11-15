package com.example.readingapp.ui.user

import android.content.Context
import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.AuthException
import com.example.readingapp.auth.AuthFailure
import com.example.readingapp.auth.AuthUser
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.testing.MainDispatcherRule
import com.example.readingapp.ui.components.DialogState
import com.example.readingapp.ui.destinations.LoginScreenDestination
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Logout and account deletion on the profile screen. */
@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelAccountTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mockk<Context>().also { context ->
        every { context.getString(any()) } answers { str(firstArg()) }
    }
    private val authRepository = FakeAuthRepository().apply {
        current = AuthUser(uid = "uid-1", displayName = "Jane Austen", email = "jane@example.com")
    }
    private val fireRepository = spyk(
        FireRepository(
            queryBook = mockk(relaxed = true),
            queryUser = mockk(relaxed = true),
            querySession = mockk(relaxed = true),
            currentUserId = { authRepository.currentUserId }
        )
    )
    private val mainViewModel = MainViewModel()
    private val navigationEvents = mutableListOf<NavigationEvent?>()
    private lateinit var viewModel: UserViewModel

    private val toLogin = NavigateTo(LoginScreenDestination, clearBackStack = true)

    @Before
    fun setUp() {
        viewModel = UserViewModel(fireRepository, authRepository, DependencyContextWrapper(context))
        viewModel.setMainViewModel(mainViewModel)
    }

    private fun TestScope.collectNavigation() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            mainViewModel.navigationFlow.collect { navigationEvents += it }
        }
    }

    private fun dialog(): DialogState = mainViewModel.dialogFlow.value ?: error("No dialog shown")

    @Test
    fun logout_signsOutClearsCacheAndGoesToLogin() = runTest {
        collectNavigation()

        viewModel.onLogoutClick()
        dialog().onPrimaryClick!!()

        assertEquals(1, authRepository.signOutCount)
        verify { fireRepository.clearUserData() }
        assertEquals(listOf<NavigationEvent?>(toLogin), navigationEvents)
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun deleteAccount_confirmed_deletesEverythingAndGoesToLogin() = runTest {
        collectNavigation()

        viewModel.onDeleteAccountClick()
        assertEquals(str(R.string.delete_account_message), dialog().message)
        dialog().onPrimaryClick!!()

        assertEquals(1, authRepository.deleteCount)
        verify { fireRepository.clearUserData() }
        assertEquals(str(R.string.delete_account_done), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(toLogin), navigationEvents)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
    }

    @Test
    fun deleteAccount_cancelled_doesNothing() = runTest {
        collectNavigation()

        viewModel.onDeleteAccountClick()
        dialog().onSecondaryClick!!()

        assertEquals(0, authRepository.deleteCount)
        assertTrue(navigationEvents.isEmpty())
        assertNull(mainViewModel.dialogFlow.value)
    }

    @Test
    fun deleteAccount_oldSignIn_asksToSignInAgainBeforeDeletingAnything() = runTest {
        collectNavigation()
        authRepository.recentlySignedIn = false

        viewModel.onDeleteAccountClick()

        assertEquals(str(R.string.delete_account_sign_in_again), dialog().message)
        assertEquals(0, authRepository.deleteCount)

        // The dialog's button logs the user out so they can sign in again.
        dialog().onPrimaryClick!!()
        assertEquals(1, authRepository.signOutCount)
        assertEquals(listOf<NavigationEvent?>(toLogin), navigationEvents)
    }

    @Test
    fun deleteAccount_firebaseWantsRecentLogin_asksToSignInAgain() = runTest {
        collectNavigation()
        authRepository.deleteError = AuthException(AuthFailure.REQUIRES_RECENT_LOGIN)

        viewModel.onDeleteAccountClick()
        dialog().onPrimaryClick!!()

        assertEquals(str(R.string.delete_account_sign_in_again), dialog().message)
        assertTrue(navigationEvents.isEmpty())
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
    }

    @Test
    fun deleteAccount_otherFailure_showsErrorAndStays() = runTest {
        collectNavigation()
        authRepository.deleteError = IllegalStateException("Firestore unavailable")

        viewModel.onDeleteAccountClick()
        dialog().onPrimaryClick!!()

        assertEquals(str(R.string.delete_account_error), mainViewModel.toastFlow.value)
        assertTrue(navigationEvents.isEmpty())
        verify(exactly = 0) { fireRepository.clearUserData() }
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
    }

    private fun str(id: Int) = "res:$id"
}
