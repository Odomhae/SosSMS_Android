package com.odom.sosSms.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val SETTINGS_CHECK_INTERVAL_MILLIS = 10_000L

class LocationProvider(private val context: Context) {

    suspend fun getLastLocation(): GeoLocation? {
        if (!hasLocationPermission()) return null

        return try {
            suspendCancellableCoroutine { continuation ->
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.lastLocation
                    .addOnSuccessListener { location ->
                        val result = location?.let { GeoLocation(it.latitude, it.longitude) }
                        if (continuation.isActive) continuation.resume(result)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
            }
        } catch (e: SecurityException) {
            null
        }
    }

    /** Whether any location provider (GPS or network) is currently turned on. */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    /** Resolvable via Play Services' one-tap "turn on location" dialog on failure. */
    fun checkLocationSettings(): Task<LocationSettingsResponse> {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            SETTINGS_CHECK_INTERVAL_MILLIS,
        ).build()
        val settingsRequest = LocationSettingsRequest.Builder().addLocationRequest(request).build()
        return LocationServices.getSettingsClient(context).checkLocationSettings(settingsRequest)
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
