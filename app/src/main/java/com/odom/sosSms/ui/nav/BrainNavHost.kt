package com.odom.sosSms.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.odom.sosSms.call.CallLauncher
import com.odom.sosSms.data.ContactsRepository
import com.odom.sosSms.data.contactsDataStore
import com.odom.sosSms.location.LocationProvider
import com.odom.sosSms.sms.SmsSender
import com.odom.sosSms.ui.contacts.ContactsScreen
import com.odom.sosSms.ui.contacts.ContactsViewModel
import com.odom.sosSms.ui.home.HomeScreen
import com.odom.sosSms.ui.home.ShareLocationViewModel
import com.odom.sosSms.ui.sos.SosCountdownScreen
import com.odom.sosSms.ui.sos.SosViewModel
import kotlinx.coroutines.flow.first

private const val ROUTE_HOME = "home"
private const val ROUTE_SOS = "sos"
private const val ROUTE_CONTACTS = "contacts"

@Composable
fun BrainNavHost(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context.contactsDataStore) }
    val locationProvider = remember { LocationProvider(context) }
    val smsSender = remember { SmsSender() }

    // Guards the contacts-onboarding redirect to once per app launch: this state
    // is hoisted above the NavHost so it survives Home <-> Contacts back-stack
    // navigation, instead of resetting every time Home recomposes.
    var contactsOnboardingChecked by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            val shareViewModel: ShareLocationViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ShareLocationViewModel(
                            contactsProvider = { repository.contacts.first() },
                            locationProvider = { locationProvider.getLastLocation() },
                            sendSms = { contacts, message -> smsSender.sendTo(contacts, message) },
                        )
                    }
                },
            )
            HomeScreen(
                onSosReady = { navController.navigate(ROUTE_SOS) },
                onNavigateToContacts = { navController.navigate(ROUTE_CONTACTS) },
                shareLocationViewModel = shareViewModel,
                locationProvider = locationProvider,
                hasNoSavedContacts = {
                    if (contactsOnboardingChecked) {
                        false
                    } else {
                        contactsOnboardingChecked = true
                        repository.contacts.first().isEmpty()
                    }
                },
            )
        }
        composable(ROUTE_SOS) {
            val sosViewModel: SosViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        SosViewModel(
                            contactsProvider = { repository.contacts.first() },
                            locationProvider = { locationProvider.getLastLocation() },
                            sendSms = { contacts, message -> smsSender.sendTo(contacts, message) },
                            launchCall = { phone -> CallLauncher.call(context, phone) },
                        )
                    }
                },
            )
            SosCountdownScreen(
                viewModel = sosViewModel,
                onCancelled = { navController.popBackStack() },
                onSent = { navController.popBackStack() },
            )
        }
        composable(ROUTE_CONTACTS) {
            val contactsViewModel: ContactsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ContactsViewModel(repository) }
                },
            )
            ContactsScreen(
                viewModel = contactsViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
