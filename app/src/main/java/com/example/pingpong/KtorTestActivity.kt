package com.example.pingpong

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
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
import kotlinx.coroutines.launch

class KtorTestActivity : AppCompatActivity() {


    private lateinit var webSocketClient: HttpClient
    private lateinit var textViewLog: TextView
    private var socket: WebSocketSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ktor_test)
        webSocketClient = HttpClient {
            install(WebSockets) {
                pingInterval = 5L
            }
        }
        main()
    }

    override fun onDestroy() {
        lifecycleScope.launch(Dispatchers.IO) {
            socket?.close()
        }
        super.onDestroy()
    }

    private fun main() {

        lifecycleScope.launch(Dispatchers.IO) {
            socket = webSocketClient.webSocketSession {
                url("ws://10.0.0.138/ws/dali/devices")
                headers {
                    append("username", "atxled")
                    append("password", "atxled")
                }
                contentType(ContentType.Application.Json)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            socket?.incoming?.consumeAsFlow()?.collectLatest { frame ->
                when (frame) {
                    is Frame.Text -> {

                        runOnUiThread {
                            textViewLog.append("Received: ${frame.readText()}")
                        }
                    }

                    is Frame.Ping -> {
                        runOnUiThread {
                            textViewLog.append("Ping")
                        }

                    }

                    is Frame.Pong -> {
                        runOnUiThread {
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