package com.odom.sosSms.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odom.sosSms.data.Contact
import com.odom.sosSms.location.GeoLocation
import com.odom.sosSms.sms.SmsMessageBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SosUiState {
    data object Idle : SosUiState()
    data class Counting(val secondsRemaining: Int) : SosUiState()
    data object Cancelled : SosUiState()
    data object Sent : SosUiState()
}

private const val DEFAULT_COUNTDOWN_SECONDS = 3
private const val TICK_MILLIS = 1000L

/**
 * Countdown -> SMS-to-all-contacts -> auto-call-contact-#1 state machine.
 * Dependencies are plain functions so this can be unit tested without Android framework classes.
 */
class SosViewModel(
    private val contactsProvider: suspend () -> List<Contact>,
    private val locationProvider: suspend () -> GeoLocation?,
    private val sendSms: (List<Contact>, String) -> Boolean,
    private val launchCall: (String) -> Boolean,
    private val countdownSeconds: Int = DEFAULT_COUNTDOWN_SECONDS,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SosUiState>(SosUiState.Idle)
    val uiState: StateFlow<SosUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var pendingMessagePrefix: String = ""
    private var pendingLocationUnavailableText: String = ""

    fun startCountdown(messagePrefix: String, locationUnavailableText: String) {
        pendingMessagePrefix = messagePrefix
        pendingLocationUnavailableText = locationUnavailableText
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (remaining in countdownSeconds downTo 1) {
                _uiState.value = SosUiState.Counting(remaining)
                delay(TICK_MILLIS)
            }
            fireAlert()
        }
    }

    fun cancel() {
        countdownJob?.cancel()
        _uiState.value = SosUiState.Cancelled
    }

    private suspend fun fireAlert() {
        val contacts = contactsProvider()
        val location = locationProvider()
        val message = SmsMessageBuilder.build(pendingMessagePrefix, location, pendingLocationUnavailableText)
        sendSms(contacts, message)
        contacts.firstOrNull()?.let { launchCall(it.phone) }
        _uiState.value = SosUiState.Sent
    }
}
