package com.example.readingapp.ui.login

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.readingapp.MainViewModel
import com.example.readingapp.R
import com.example.readingapp.auth.AuthUser
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.auth.FakeGoogleIdTokenProvider
import com.example.readingapp.auth.NoActivityException
import com.example.readingapp.common.DependencyContextWrapper
import com.example.readingapp.common.LoadingState
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.testing.MainDispatcherRule
import com.example.readingapp.ui.destinations.DashboardScreenDestination
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import androidx.lifecycle.viewModelScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mockk<Context> {
        every { getString(R.string.google_sign_in_failed) } returns SIGN_IN_FAILED
        every { getString(R.string.sign_in_failed) } returns GENERIC_FAILURE
    }
    private val tokenProvider = FakeGoogleIdTokenProvider()
    private val authRepository = FakeAuthRepository()
    private val mainViewModel = MainViewModel()
    private val navigationEvents = mutableListOf<NavigationEvent?>()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(DependencyContextWrapper(context), tokenProvider, authRepository)
        viewModel.setMainViewModel(mainViewModel)
    }

    private fun TestScope.collectNavigation() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            mainViewModel.navigationFlow.collect { navigationEvents += it }
        }
    }

    private val dashboard = NavigateTo(to = DashboardScreenDestination, popCurrent = true)

    // region Google sign-in

    @Test
    fun googleSignIn_newUser_createsUserDocumentAndNavigatesToDashboard() = runTest {
        collectNavigation()

        viewModel.onGoogleSignInClick()

        assertEquals(listOf("fake-id-token"), authRepository.receivedIdTokens)
        assertEquals(listOf("uid-1" to "Jane Austen"), authRepository.createdUserDocuments)
        assertEquals(listOf<NavigationEvent?>(dashboard), navigationEvents)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun googleSignIn_existingUser_doesNotCreateDuplicateDocument() = runTest {
        collectNavigation()
        authRepository.existingUserDocuments += "uid-1"

        viewModel.onGoogleSignInClick()

        assertTrue(authRepository.createdUserDocuments.isEmpty())
        assertEquals(listOf<NavigationEvent?>(dashboard), navigationEvents)
    }

    @Test
    fun googleSignIn_noGoogleName_usesEmailPrefixAsDisplayName() = runTest {
        authRepository.signedInUser = AuthUser(uid = "uid-2", displayName = " ", email = "bookworm@gmail.com")

        viewModel.onGoogleSignInClick()

        assertEquals(listOf("uid-2" to "bookworm"), authRepository.createdUserDocuments)
    }

    @Test
    fun googleSignIn_showsLoadingWhileWaitingForAccountPicker() = runTest {
        val token = CompletableDeferred<String>()
        tokenProvider.onRequest = { token.await() }

        viewModel.onGoogleSignInClick()
        assertTrue(mainViewModel.loadingFlow.value is LoadingState.Loading)

        token.complete("late-token")
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        assertEquals(listOf("late-token"), authRepository.receivedIdTokens)
    }

    @Test
    fun googleSignIn_clickWhileLoading_isIgnored() = runTest {
        tokenProvider.onRequest = { CompletableDeferred<String>().await() }

        viewModel.onGoogleSignInClick()
        viewModel.onGoogleSignInClick()

        assertEquals(1, tokenProvider.requestCount)
    }

    @Test
    fun googleSignIn_userCancels_noErrorAndNoNavigation() = runTest {
        collectNavigation()
        tokenProvider.onRequest = { throw GetCredentialCancellationException("cancelled") }

        viewModel.onGoogleSignInClick()

        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(navigationEvents.isEmpty())
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
    }

    @Test
    fun googleSignIn_screenClosedWhilePickerOpen_resetsLoadingWithoutError() = runTest {
        collectNavigation()
        tokenProvider.onRequest = { CompletableDeferred<String>().await() }
        viewModel.onGoogleSignInClick()
        assertTrue(mainViewModel.loadingFlow.value is LoadingState.Loading)

        // What happens when the ViewModel is cleared mid-request.
        viewModel.viewModelScope.cancel()

        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(navigationEvents.isEmpty())
    }

    @Test
    fun googleSignIn_errorWithoutMessage_showsGenericFailure() = runTest {
        authRepository.signInError = IllegalStateException()

        viewModel.onGoogleSignInClick()

        assertEquals(GENERIC_FAILURE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun googleSignIn_noActivity_showsGenericError() = runTest {
        tokenProvider.onRequest = { throw NoActivityException() }

        viewModel.onGoogleSignInClick()

        assertEquals(SIGN_IN_FAILED, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun googleSignIn_credentialError_showsGenericError() = runTest {
        tokenProvider.onRequest = { throw NoCredentialException("no accounts") }

        viewModel.onGoogleSignInClick()

        assertEquals(SIGN_IN_FAILED, viewModel.uiState.value.errorMessage)
        assertTrue(authRepository.receivedIdTokens.isEmpty())
    }

    @Test
    fun googleSignIn_firebaseFailure_showsFirebaseMessageAndStaysOnLogin() = runTest {
        collectNavigation()
        authRepository.signInError = IllegalStateException("The supplied auth credential is malformed")

        viewModel.onGoogleSignInClick()

        assertEquals("The supplied auth credential is malformed", viewModel.uiState.value.errorMessage)
        assertTrue(navigationEvents.isEmpty())
        assertTrue(authRepository.createdUserDocuments.isEmpty())
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
    }

    @Test
    fun googleSignIn_clearsPreviousError() = runTest {
        tokenProvider.onRequest = { throw NoCredentialException() }
        viewModel.onGoogleSignInClick()
        assertEquals(SIGN_IN_FAILED, viewModel.uiState.value.errorMessage)

        tokenProvider.onRequest = { "fake-id-token" }
        viewModel.onGoogleSignInClick()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    // endregion

    // region Email login / register

    @Test
    fun emailLogin_success_signsInWithFormValuesAndNavigates() = runTest {
        collectNavigation()
        viewModel.onEmailChange("reader@example.com")
        viewModel.onPasswordChange("secret")

        viewModel.onSubmitClick()

        assertEquals(listOf("reader@example.com" to "secret"), authRepository.emailSignIns)
        assertTrue(authRepository.createdUserDocuments.isEmpty())
        assertEquals(listOf<NavigationEvent?>(dashboard), navigationEvents)
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
    }

    @Test
    fun emailLogin_failure_showsMessageAndStaysOnLogin() = runTest {
        collectNavigation()
        authRepository.signInError = IllegalStateException("The password is invalid")

        viewModel.onSubmitClick()

        assertEquals("The password is invalid", viewModel.uiState.value.errorMessage)
        assertTrue(navigationEvents.isEmpty())
        assertEquals(LoadingState.Idle, mainViewModel.loadingFlow.value)
    }

    @Test
    fun register_success_createsUserDocumentFromEmailAndNavigates() = runTest {
        collectNavigation()
        authRepository.signedInUser = AuthUser(uid = "uid-3", displayName = null, email = "new.reader@example.com")
        viewModel.onSwitchScreens(LoginScreenState.SIGN_UP)
        viewModel.onEmailChange("new.reader@example.com")
        viewModel.onPasswordChange("secret1")

        viewModel.onSubmitClick()

        assertEquals(listOf("new.reader@example.com" to "secret1"), authRepository.emailRegistrations)
        assertEquals(listOf("uid-3" to "new.reader"), authRepository.createdUserDocuments)
        assertEquals(listOf<NavigationEvent?>(dashboard), navigationEvents)
    }

    @Test
    fun register_failure_doesNotCreateUserDocument() = runTest {
        authRepository.signInError = IllegalStateException("The email address is already in use")
        viewModel.onSwitchScreens(LoginScreenState.SIGN_UP)

        viewModel.onSubmitClick()

        assertEquals("The email address is already in use", viewModel.uiState.value.errorMessage)
        assertTrue(authRepository.createdUserDocuments.isEmpty())
    }

    @Test
    fun emailLogin_trimsEmailButSendsPasswordAsTyped() = runTest {
        viewModel.onEmailChange("  reader@example.com ")
        viewModel.onPasswordChange(" pass word ")

        viewModel.onSubmitClick()

        assertEquals(listOf("reader@example.com" to " pass word "), authRepository.emailSignIns)
    }

    @Test
    fun register_trimsEmail() = runTest {
        viewModel.onSwitchScreens(LoginScreenState.SIGN_UP)
        viewModel.onEmailChange("new.reader@example.com ")
        viewModel.onPasswordChange("secret1")

        viewModel.onSubmitClick()

        assertEquals(listOf("new.reader@example.com" to "secret1"), authRepository.emailRegistrations)
    }

    // endregion

    // region Form validation

    @Test
    fun emailChange_blankIsInvalid() {
        viewModel.onEmailChange("   ")
        assertFalse(viewModel.uiState.value.isEmailValid)

        viewModel.onEmailChange("reader@example.com")
        assertTrue(viewModel.uiState.value.isEmailValid)
    }

    @Test
    fun emailChange_malformedIsInvalidButKeepsInput() {
        viewModel.onEmailChange("reader@example")

        assertFalse(viewModel.uiState.value.isEmailValid)
        assertEquals("reader@example", viewModel.uiState.value.email)
    }

    @Test
    fun emailChange_trailingSpaceIsStillValid() {
        viewModel.onEmailChange("reader@example.com ")

        assertTrue(viewModel.uiState.value.isEmailValid)
    }

    @Test
    fun passwordChange_loginAcceptsAnyNonBlankPassword() {
        viewModel.onPasswordChange("a")
        assertTrue(viewModel.uiState.value.isPasswordValid)
    }

    @Test
    fun passwordChange_signUpRequiresSixCharacters() {
        viewModel.onSwitchScreens(LoginScreenState.SIGN_UP)

        viewModel.onPasswordChange("12345")
        assertFalse(viewModel.uiState.value.isPasswordValid)

        viewModel.onPasswordChange("123456")
        assertTrue(viewModel.uiState.value.isPasswordValid)
    }

    @Test
    fun switchScreens_resetsFormAndError() = runTest {
        tokenProvider.onRequest = { throw NoCredentialException() }
        viewModel.onGoogleSignInClick()
        viewModel.onEmailChange("reader@example.com")
        viewModel.onPasswordChange("secret")

        viewModel.onSwitchScreens(LoginScreenState.SIGN_UP)

        assertEquals(LoginUiState(loginScreenState = LoginScreenState.SIGN_UP), viewModel.uiState.value)
    }

    // endregion

    private companion object {
        const val SIGN_IN_FAILED = "Google sign-in failed"
        const val GENERIC_FAILURE = "Something went wrong"
    }
}
