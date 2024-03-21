package com.example.pingpong

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
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
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KtorTestActivity : AppCompatActivity() {

    private val httpClient: HttpClient by lazy {
        HttpClient(Android) {
            install(WebSockets) {
                pingInterval = 10_000
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ktor_test)
        //main()
        initWebSocket()
    }

    override fun onDestroy() {
        lifecycleScope.launch(Dispatchers.IO) {
            //socket.close()
            httpClient.close()
        }
        super.onDestroy()
    }


    private fun initWebSocket() = lifecycleScope.launch(Dispatchers.IO) {
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
            while (true) {
                when (val frame = incoming.receive()) {
                    is Frame.Binary -> withContext(Dispatchers.Main) {
                        textViewLog.append("Binary")
                    }

                    is Frame.Close -> withContext(Dispatchers.Main) {
                        textViewLog.append("Close")
                    }

                    is Frame.Ping -> withContext(Dispatchers.Main) {
                        textViewLog.append("Ping")
                    }

                    is Frame.Pong -> withContext(Dispatchers.Main) {
                        textViewLog.append("Pong")
                    }

                    is Frame.Text -> withContext(Dispatchers.Main) {
                        textViewLog.append(frame.readText())
                    }
                }
            }
        }
    }

    private fun main() {
        lifecycleScope.launch(Dispatchers.IO) {
            socket = httpClient.webSocketSession {
                url("ws://10.0.0.138/ws/dali/devices")
                headers {
                    append("username", "atxled")
                    append("password", "atxled")
                }
            }
            if (socket.isActive) withContext(Dispatchers.Main) {
                textViewLog.append("Connected")
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            socket.incoming.consumeAsFlow().collectLatest { frame ->
                when (frame) {
                    is Frame.Text -> {
                        withContext(Dispatchers.Main) {
                            textViewLog.append("Received: ${frame.readText()}")
                        }
                    }

                    is Frame.Ping -> {
                        withContext(Dispatchers.Main) {
                            textViewLog.append("Ping")
                        }

                    }

                    is Frame.Pong -> {
                        withContext(Dispatchers.Main) {
                            textViewLog.append("Pong")
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }
}