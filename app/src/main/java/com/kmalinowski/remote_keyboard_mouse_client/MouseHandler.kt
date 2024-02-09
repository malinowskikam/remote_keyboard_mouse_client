package com.kmalinowski.remote_keyboard_mouse_client

import android.annotation.SuppressLint
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer

enum class Gesture {
    NONE,
    LEFT_CLICK,
    LEFT_DRAG,
    LEFT_DRAG_IN_PROGRESS,
    LEFT_DRAG_FINISHED,
    LEFT_DOUBLE_CLICK,
}

class MouseHandler(private val client: UDPClient) : OnTouchListener {
    private val logTag = "MouseHandler"

    private var currentGesture = Gesture.NONE

    var lastEventTime = -1L
    var lastDownEventTime = -1L
    var lastUpEventTime = -1L
    var lastDownEventX = -1.0f
    var lastDownEventY = -1.0f


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.v(logTag, "onTouch event: $event")

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                if (currentGesture == Gesture.LEFT_CLICK && lastUpEventTime != -1L && event.eventTime - lastUpEventTime < MOUSE_GESTURE_THRESHOLD) {
                    currentGesture = Gesture.LEFT_DRAG
                }

                lastUpEventTime = -1L
                lastEventTime = event.eventTime
                lastDownEventTime = event.eventTime
                lastDownEventX = event.rawX
                lastDownEventY = event.rawY
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (lastDownEventTime != -1L && event.eventTime - lastDownEventTime < MOUSE_GESTURE_THRESHOLD) {
                    if (currentGesture == Gesture.NONE) {
                        currentGesture = Gesture.LEFT_CLICK
                    } else if (currentGesture == Gesture.LEFT_DRAG) {
                        currentGesture = Gesture.LEFT_DOUBLE_CLICK
                    }
                } else {
                    if (currentGesture == Gesture.LEFT_DRAG_IN_PROGRESS) {
                        currentGesture = Gesture.LEFT_DRAG_FINISHED
                    }
                }

                lastEventTime = event.eventTime
                lastUpEventTime = event.eventTime
                lastDownEventTime = -1L
                lastDownEventX = -1.0f
                lastDownEventY = -1.0f
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                assert(lastDownEventTime != -1L && lastDownEventX != -1.0f && lastDownEventY != -1.0f)

                val dx = event.rawX - lastDownEventX
                val dy = event.rawY - lastDownEventY

                lastDownEventX = event.rawX
                lastDownEventY = event.rawY

                sendMouseMove(dx, dy)
                return true
            }
        }
        return true
    }

    fun handleGesture() {
        val time = SystemClock.uptimeMillis()
        if (time - lastEventTime > MOUSE_GESTURE_THRESHOLD) {
            when (currentGesture) {
                Gesture.LEFT_CLICK -> {
                    Log.e(logTag, "LEFT_CLICK")
                    sendLmbDown()
                    sendLmbUp()
                    currentGesture = Gesture.NONE
                }
                Gesture.LEFT_DOUBLE_CLICK -> {
                    Log.e(logTag, "LEFT_DOUBLE_CLICK")
                    sendLmbDown()
                    sendLmbUp()
                    sendLmbDown()
                    sendLmbUp()
                    currentGesture = Gesture.NONE
                }
                Gesture.LEFT_DRAG -> {
                    Log.e(logTag, "LEFT_DRAG")
                    sendLmbDown()
                    currentGesture = Gesture.LEFT_DRAG_IN_PROGRESS
                }
                Gesture.LEFT_DRAG_FINISHED -> {
                    Log.e(logTag, "LEFT_DRAG_FINISHED")
                    sendLmbUp()
                    currentGesture = Gesture.NONE
                }
                else -> {}
            }
        }
    }

    private fun sendLmbDown() {
        ByteBuffer.allocate(1).apply {
            put(0, PACKET_ID_LMB_DOWN)
        }.array().let { runBlocking(Dispatchers.IO) { client.sendPacket(it) } }
    }

    private fun sendLmbUp() {
        ByteBuffer.allocate(1).apply {
            put(0, PACKET_ID_LMB_UP)
        }.array().let { runBlocking(Dispatchers.IO) { client.sendPacket(it) } }
    }

    private fun sendMouseMove(x: Float, y: Float) {
        ByteBuffer.allocate(9).apply {
            put(0, PACKET_ID_MOUSE_MOVE)
            putFloat(1, x)
            putFloat(5, y)
        }.array().let { runBlocking(Dispatchers.IO) { client.sendPacket(it) } }
    }
}