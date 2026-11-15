package com.example.readingapp.ui.user

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.AuthException
import com.example.readingapp.auth.AuthFailure
import com.example.readingapp.auth.AuthUser
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.auth.FakeGoogleIdTokenProvider
import com.example.readingapp.auth.ReauthMethod
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Logout and account deletion (with on-the-spot reauthentication) on the profile screen. */
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
    private val tokenProvider = FakeGoogleIdTokenProvider()
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
        viewModel = UserViewModel(
            fireRepository,
            authRepository,
            tokenProvider,
            DependencyContextWrapper(context)
        )
        viewModel.setMainViewModel(mainViewModel)
    }

    private fun TestScope.collectNavigation() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            mainViewModel.navigationFlow.collect { navigationEvents += it }
        }
    }

    private fun dialog(): DialogState = mainViewModel.dialogFlow.value ?: error("No dialog shown")

    private fun typePassword(password: String) = dialog().passwordInput!!.onValueChange(password)

    private fun assertNothingDeleted() {
        assertEquals(0, authRepository.deleteCount)
        verify(exactly = 0) { fireRepository.clearUserData() }
    }

    // region Logout

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

    // endregion

    // region Delete account: Google

    @Test
    fun deleteGoogleAccount_confirmWithGoogle_reauthsThenDeletesAndGoesToLogin() = runTest {
        collectNavigation()

        viewModel.onDeleteAccountClick()
        assertEquals(str(R.string.delete_account_message_google), dialog().message)
        assertNull(dialog().passwordInput)
        dialog().onPrimaryClick!!()

        assertEquals(1, tokenProvider.requestCount)
        assertEquals(listOf("fake-id-token"), authRepository.googleReauthTokens)
        assertEquals(1, authRepository.deleteCount)
        verify { fireRepository.clearUserData() }
        assertEquals(str(R.string.delete_account_done), mainViewModel.toastFlow.value)
        assertEquals(listOf<NavigationEvent?>(toLogin), navigationEvents)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        // No logout detour any more.
        assertEquals(0, authRepository.signOutCount)
    }

    @Test
    fun deleteGoogleAccount_pickerClosed_deletesNothingAndSaysNothing() = runTest {
        collectNavigation()
        tokenProvider.onRequest = { throw GetCredentialCancellationException("closed") }

        viewModel.onDeleteAccountClick()
        dialog().onPrimaryClick!!()

        assertNothingDeleted()
        assertNull(mainViewModel.toastFlow.value)
        assertTrue(navigationEvents.isEmpty())
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
    }

    @Test
    fun deleteGoogleAccount_differentGoogleAccountPicked_deletesNothing() = runTest {
        authRepository.reauthError = AuthException(AuthFailure.WRONG_ACCOUNT)

        viewModel.onDeleteAccountClick()
        dialog().onPrimaryClick!!()

        assertNothingDeleted()
        assertEquals(str(R.string.auth_error_wrong_account), mainViewModel.toastFlow.value)
    }

    @Test
    fun deleteAccount_cancelled_doesNothing() = runTest {
        collectNavigation()

        viewModel.onDeleteAccountClick()
        dialog().onSecondaryClick!!()

        assertEquals(0, tokenProvider.requestCount)
        assertNothingDeleted()
        assertTrue(navigationEvents.isEmpty())
        assertNull(mainViewModel.dialogFlow.value)
    }

    // endregion

    // region Delete account: email/password

    @Test
    fun deletePasswordAccount_correctPassword_reauthsThenDeletes() = runTest {
        collectNavigation()
        authRepository.reauthMethod = ReauthMethod.PASSWORD

        viewModel.onDeleteAccountClick()
        assertEquals(str(R.string.delete_account_message_password), dialog().message)
        assertNotNull(dialog().passwordInput)
        typePassword("secret1")
        assertEquals("secret1", dialog().passwordInput!!.value)
        dialog().onPrimaryClick!!()

        assertEquals(listOf("secret1"), authRepository.passwordReauths)
        assertEquals(0, tokenProvider.requestCount)
        assertEquals(1, authRepository.deleteCount)
        assertEquals(listOf<NavigationEvent?>(toLogin), navigationEvents)
    }

    @Test
    fun deletePasswordAccount_emptyPassword_asksForItWithoutTrying() = runTest {
        authRepository.reauthMethod = ReauthMethod.PASSWORD

        viewModel.onDeleteAccountClick()
        dialog().onPrimaryClick!!()

        assertEquals(
            str(R.string.delete_account_password_required),
            dialog().passwordInput!!.error
        )
        assertTrue(authRepository.passwordReauths.isEmpty())
        assertNothingDeleted()
    }

    @Test
    fun deletePasswordAccount_wrongPassword_reopensDialogWithErrorAndDeletesNothing() = runTest {
        authRepository.reauthMethod = ReauthMethod.PASSWORD
        authRepository.reauthError = AuthException(AuthFailure.INVALID_CREDENTIALS)

        viewModel.onDeleteAccountClick()
        typePassword("wrong")
        dialog().onPrimaryClick!!()

        assertNothingDeleted()
        assertEquals(str(R.string.delete_account_wrong_password), dialog().passwordInput!!.error)
        assertEquals("", dialog().passwordInput!!.value)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)

        // Typing again clears the error.
        typePassword("s")
        assertNull(dialog().passwordInput!!.error)
    }

    // endregion

    @Test
    fun deleteAccount_deletionFailsAfterReauth_showsErrorAndStays() = runTest {
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
