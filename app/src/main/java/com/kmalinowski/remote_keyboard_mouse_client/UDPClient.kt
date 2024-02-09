package com.kmalinowski.remote_keyboard_mouse_client

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UDPClient(address: String, private val port: Int) {
    private val address = InetAddress.getByName(address)
    private val socket: DatagramSocket = DatagramSocket();

    init {
        socket.soTimeout = 2000
    }

    fun sendPacket(payload: ByteArray) {
        socket.send(DatagramPacket(payload, payload.size, address, port))
    }

    fun receivePacket(buffer: ByteArray) {
        val packet = DatagramPacket(buffer, buffer.size)
        socket.receive(packet)
    }
}