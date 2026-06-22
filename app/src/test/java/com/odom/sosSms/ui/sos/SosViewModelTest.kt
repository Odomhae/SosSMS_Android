package com.odom.sosSms.ui.sos

import com.odom.sosSms.data.Contact
import com.odom.sosSms.location.GeoLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startCountdown_countsDownFromThreeToOne() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.startCountdown("SOS", "no location")
        runCurrent()
        assertEquals(SosUiState.Counting(3), viewModel.uiState.value)

        advanceTimeBy(1000)
        runCurrent()
        assertEquals(SosUiState.Counting(2), viewModel.uiState.value)

        advanceTimeBy(1000)
        runCurrent()
        assertEquals(SosUiState.Counting(1), viewModel.uiState.value)
    }

    @Test
    fun cancel_beforeCountdownEnds_stopsAndDoesNotSend() = runTest(testDispatcher) {
        var smsSent = false
        var callLaunched = false
        val viewModel = createViewModel(
            sendSms = { _, _ -> smsSent = true; true },
            launchCall = { callLaunched = true; true },
        )

        viewModel.startCountdown("SOS", "no location")
        advanceTimeBy(1000)
        viewModel.cancel()
        advanceUntilIdle()

        assertEquals(SosUiState.Cancelled, viewModel.uiState.value)
        assertFalse(smsSent)
        assertFalse(callLaunched)
    }

    @Test
    fun countdownCompletes_sendsSmsAndCallsFirstContact() = runTest(testDispatcher) {
        var sentMessage: String? = null
        var sentContacts: List<Contact>? = null
        var calledNumber: String? = null
        val contacts = listOf(Contact("Mom", "111"), Contact("Dad", "222"))
        val viewModel = createViewModel(
            contactsProvider = { contacts },
            locationProvider = { GeoLocation(1.0, 2.0) },
            sendSms = { c, m -> sentContacts = c; sentMessage = m; true },
            launchCall = { calledNumber = it; true },
        )

        viewModel.startCountdown("SOS", "no location")
        advanceUntilIdle()

        assertEquals(SosUiState.Sent, viewModel.uiState.value)
        assertEquals(contacts, sentContacts)
        assertEquals("SOS https://maps.google.com/?q=1.0,2.0", sentMessage)
        assertEquals("111", calledNumber)
    }

    @Test
    fun countdownCompletes_noContacts_doesNotCallButStillSends() = runTest(testDispatcher) {
        var callLaunched = false
        var smsSent = false
        val viewModel = createViewModel(
            contactsProvider = { emptyList() },
            sendSms = { _, _ -> smsSent = true; true },
            launchCall = { callLaunched = true; true },
        )

        viewModel.startCountdown("SOS", "no location")
        advanceUntilIdle()

        assertEquals(SosUiState.Sent, viewModel.uiState.value)
        assertFalse(callLaunched)
        assertEquals(true, smsSent)
    }

    private fun createViewModel(
        contactsProvider: suspend () -> List<Contact> = { emptyList() },
        locationProvider: suspend () -> GeoLocation? = { null },
        sendSms: (List<Contact>, String) -> Boolean = { _, _ -> true },
        launchCall: (String) -> Boolean = { true },
    ) = SosViewModel(contactsProvider, locationProvider, sendSms, launchCall)
}
