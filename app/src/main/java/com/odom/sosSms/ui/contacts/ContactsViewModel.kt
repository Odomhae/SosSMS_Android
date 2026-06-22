package com.odom.sosSms.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odom.sosSms.data.Contact
import com.odom.sosSms.data.ContactsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val MAX_CONTACTS = 3

/**
 * Contacts list order is call priority: index 0 is auto-called on SOS.
 */
class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    val contacts: StateFlow<List<Contact>> = repository.contacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    fun addContact(name: String, phone: String) {
        viewModelScope.launch { repository.addContact(Contact(name, phone)) }
    }

    fun updateContact(index: Int, name: String, phone: String) {
        viewModelScope.launch { repository.updateContact(index, Contact(name, phone)) }
    }

    fun deleteContact(index: Int) {
        viewModelScope.launch { repository.deleteContact(index) }
    }

    fun moveUp(index: Int) {
        viewModelScope.launch { repository.moveContact(index, index - 1) }
    }

    fun moveDown(index: Int) {
        viewModelScope.launch { repository.moveContact(index, index + 1) }
    }
}
