package com.odom.sosSms.sms

import android.telephony.SmsManager
import com.odom.sosSms.data.Contact

class SmsSender {

    fun sendTo(contacts: List<Contact>, message: String): Boolean {
        val smsManager = SmsManager.getDefault()
        return try {
            contacts.forEach { contact ->
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
            }
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
