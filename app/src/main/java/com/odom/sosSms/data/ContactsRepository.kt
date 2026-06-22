package com.odom.sosSms.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.contactsDataStore: DataStore<Preferences> by preferencesDataStore(name = "contacts")

private val CONTACTS_KEY = stringPreferencesKey("contacts")
private const val MAX_CONTACTS = 3
private const val FIELD_SEPARATOR = "\t"
private const val RECORD_SEPARATOR = "\n"

/**
 * List order is call priority: index 0 is auto-called on SOS.
 */
class ContactsRepository(private val dataStore: DataStore<Preferences>) {

    val contacts: Flow<List<Contact>> = dataStore.data.map { prefs -> decode(prefs[CONTACTS_KEY]) }

    suspend fun addContact(contact: Contact) {
        dataStore.edit { prefs ->
            val current = decode(prefs[CONTACTS_KEY])
            if (current.size < MAX_CONTACTS) {
                prefs[CONTACTS_KEY] = encode(current + contact.sanitized())
            }
        }
    }

    suspend fun updateContact(index: Int, contact: Contact) {
        dataStore.edit { prefs ->
            val current = decode(prefs[CONTACTS_KEY]).toMutableList()
            if (index in current.indices) {
                current[index] = contact.sanitized()
                prefs[CONTACTS_KEY] = encode(current)
            }
        }
    }

    suspend fun deleteContact(index: Int) {
        dataStore.edit { prefs ->
            val current = decode(prefs[CONTACTS_KEY]).toMutableList()
            if (index in current.indices) {
                current.removeAt(index)
                prefs[CONTACTS_KEY] = encode(current)
            }
        }
    }

    suspend fun moveContact(fromIndex: Int, toIndex: Int) {
        dataStore.edit { prefs ->
            val current = decode(prefs[CONTACTS_KEY]).toMutableList()
            if (fromIndex in current.indices && toIndex in current.indices) {
                val item = current.removeAt(fromIndex)
                current.add(toIndex, item)
                prefs[CONTACTS_KEY] = encode(current)
            }
        }
    }

    private fun decode(raw: String?): List<Contact> {
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.split(RECORD_SEPARATOR).mapNotNull { record ->
            val parts = record.split(FIELD_SEPARATOR)
            if (parts.size == 2) Contact(name = parts[0], phone = parts[1]) else null
        }
    }

    private fun encode(contacts: List<Contact>): String =
        contacts.joinToString(RECORD_SEPARATOR) { "${it.name}$FIELD_SEPARATOR${it.phone}" }

    private fun Contact.sanitized(): Contact = Contact(
        name = name.replace(FIELD_SEPARATOR, " ").replace(RECORD_SEPARATOR, " ").trim(),
        phone = phone.replace(FIELD_SEPARATOR, " ").replace(RECORD_SEPARATOR, " ").trim(),
    )
}
