package com.warrantymitra.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun Context.openUrl(url: String) = launch(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

fun Context.dial(number: String) = launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number.filter { it.isDigit() || it == '+' })))

fun Context.mapsSearch(query: String) = openUrl("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))

fun Context.openMapAt(lat: Double, lng: Double, label: String) =
    launch(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")))

fun Context.shareText(text: String) =
    launch(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))

private fun Context.launch(intent: Intent) {
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, "No app on this phone can open that.", Toast.LENGTH_SHORT).show()
    }
}

/** Order history pages for the big Indian shopping sites. */
val ORDER_LINKS = mapOf(
    "Amazon" to "https://www.amazon.in/gp/css/order-history",
    "Flipkart" to "https://www.flipkart.com/account/orders",
    "Myntra" to "https://www.myntra.com/my/orders",
    "Croma" to "https://www.croma.com/my-account",
    "Reliance Digital" to "https://www.reliancedigital.in/",
    "Vijay Sales" to "https://www.vijaysales.com/",
    "JioMart" to "https://www.jiomart.com/",
    "Tata CLiQ" to "https://www.tatacliq.com/",
)
