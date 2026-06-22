package com.odom.sosSms.ui.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.common.api.ResolvableApiException
import com.odom.sosSms.R
import com.odom.sosSms.location.LocationProvider
import com.odom.sosSms.permissions.PermissionDeniedCard
import com.odom.sosSms.permissions.PermissionRationaleCard
import com.odom.sosSms.permissions.PermissionStatus
import com.odom.sosSms.permissions.checkPermission
import com.odom.sosSms.permissions.openAppSettings
import com.odom.sosSms.ui.components.BigButton
import com.odom.sosSms.ui.theme.OnShareBlue
import com.odom.sosSms.ui.theme.OnSosRed
import com.odom.sosSms.ui.theme.ShareBlue
import com.odom.sosSms.ui.theme.SosRed

private val STARTUP_PERMISSIONS = listOf(
    Manifest.permission.SEND_SMS,
    Manifest.permission.CALL_PHONE,
    Manifest.permission.ACCESS_FINE_LOCATION,
)

private sealed class PermissionFlowStep {
    data class ShowRationale(val permission: String) : PermissionFlowStep()
    data class ShowDenied(val permission: String) : PermissionFlowStep()
}

@Composable
fun HomeScreen(
    onSosReady: () -> Unit,
    onNavigateToContacts: () -> Unit,
    shareLocationViewModel: ShareLocationViewModel,
    locationProvider: LocationProvider,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var actionQueue by remember { mutableStateOf<List<String>>(emptyList()) }
    var mandatorySet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var currentStep by remember { mutableStateOf<PermissionFlowStep?>(null) }
    var onActionReady by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingLocationCheck by remember { mutableStateOf(false) }

    fun cancelFlow() {
        actionQueue = emptyList()
        currentStep = null
        onActionReady = null
        pendingLocationCheck = false
    }

    fun proceedToReady() {
        val action = onActionReady
        onActionReady = null
        action?.invoke()
    }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        // Best-effort: proceed whether the user turned location on or dismissed the dialog.
        proceedToReady()
    }

    fun maybeRequestLocationEnable() {
        if (!pendingLocationCheck) {
            proceedToReady()
            return
        }
        pendingLocationCheck = false
        if (locationProvider.isLocationEnabled()) {
            proceedToReady()
            return
        }
        locationProvider.checkLocationSettings()
            .addOnSuccessListener { proceedToReady() }
            .addOnFailureListener { exception ->
                val resolvable = exception as? ResolvableApiException
                if (resolvable != null) {
                    locationSettingsLauncher.launch(
                        IntentSenderRequest.Builder(resolvable.resolution).build(),
                    )
                } else {
                    proceedToReady()
                }
            }
    }

    fun advance() {
        val next = actionQueue.firstOrNull()
        if (next == null) {
            currentStep = null
            maybeRequestLocationEnable()
            return
        }
        actionQueue = actionQueue.drop(1)
        if (checkPermission(context, next) == PermissionStatus.Granted) {
            advance()
        } else {
            currentStep = PermissionFlowStep.ShowRationale(next)
        }
    }

    fun startFlow(
        permissions: List<String>,
        mandatory: Set<String>,
        needsLocationCheck: Boolean = false,
        onReady: () -> Unit,
    ) {
        mandatorySet = mandatory
        actionQueue = permissions
        pendingLocationCheck = needsLocationCheck
        onActionReady = onReady
        advance()
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val permission = (currentStep as? PermissionFlowStep.ShowRationale)?.permission
        currentStep = null
        if (granted || permission == null || permission !in mandatorySet) {
            advance()
        } else {
            currentStep = PermissionFlowStep.ShowDenied(permission)
        }
    }

    LaunchedEffect(Unit) {
        startFlow(permissions = STARTUP_PERMISSIONS, mandatory = emptySet(), onReady = {})
    }

    val shareMessagePrefix = stringResource(R.string.share_message_prefix)
    val locationUnavailableText = stringResource(R.string.location_unavailable_text)
    val sentToastText = stringResource(R.string.share_sent_toast)

    val shareState by shareLocationViewModel.uiState.collectAsState()
    LaunchedEffect(shareState) {
        if (shareState is ShareLocationUiState.Sent) {
            Toast.makeText(context, sentToastText, Toast.LENGTH_SHORT).show()
            shareLocationViewModel.resetState()
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextButton(onClick = onNavigateToContacts) {
                Text(stringResource(R.string.home_settings_button))
            }

            BigButton(
                text = stringResource(R.string.home_sos_button),
                onClick = {
                    startFlow(
                        permissions = listOf(
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ),
                        mandatory = setOf(Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE),
                        needsLocationCheck = true,
                        onReady = onSosReady,
                    )
                },
                containerColor = SosRed,
                contentColor = OnSosRed,
            )

            BigButton(
                text = stringResource(R.string.home_share_location_button),
                onClick = {
                    startFlow(
                        permissions = listOf(
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ),
                        mandatory = setOf(Manifest.permission.SEND_SMS),
                        needsLocationCheck = true,
                        onReady = {
                            shareLocationViewModel.share(shareMessagePrefix, locationUnavailableText)
                        },
                    )
                },
                containerColor = ShareBlue,
                contentColor = OnShareBlue,
            )

            when (val step = currentStep) {
                is PermissionFlowStep.ShowRationale -> {
                    PermissionRationaleCard(
                        rationale = rationaleTextFor(step.permission),
                        confirmLabel = stringResource(R.string.permission_grant_button),
                        onConfirm = { launcher.launch(step.permission) },
                    )
                    TextButton(onClick = { cancelFlow() }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }

                is PermissionFlowStep.ShowDenied -> {
                    PermissionDeniedCard(
                        message = deniedTextFor(step.permission),
                        openSettingsLabel = stringResource(R.string.permission_open_settings_button),
                        onOpenSettings = { openAppSettings(context) },
                    )
                    TextButton(onClick = { cancelFlow() }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }

                null -> Unit
            }
        }
    }
}

@Composable
private fun rationaleTextFor(permission: String): String = when (permission) {
    Manifest.permission.SEND_SMS -> stringResource(R.string.home_sms_permission_rationale)
    Manifest.permission.CALL_PHONE -> stringResource(R.string.home_call_permission_rationale)
    Manifest.permission.ACCESS_FINE_LOCATION -> stringResource(R.string.home_location_permission_rationale)
    else -> ""
}

@Composable
private fun deniedTextFor(permission: String): String = when (permission) {
    Manifest.permission.SEND_SMS -> stringResource(R.string.permission_denied_sms)
    Manifest.permission.CALL_PHONE -> stringResource(R.string.permission_denied_call)
    Manifest.permission.ACCESS_FINE_LOCATION -> stringResource(R.string.permission_denied_location)
    else -> ""
}
