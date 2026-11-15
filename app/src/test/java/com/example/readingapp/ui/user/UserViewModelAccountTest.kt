package com.example.readingapp.ui.user

import android.content.Context
import com.example.readingapp.MainViewModel
import com.example.readingapp.auth.AuthUser
import com.example.readingapp.auth.FakeAuthRepository
import com.example.readingapp.common.DependencyContextWrapper
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Logout on the profile screen. */
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

    private fun str(id: Int) = "res:$id"
}
