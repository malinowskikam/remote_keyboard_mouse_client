package com.kmalinowski.remote_keyboard_mouse_client

const val SERVER_IP = "192.168.0.185"
const val SERVER_PORT = 6789

const val MOUSE_GESTURE_THRESHOLD = 150

const val PACKET_ID_MOUSE_MOVE: Byte = 0x02
const val PACKET_ID_LMB_DOWN: Byte = 0x03
const val PACKET_ID_LMB_UP: Byte = 0x04