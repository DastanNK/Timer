package com.example.timer

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.timer.Constants.ACTION_SERVICE_CANCEL
import com.example.timer.Constants.ACTION_SERVICE_START
import com.example.timer.Constants.ACTION_SERVICE_STOP
import com.example.timer.Constants.NOTIFICATION_CHANNEL_ID
import com.example.timer.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.timer.Constants.NOTIFICATION_ID
import com.example.timer.Constants.STOPWATCH_STATE
import com.example.timer.ServiceHelper.resumePendingIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@ExperimentalAnimationApi
@AndroidEntryPoint
class StopwatchService : Service() {
    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager
    private var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer
    private val binder = StopwatchBinder()

    var second = mutableStateOf("00")
        private set
    var minutes = mutableStateOf("00")
        private set
    var hours = mutableStateOf("00")
        private set
    var currentState = mutableStateOf(StopwatchState.Idle)
        private set

    override fun onBind(intent: Intent?) = binder
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(STOPWATCH_STATE)) {
            StopwatchState.Started.name -> {
                setStopButton()
                startForegroundService()
                startStopwatch { hours, minutes, seconds ->
                    updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                }
            }

            StopwatchState.Stopped.name -> {
                stopStopwatch()
                setResumeButton()
            }

            StopwatchState.Canceled.name->{
                stopStopwatch()
                cancelStopwatch()
                stopForegroundService()
            }
        }
        intent?.action.let{
            when(it){
                ACTION_SERVICE_START->{
                    setStopButton()
                    startForegroundService()
                    startStopwatch { hours, minutes, seconds ->
                        updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                    }
                }
                ACTION_SERVICE_STOP->{
                    stopStopwatch()
                    setResumeButton()
                }
                ACTION_SERVICE_CANCEL->{
                    stopStopwatch()
                    cancelStopwatch()
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun stopForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cancelStopwatch() {
        duration=Duration.ZERO
        currentState.value=StopwatchState.Idle
        updateTimeUnits()
    }

    private fun stopStopwatch() {
        if(this::timer.isInitialized) {
            timer.cancel()
        }
        currentState.value = StopwatchState.Stopped

    }

    @SuppressLint("RestrictedApi")
    private fun setResumeButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Resume",
                resumePendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun startStopwatch(onTick: (h: String, m: String, s: String) -> Unit) {
        currentState.value = StopwatchState.Started
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, second.value)
        }
    }

    private fun updateNotification(hours: String, minutes: String, seconds: String) {
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                formatTime(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                )
            ).build()
        )
    }

    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@StopwatchService.hours.value = hours.toInt().pad()
            this@StopwatchService.minutes.value = minutes.pad()
            this@StopwatchService.second.value = seconds.pad()
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setStopButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Stop",
                ServiceHelper.stopPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    inner class StopwatchBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }

}

enum class StopwatchState {
    Idle,
    Started,
    Stopped,
    Canceled
}