package com.kmalinowski.remote_keyboard_mouse_client

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    lateinit var client: UDPClient
    lateinit var mouseHandler: MouseHandler
    lateinit var mouseView: MouseView
    lateinit var statusBar: View


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        client = UDPClient(SERVER_IP, SERVER_PORT)
        mouseHandler = MouseHandler(client)
        mouseView = findViewById(R.id.mouseView)
        statusBar = findViewById(R.id.statusBar)
        mouseView.setOnTouchListener(mouseHandler)
        mouseView.setRenderer(MouseRenderer(mouseHandler))
    }

    override fun onResume() {
        super.onResume()

        Thread {
            while (true) {
                val payload = ByteBuffer.allocate(1).put(0, 1.toByte()).array()
                client.sendPacket(payload)
                val buffer = ByteArray(1)
                try {
                    client.receivePacket(buffer)
                    if (buffer[0] == 1.toByte()) {
                        runOnUiThread {
                            statusBar.setBackgroundColor(
                                ContextCompat.getColor(
                                    baseContext,
                                    R.color.green
                                )
                            )
                        }
                    }
                } catch (SocketTimeoutException: Exception) {
                    runOnUiThread {
                        statusBar.setBackgroundColor(
                            ContextCompat.getColor(
                                baseContext,
                                R.color.red
                            )
                        )
                    }
                }
                Thread.sleep(2000)
            }
        }.start()
    }
}