package com.example.pingpong

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.runBlocking

class KtorTestActivity : AppCompatActivity() {



    private lateinit var textViewLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ktor_test)


        main()
    }

    private fun main() {
        var webSocketClient = HttpClient() {
            install(WebSockets)
        }

        runBlocking {
            webSocketClient.webSocket(
                method = HttpMethod.Get,
                host = "10.0.0.138",
                port = 80,
                path = "/ws/dali/devices"
            ) {
                for (frame in incoming) {
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
}