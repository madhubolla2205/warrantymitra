package com.warrantymitra.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.warrantymitra.domain.readableAddress
import com.warrantymitra.domain.stripPlusCode
import java.util.Locale
import kotlin.concurrent.thread

data class Place(val lat: Double, val lng: Double, val address: String?, val city: String?, val pin: String?)

/**
 * Gets the phone's current position (for "I'm at the shop now") and turns it into an address, city and PIN.
 * Calls back on the main thread with null when the position is unavailable. Location permission must already be granted.
 */
@SuppressLint("MissingPermission")
fun fetchCurrentPlace(context: Context, onResult: (Place?) -> Unit) {
    val main = Handler(Looper.getMainLooper())
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
        .addOnSuccessListener { loc ->
            if (loc == null) onResult(null)
            else reverseGeocode(context, loc.latitude, loc.longitude) { main.post { onResult(it) } }
        }
        .addOnFailureListener { onResult(null) }
}

private fun reverseGeocode(context: Context, lat: Double, lng: Double, done: (Place) -> Unit) {
    fun toPlace(a: Address?) = Place(
        lat, lng,
        address = a?.let {
            val street = listOfNotNull(it.subThoroughfare, it.thoroughfare).joinToString(" ")
            readableAddress(street, it.subLocality).ifBlank { it.getAddressLine(0)?.let(::stripPlusCode) }
        },
        city = a?.locality ?: a?.subAdminArea,
        pin = a?.postalCode,
    )
    if (!Geocoder.isPresent()) return done(toPlace(null))
    val geocoder = Geocoder(context, Locale("en", "IN"))
    if (Build.VERSION.SDK_INT >= 33) {
        geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) = done(toPlace(addresses.firstOrNull()))
            override fun onError(errorMessage: String?) = done(toPlace(null))
        })
    } else {
        thread {
            @Suppress("DEPRECATION")
            val list = runCatching { geocoder.getFromLocation(lat, lng, 1) }.getOrNull()
            done(toPlace(list?.firstOrNull()))
        }
    }
}
