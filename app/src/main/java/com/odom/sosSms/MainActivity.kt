package com.odom.sosSms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.odom.sosSms.ui.nav.BrainNavHost
import com.odom.sosSms.ui.theme.BrainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrainTheme {
                BrainNavHost()
            }
        }
    }
}
