package com.example.pingpong

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class KtorTestActivity : AppCompatActivity() {

    private val workManager by lazy {
        WorkManager.getInstance(this)
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val CHANNEL_ID = "WebSocket"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ktor_test)
        val name = applicationContext.getString(R.string.channel_name)
        val descriptionText = applicationContext.getString(R.string.channel_description)
        val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_NONE)
        mChannel.description = descriptionText
        notificationManager.createNotificationChannel(mChannel)
        val button = findViewById<Button>(R.id.button_ping)
        button.setOnClickListener {
            createDeviceWebSocket()
        }
    }

    private fun createDeviceWebSocket() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work = PeriodicWorkRequestBuilder<WebSocketWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "WebSocketDeviceWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }
}