package com.example.pingpong

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.isActive

class WebSocketWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        const val CHANNEL_ID = "WebSocket"
        const val NOTIFICATION_ID = 1
    }

    private val workManager by lazy {
        WorkManager.getInstance(context)
    }

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val title = applicationContext.getString(R.string.notification_title)
        val cancel = applicationContext.getString(R.string.cancel_download)
        val intent = workManager.createCancelPendingIntent(id)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = 30_000
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.v("Ktor", message)
                    }

                }
                level = LogLevel.ALL
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

    override suspend fun doWork(): Result {
        try {
            setForeground(createForegroundInfo())
            httpClient.webSocket(
                method = HttpMethod.Get,
                request = {
                    url("ws://192.168.1.34:8080/ws/echo")
                    contentType(ContentType.Application.Json)
                }
            ) {
                while (isActive) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) Log.i("WebSocket", "Frame: ${frame.readText()}")
                    else Log.i("WebSocket", "Frame: ${frame.frameType.name}")
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("WebSocket", e.message.toString())
            return Result.retry()
        } finally {
            httpClient.close()
        }
    }

}