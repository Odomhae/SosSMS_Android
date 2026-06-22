package com.odom.sosSms.ui.contacts

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.odom.sosSms.data.Contact
import com.odom.sosSms.data.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
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
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContactsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: CoroutineScope): ContactsViewModel {
        val tempDir = createTempDirectory().toFile()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { File(tempDir, "test.preferences_pb") }
        )
        return ContactsViewModel(ContactsRepository(dataStore))
    }

    @Test
    fun addContact_appearsInState() = runTest(testDispatcher) {
        val viewModel = createViewModel(this)

        viewModel.addContact("Mom", "111")
        advanceUntilIdle()

        assertEquals(listOf(Contact("Mom", "111")), viewModel.contacts.value)
    }

    @Test
    fun addContact_cappedAtThree() = runTest(testDispatcher) {
        val viewModel = createViewModel(this)

        viewModel.addContact("A", "1")
        viewModel.addContact("B", "2")
        viewModel.addContact("C", "3")
        viewModel.addContact("D", "4")
        advanceUntilIdle()

        assertEquals(3, viewModel.contacts.value.size)
    }

    @Test
    fun deleteContact_removesByIndex() = runTest(testDispatcher) {
        val viewModel = createViewModel(this)
        viewModel.addContact("A", "1")
        viewModel.addContact("B", "2")
        advanceUntilIdle()

        viewModel.deleteContact(0)
        advanceUntilIdle()

        assertEquals(listOf(Contact("B", "2")), viewModel.contacts.value)
    }

    @Test
    fun moveUp_swapsWithPrevious() = runTest(testDispatcher) {
        val viewModel = createViewModel(this)
        viewModel.addContact("A", "1")
        viewModel.addContact("B", "2")
        advanceUntilIdle()

        viewModel.moveUp(1)
        advanceUntilIdle()

        assertEquals(listOf(Contact("B", "2"), Contact("A", "1")), viewModel.contacts.value)
    }

    @Test
    fun moveUp_atTop_isNoOp() = runTest(testDispatcher) {
        val viewModel = createViewModel(this)
        viewModel.addContact("A", "1")
        viewModel.addContact("B", "2")
        advanceUntilIdle()

        viewModel.moveUp(0)
        advanceUntilIdle()

        assertEquals(listOf(Contact("A", "1"), Contact("B", "2")), viewModel.contacts.value)
    }

    @Test
    fun updateContact_replacesFields() = runTest(testDispatcher) {
        val viewModel = createViewModel(this)
        viewModel.addContact("A", "1")
        advanceUntilIdle()

        viewModel.updateContact(0, "A2", "2")
        advanceUntilIdle()

        assertEquals(listOf(Contact("A2", "2")), viewModel.contacts.value)
    }
}
