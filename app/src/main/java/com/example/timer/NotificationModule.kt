package com.example.timer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.app.NotificationCompat
import com.example.timer.Constants.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent

@Module
@ExperimentalAnimationApi
@InstallIn(SingletonComponent::class)
object NotificationModule {
    @ServiceScoped
    @Provides
    fun NotificationBuilder(@ApplicationContext context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).setContentTitle("Stopwatch App")
            .setContentText("00:00:00").setSmallIcon(R.drawable.baseline_access_time_24).setOngoing(true)
            .addAction(0, "Stop", ServiceHelper.stopPendingIntent(context))
            .addAction(0, "Cancel", ServiceHelper.cancelPendingIntent(context))
            .setContentIntent(ServiceHelper.clickPendingIntent(context))
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}