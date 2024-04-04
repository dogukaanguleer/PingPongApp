package com.example.pingpong

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headers
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

class KtorTestActivity : AppCompatActivity() {

    private val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(WebSockets){
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.v("Logger Ktor =>", message)
                    }

                }
                level = LogLevel.ALL
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

    private lateinit var textViewLog: TextView
    private lateinit var socket: WebSocketSession
    private lateinit var buttonPing: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ktor_test)

        textViewLog = findViewById(R.id.text_view_log)
        buttonPing = findViewById(R.id.button_ping)



        /*
        buttonPing.setOnClickListener {

            lifecycleScope.launch {

                if (::socket.isInitialized && socket.isActive)
                    socket.send(Frame.Ping(byteArrayOf()))
            }
        }
         */

        initWebSocket()
    }

    private fun initWebSocket() = lifecycleScope.launch(Dispatchers.IO) {
        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = "10.0.0.138",
                port = 80,
                path = "/ws/dali/devices"
            ) {
                headers {
                    append("username", "atxled")
                    append("password", "atxled")
                }
                buttonPing.setOnClickListener {
                    lifecycleScope.launch {
                            send(Frame.Text("testt by cratus"))
                    }
                }

                while (true) {
                    when (val frame = incoming.receive()) {
                        is Frame.Binary -> withContext(Dispatchers.Main) {
                            textViewLog.append("Binary\n")
                        }

                        is Frame.Close -> withContext(Dispatchers.Main) {
                            textViewLog.append("Close\n")
                        }

                        is Frame.Ping -> withContext(Dispatchers.Main) {
                            textViewLog.append("Ping\n")
                        }

                        is Frame.Pong -> withContext(Dispatchers.Main) {
                            textViewLog.append("Pong\n")
                        }

                        is Frame.Text -> withContext(Dispatchers.Main) {
                            textViewLog.append("${frame.readText()}\n")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                textViewLog.append("${e.message}\n")
            }
            httpClient.close()
        }


    }

}