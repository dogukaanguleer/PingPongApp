package com.example.pingpong

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.net.toUri
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MainActivity : AppCompatActivity() {


    private lateinit var webSocketClient: WebSocketClient
    private lateinit var buttonPing: Button
    private lateinit var textViewLog: TextView

    val serverURI = URI("ws://10.0.0.138/ws/dali/devices")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonPing = findViewById(R.id.button_ping)
        textViewLog = findViewById(R.id.text_view_log)

        // WebSocketClient creation
        webSocketClient = object : WebSocketClient(serverURI) {
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
                webSocketClient.send("ping")
                textViewLog.append("Ping send!\n")
            } else {
                textViewLog.append("No connection, ping not send\n")
            }
        }

        // open connection
        webSocketClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}