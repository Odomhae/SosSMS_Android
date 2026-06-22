package com.odom.sosSms.sms

import com.odom.sosSms.location.GeoLocation

object SmsMessageBuilder {

    fun build(messagePrefix: String, location: GeoLocation?, locationUnavailableText: String): String {
        val locationPart = if (location != null) {
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            locationUnavailableText
        }
        return "$messagePrefix $locationPart"
    }
}
