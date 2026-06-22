package com.odom.sosSms.ui.sos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.odom.sosSms.R
import com.odom.sosSms.ui.components.BigButton

@Composable
fun SosCountdownScreen(
    viewModel: SosViewModel,
    onCancelled: () -> Unit,
    onSent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val messagePrefix = stringResource(R.string.sos_message_prefix)
    val locationUnavailableText = stringResource(R.string.location_unavailable_text)

    LaunchedEffect(Unit) {
        viewModel.startCountdown(messagePrefix, locationUnavailableText)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is SosUiState.Cancelled -> onCancelled()
            is SosUiState.Sent -> onSent()
            else -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val remaining = (uiState as? SosUiState.Counting)?.secondsRemaining ?: 0
            Text(
                text = stringResource(R.string.sos_countdown_title),
                fontSize = 24.sp,
            )
            Text(text = remaining.toString(), fontSize = 96.sp)
            Spacer(modifier = Modifier.height(32.dp))
            BigButton(
                text = stringResource(R.string.common_cancel),
                onClick = { viewModel.cancel() },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            )
        }
    }
}
