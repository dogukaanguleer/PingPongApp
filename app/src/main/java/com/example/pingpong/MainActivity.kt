package com.example.pingpong

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.net.toUri
import io.ktor.client.HttpClient

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod

import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.runBlocking
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import java.net.URI

class MainActivity : AppCompatActivity() {


    //private lateinit var webSocketClient: WebSocketClient
    private lateinit var buttonPing: Button
    private lateinit var textViewLog: TextView
    private lateinit var webSocketClient: HttpClient


    val serverURI = URI("ws://10.0.0.138/ws/dali/devices")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonPing = findViewById(R.id.button_ping)
        textViewLog = findViewById(R.id.text_view_log)


        /*

        // WebSocketClient creation
        webSocketClient = object : WebSocketClient(serverURI) {

            override fun onWebsocketPing(conn: WebSocket?, f: Framedata?) {
                super.onWebsocketPing(conn, f)
                runOnUiThread {
                    textViewLog.append("Ping\n")
                }
            }

            override fun onWebsocketPong(conn: WebSocket?, f: Framedata?) {
                super.onWebsocketPong(conn, f)
                runOnUiThread {
                    textViewLog.append("Pong\n")
                }
            }

            override fun onOpen(serverHandshake: ServerHandshake) {
                Log.d("WebSocket", "Connection Open!")
                runOnUiThread {
                    textViewLog.append("Connection Open!\n")
                }
            }

            override fun onMessage(message: String) {
                Log.d("WebSocket", "Message: $message")
                runOnUiThread {
                    textViewLog.append("Message: $message\n")
                }
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.d("WebSocket", "Connection closed: $code - $reason")
                runOnUiThread {
                    textViewLog.append("Connection closed: $code - $reason\n")
                }
            }

            override fun onError(ex: Exception) {
                Log.e("WebSocket", "error: ${ex.message}")
                runOnUiThread {
                    textViewLog.append("error: ${ex.message}\n")
                }
            }
        }

        // Ping button click

        buttonPing.setOnClickListener {
            if (webSocketClient.isOpen) {
                webSocketClient.sendPing()
            } else {
                textViewLog.append("No connection, ping not send\n")
                webSocketClient.reconnect()
            }
                                    send(Frame.Pong(frame.buffer))

        }
        // open connection
        webSocketClient.connect()

         */
        main()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }



    private fun main() {
        webSocketClient = HttpClient() {
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