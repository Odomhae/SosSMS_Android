package com.odom.sosSms.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odom.sosSms.data.Contact
import com.odom.sosSms.location.GeoLocation
import com.odom.sosSms.sms.SmsMessageBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ShareLocationUiState {
    data object Idle : ShareLocationUiState()
    data object Sending : ShareLocationUiState()
    data object Sent : ShareLocationUiState()
}

/**
 * Non-emergency "share my location" flow: no countdown, no auto-call, text only.
 */
class ShareLocationViewModel(
    private val contactsProvider: suspend () -> List<Contact>,
    private val locationProvider: suspend () -> GeoLocation?,
    private val sendSms: (List<Contact>, String) -> Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShareLocationUiState>(ShareLocationUiState.Idle)
    val uiState: StateFlow<ShareLocationUiState> = _uiState.asStateFlow()

    fun share(messagePrefix: String, locationUnavailableText: String) {
        viewModelScope.launch {
            _uiState.value = ShareLocationUiState.Sending
            val contacts = contactsProvider()
            val location = locationProvider()
            val message = SmsMessageBuilder.build(messagePrefix, location, locationUnavailableText)
            sendSms(contacts, message)
            _uiState.value = ShareLocationUiState.Sent
        }
    }

    fun resetState() {
        _uiState.value = ShareLocationUiState.Idle
    }
}
