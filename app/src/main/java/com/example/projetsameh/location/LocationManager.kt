package com.example.projetsameh.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context)

    suspend fun getCurrentLocation(): Location {
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    object : CancellationToken() {
                        override fun onCanceledRequested(listener: OnTokenCanceledListener) = 
                            CancellationTokenSource().token

                        override fun isCancellationRequested() = false
                    }
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        continuation.resumeWithException(Exception("Location is null"))
                    }
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val addressText = buildString {
                        append(address.getAddressLine(0))
                        if (address.locality != null) {
                            append(", ${address.locality}")
                        }
                        if (address.postalCode != null) {
                            append(" ${address.postalCode}")
                        }
                        if (address.countryName != null) {
                            append(", ${address.countryName}")
                        }
                    }
                    continuation.resume(addressText)
                } else {
                    continuation.resumeWithException(Exception("No address found"))
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    suspend fun getLocationFromAddress(address: String): Pair<Double, Double> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val addresses = geocoder.getFromLocationName(address, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val location = addresses[0]
                    continuation.resume(Pair(location.latitude, location.longitude))
                } else {
                    continuation.resumeWithException(Exception("No location found for address"))
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
} 