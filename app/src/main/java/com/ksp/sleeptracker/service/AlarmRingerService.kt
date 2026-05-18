package com.ksp.sleeptracker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlarmRingerService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoStop = Runnable { stopSelf() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        val uriExtra = intent?.getStringExtra(EXTRA_RINGTONE_URI).orEmpty()
        val vibrate = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true
        startRingtone(uriExtra)
        if (vibrate) startVibration()
        handler.removeCallbacks(autoStop)
        handler.postDelayed(autoStop, AUTO_STOP_MS)
        return START_NOT_STICKY
    }

    private fun startRingtone(uriString: String) {
        val uri: Uri = uriString.takeIf { it.isNotBlank() }?.let(Uri::parse)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return
        ringtone = RingtoneManager.getRingtone(this, uri)?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLooping = true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                volume = 1f
            }
            play()
        }
    }

    private fun startVibration() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator = vib
        val pattern = longArrayOf(0, 500, 500)
        val effect = VibrationEffect.createWaveform(pattern, 0)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        vib.vibrate(effect, attrs)
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoStop)
        try { ringtone?.stop() } catch (_: Throwable) {}
        ringtone = null
        try { vibrator?.cancel() } catch (_: Throwable) {}
        vibrator = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.ksp.sleeptracker.action.RINGER_START"
        const val ACTION_STOP = "com.ksp.sleeptracker.action.RINGER_STOP"
        const val EXTRA_RINGTONE_URI = "ringtone_uri"
        const val EXTRA_VIBRATE = "vibrate"
        private const val AUTO_STOP_MS = 60_000L

        fun startIntent(context: Context, ringtoneUri: String, vibrate: Boolean): Intent =
            Intent(context, AlarmRingerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RINGTONE_URI, ringtoneUri)
                putExtra(EXTRA_VIBRATE, vibrate)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, AlarmRingerService::class.java).apply { action = ACTION_STOP }
    }
}
