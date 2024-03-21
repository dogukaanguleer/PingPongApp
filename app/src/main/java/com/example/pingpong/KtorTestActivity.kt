package com.example.pingpong

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KtorTestActivity : AppCompatActivity() {

    private val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = 10_000
            }
        }
    }

    private lateinit var textViewLog: TextView
    private lateinit var socket: WebSocketSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ktor_test)
        main()
    }

    override fun onDestroy() {
        lifecycleScope.launch(Dispatchers.IO) {
            socket.close()
            httpClient.close()
        }
        super.onDestroy()
    }

    private fun main() {

        lifecycleScope.launch(Dispatchers.IO) {
            socket = httpClient.webSocketSession {
                url("ws://10.0.0.138/ws/dali/devices")
                headers {
                    append("username", "atxled")
                    append("password", "atxled")
                }
                contentType(ContentType.Application.Json)
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