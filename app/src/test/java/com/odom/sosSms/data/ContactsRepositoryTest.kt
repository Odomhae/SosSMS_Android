package com.odom.sosSms.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Runs under Robolectric so Build.VERSION.SDK_INT reflects a real API level (>=26):
 * DataStore's file-rename path otherwise falls back to the legacy [File.renameTo],
 * which silently fails to overwrite an existing destination on Windows JVM unit tests.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContactsRepositoryTest {

    private fun createRepo(): ContactsRepository {
        val tempDir = createTempDirectory().toFile()
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempDir, "test.preferences_pb") }
        )
        return ContactsRepository(dataStore)
    }

    @Test
    fun addContact_persistsAndIsRetrievable() = runTest {
        val repo = createRepo()

        repo.addContact(Contact("Mom", "555-1234"))

        val contacts = repo.contacts.first()
        assertEquals(1, contacts.size)
        assertEquals("Mom", contacts[0].name)
        assertEquals("555-1234", contacts[0].phone)
    }

    @Test
    fun addContact_cappedAtThreeContacts() = runTest {
        val repo = createRepo()

        repeat(4) { i -> repo.addContact(Contact("Name$i", "111$i")) }

        val contacts = repo.contacts.first()
        assertEquals(3, contacts.size)
        assertEquals("Name0", contacts[0].name)
        assertEquals("Name2", contacts[2].name)
    }

    @Test
    fun deleteContact_removesByIndex() = runTest {
        val repo = createRepo()
        repo.addContact(Contact("A", "1"))
        repo.addContact(Contact("B", "2"))

        repo.deleteContact(0)

        val contacts = repo.contacts.first()
        assertEquals(1, contacts.size)
        assertEquals("B", contacts[0].name)
    }

    @Test
    fun moveContact_reordersPriority() = runTest {
        val repo = createRepo()
        repo.addContact(Contact("A", "1"))
        repo.addContact(Contact("B", "2"))

        repo.moveContact(fromIndex = 1, toIndex = 0)

        val contacts = repo.contacts.first()
        assertEquals("B", contacts[0].name)
        assertEquals("A", contacts[1].name)
    }

    @Test
    fun updateContact_replacesFieldsAtIndex() = runTest {
        val repo = createRepo()
        repo.addContact(Contact("A", "1"))

        repo.updateContact(0, Contact("A2", "2"))

        val contacts = repo.contacts.first()
        assertEquals("A2", contacts[0].name)
        assertEquals("2", contacts[0].phone)
    }

    @Test
    fun addContact_sanitizesDelimiterCharacters() = runTest {
        val repo = createRepo()

        repo.addContact(Contact("A\tB\nC", "1\t2"))

        val contacts = repo.contacts.first()
        assertFalse(contacts[0].name.contains("\t"))
        assertFalse(contacts[0].name.contains("\n"))
        assertFalse(contacts[0].phone.contains("\t"))
    }
}
