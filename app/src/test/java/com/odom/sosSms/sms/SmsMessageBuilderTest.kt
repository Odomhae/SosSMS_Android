package com.odom.sosSms.sms

import com.odom.sosSms.location.GeoLocation
import org.junit.Assert.assertEquals
import org.junit.Test

class SmsMessageBuilderTest {

    @Test
    fun build_withLocation_includesMapsLink() {
        val location = GeoLocation(latitude = 37.5665, longitude = 126.9780)

        val message = SmsMessageBuilder.build(
            messagePrefix = "SOS",
            location = location,
            locationUnavailableText = "location unavailable",
        )

        val expectedLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        assertEquals("SOS $expectedLink", message)
    }

    @Test
    fun build_withoutLocation_usesFallbackText() {
        val message = SmsMessageBuilder.build(
            messagePrefix = "SOS",
            location = null,
            locationUnavailableText = "위치 확인 불가",
        )

        assertEquals("SOS 위치 확인 불가", message)
    }
}
