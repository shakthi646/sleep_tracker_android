package com.ksp.sleeptracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.billingsdk.core.BillingSDK
import com.billingsdk.model.PaywallCustomization
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.TextPrimary
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SleepTrackerApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initializeBilling()
    }

    private fun initializeBilling() {
        BillingSDK.initialize(
            context = this,
            publicKeyBase64 = "",
            enableLogging = BuildConfig.DEBUG,
            paywallCustomization = PaywallCustomization.Default.copy(
                appName = "Sleep Tracker",
                premiumTitle = "Unlock Sleep Tracker",
                subtitle = "Smart wake-ups, sleep insights, and unlimited history.",
                featureLines = listOf(
                    "Smart alarm with light-sleep window",
                    "Full sleep history & trends",
                    "Snore detection & sound analysis",
                    "No ads, ever",
                ),
                gradientStart = NightNavy,
                gradientEnd = MidnightIndigo,
                accent = AccentIndigo,
                onAccent = TextPrimary,
            ),
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TRACKING,
                "Sleep tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Ongoing sleep session notification" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALARM,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Wake-up and bedtime alarms" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDER,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Bedtime wind-down reminders" }
        )
    }

    companion object {
        const val CHANNEL_TRACKING = "sleep_tracking"
        const val CHANNEL_ALARM = "sleep_alarm"
        const val CHANNEL_REMINDER = "sleep_reminder"
    }
}
