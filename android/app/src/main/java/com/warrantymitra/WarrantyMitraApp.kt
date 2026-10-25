package com.warrantymitra

import android.app.Application
import com.warrantymitra.reminders.Notifications
import com.warrantymitra.reminders.ReminderWorker

class WarrantyMitraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannel(this)
        ReminderWorker.schedule(this)
    }
}
