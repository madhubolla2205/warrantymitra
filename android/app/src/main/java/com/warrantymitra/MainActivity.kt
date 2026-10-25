package com.warrantymitra

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.warrantymitra.reminders.Notifications
import com.warrantymitra.reminders.ReminderWorker
import com.warrantymitra.ui.AppNavigation
import com.warrantymitra.ui.theme.WarrantyMitraTheme

/** Something opened the app from outside: a reminder tap, or text or a bill photo shared from another app. */
sealed interface Incoming {
    data class OpenItem(val id: Long) : Incoming
    data class SharedText(val text: String) : Incoming
    data class SharedImage(val uri: Uri) : Incoming
}

class MainActivity : ComponentActivity() {
    private val incoming = mutableStateOf<Incoming?>(null)

    private val askNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handle(intent)
        if (Build.VERSION.SDK_INT >= 33 && !Notifications.canPost(this)) {
            askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            WarrantyMitraTheme {
                AppNavigation(incoming = incoming.value, onIncomingHandled = { incoming.value = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    private fun handle(intent: Intent?) {
        if (intent == null) return
        val itemId = intent.getLongExtra(Notifications.EXTRA_ITEM_ID, -1)
        when {
            // Debug builds only: lets testers fire today's reminders now instead of waiting for 9 AM.
            BuildConfig.DEBUG && intent.action == ACTION_RUN_REMINDERS ->
                WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<ReminderWorker>().build())
            // Debug builds only: opens a bill photo already in the app's files the way a shared one is, for the photo-reading tests.
            BuildConfig.DEBUG && intent.action == ACTION_READ_BILL ->
                intent.getStringExtra("path")?.let { incoming.value = Incoming.SharedImage(Uri.fromFile(java.io.File(it))) }
            itemId > 0 -> incoming.value = Incoming.OpenItem(itemId)
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" ->
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { incoming.value = Incoming.SharedText(it) }
            intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true -> {
                val uri: Uri? = if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                else @Suppress("DEPRECATION") intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                uri?.let { incoming.value = Incoming.SharedImage(it) }
            }
        }
    }

    companion object {
        const val ACTION_RUN_REMINDERS = "com.warrantymitra.debug.RUN_REMINDERS"
        const val ACTION_READ_BILL = "com.warrantymitra.debug.READ_BILL"
    }
}
