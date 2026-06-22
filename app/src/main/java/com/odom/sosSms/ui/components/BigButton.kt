package com.odom.sosSms.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MinTouchTarget = 96.dp

/**
 * Large, high-contrast, TalkBack-labeled button shared by the SOS, share-location,
 * and cancel-countdown actions. Touch target meets the doc's >=96dp accessibility floor.
 */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    contentDescription: String = text,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(MinTouchTarget)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Text(text = text, fontSize = 28.sp)
    }
}
