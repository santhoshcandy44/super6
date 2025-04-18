package com.lts360.compose.ui.managers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import java.util.Locale


data class LocationCoordinates(val latitude: Double, val longitude: Double)

class LocationManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    callbackPreciseLocationUpdate: (LocationCoordinates?) -> Unit,
    callbackApproximateLocationUpdate: (LocationCoordinates?) -> Unit,
) {

    private val locationInterval = 10000L // Update interval in milliseconds
    private val locationFastestInterval = 5000L // Fastest interval for location updates
    private val locationMaxWaitTime = 30000L // Max wait time for location updates

    private val preciseLocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(locationFastestInterval)
            .setMaxUpdateDelayMillis(locationMaxWaitTime)
            .build()

    private val approximateLocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, locationInterval)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(locationFastestInterval)
            .setMaxUpdateDelayMillis(locationMaxWaitTime)
            .build()

    private val preciseLocationCallback = object : LocationCallback() {

        private var hasEmitted = false // Flag to track if value has been emitted


        override fun onLocationResult(locationResult: LocationResult) {
            if (!hasEmitted) {
                // Emit the first location and stop further emissions
                locationResult.locations.firstOrNull()?.let { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    callbackApproximateLocationUpdate(LocationCoordinates(latitude, longitude))

                    hasEmitted = true // Mark as emitted
                    // After emitting, unregister the location callback
                    removeLocationUpdates()
                }
            }

        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)

            if (!p0.isLocationAvailable) {
                callbackPreciseLocationUpdate(null)

            }
        }


    }


    private val approximateLocationCallback = object : LocationCallback() {
        private var hasEmitted = false // Flag to track if value has been emitted

        override fun onLocationResult(locationResult: LocationResult) {
            if (!hasEmitted) {
                // Emit the first location and stop further emissions
                locationResult.locations.firstOrNull()?.let { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    callbackApproximateLocationUpdate(LocationCoordinates(latitude, longitude))

                    hasEmitted = true // Mark as emitted

                    // After emitting, unregister the location callback
                    removeLocationUpdates()
                }
            }
        }


        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)

            if (!p0.isLocationAvailable) {
                callbackApproximateLocationUpdate(null)
            }
        }
    }


    fun removeLocationUpdates() {

        fusedLocationClient.removeLocationUpdates(preciseLocationCallback)
        fusedLocationClient.removeLocationUpdates(approximateLocationCallback)

    }


    fun requestLocationUpdates() {
        requestPreciseLocation()
        requestApproximateLocation()
    }


    fun checkLocationPermissions(): Boolean {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {

                return true
            }

            else -> {
                return false
            }
        }
    }

    private fun requestPreciseLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(
            preciseLocationRequest,
            preciseLocationCallback,
            null
        )

    }

    private fun requestApproximateLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(
            approximateLocationRequest,
            approximateLocationCallback,
            null
        )

    }


    companion object {

        fun getAddressName(
            context: Context,
            lat: Double, lon: Double,
            onSuccess: (String?, String?) -> Unit,
            onError: (String) -> Unit,
        ) {
            val geocoder = Geocoder(context, Locale.getDefault())

            try {
                // For API 33 and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: List<Address>) {
                            addresses.firstOrNull()?.let { address ->
                                onSuccess(formatAddressLine(address), addresses.firstOrNull()?.countryCode)
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            val error = RuntimeException("Geocoding error: $errorMessage")
                            throw error
                        }
                    })
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    onSuccess(addresses?.firstOrNull()?.getAddressLine(0), addresses?.firstOrNull()?.countryCode)
                }
            } catch (e: Exception) {
                e.message?.let {
                    onError(it)
                }
            }

        }

        private fun formatAddressLine(address: Address): String {

            val thoroughfare = address.thoroughfare
            val subThoroughfare = address.subThoroughfare
            val subLocality = address.subLocality
            val subAdminArea = address.subAdminArea
            val locality = address.locality
            val adminArea = address.adminArea
            val country = address.countryName
            val postalCode = address.postalCode

            return buildString {
                if (!thoroughfare.isNullOrEmpty()) append("$thoroughfare, ")
                if (!subThoroughfare.isNullOrEmpty()) append("$subThoroughfare, ")
                if (!subLocality.isNullOrEmpty()) append("$subLocality, ")
                if (!subAdminArea.isNullOrEmpty()) append("$subAdminArea, ")
                if (!locality.isNullOrEmpty()) append("$locality, ")
                if (!adminArea.isNullOrEmpty()) append("$adminArea, ")
                if (!country.isNullOrEmpty()) append("$country, ")
                if (!postalCode.isNullOrEmpty()) append(postalCode)

                if (endsWith(", ")) delete(length - 2, length)
            }
        }

    }

}
