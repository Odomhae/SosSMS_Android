package com.odom.sosSms.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

enum class PermissionStatus { Granted, Denied, NotDetermined }

fun checkPermission(context: Context, permission: String): PermissionStatus {
    val granted = ContextCompat.checkSelfPermission(context, permission) ==
        PackageManager.PERMISSION_GRANTED
    return if (granted) PermissionStatus.Granted else PermissionStatus.NotDetermined
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

/**
 * Plain-language explanation shown before the system permission dialog fires.
 */
@Composable
fun PermissionRationaleCard(
    rationale: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = rationale)
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = confirmLabel },
            ) {
                Text(text = confirmLabel)
            }
        }
    }
}

/**
 * Shown when the user has denied a permission the feature needs; opens the
 * system Settings page since re-requesting in-app would be silently ignored.
 */
@Composable
fun PermissionDeniedCard(
    message: String,
    openSettingsLabel: String,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = message)
            Button(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = openSettingsLabel },
            ) {
                Text(text = openSettingsLabel)
            }
        }
    }
}
