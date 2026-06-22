package com.odom.sosSms.call

import android.content.Context
import android.content.Intent
import android.net.Uri

object CallLauncher {

    fun call(context: Context, phoneNumber: String): Boolean {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        return try {
            context.startActivity(intent)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
