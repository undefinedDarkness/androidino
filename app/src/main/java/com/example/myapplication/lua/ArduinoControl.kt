package com.example.myapplication.lua

import android.content.Context
import android.os.Handler
import com.example.myapplication.serial.USBCommunicator

class ArduinoControlInterface(ctx: Context, usb: USBCommunicator, eventLoop: Handler) : SerialInterface(ctx, usb, eventLoop) {

    companion object {
        // Opcodes
        private const val SET_PIN_MODE: Byte = 'P'.code.toByte()
        private const val DIGITAL_WRITE: Byte = 'W'.code.toByte()
        private const val DIGITAL_READ: Byte = 'R'.code.toByte()
        private const val ANALOG_READ: Byte = 'A'.code.toByte()
        private const val BLINK_TWICE: Byte = 'B'.code.toByte() // New opcode for blinking twice
        private const val ANALOG_WRITE: Byte = 'w'.code.toByte()

        // Pin modes
        const val INPUT: Byte = 'I'.code.toByte()
        const val OUTPUT: Byte = 'O'.code.toByte()

        // Digital values
        const val LOW: Byte = 'L'.code.toByte()
        const val HIGH: Byte = 'H'.code.toByte()
    }

    private fun encodePin(pin: Int): Byte {
        return (pin + 'A'.code).toByte()
    }

    fun pinMode(pin: Int, mode: String) {
        val command = byteArrayOf(SET_PIN_MODE, encodePin(pin), if (mode.uppercase() == "OUTPUT") OUTPUT else INPUT)
        write(command)
        // Wait for acknowledgment or implement error checking if needed
    }

    fun digitalWrite(pin: Int, value: String) {
        val command = byteArrayOf(DIGITAL_WRITE, encodePin(pin), if (value.uppercase() == "HIGH") HIGH else LOW)
        write(command)
        // Wait for acknowledgment or implement error checking if needed
    }

    fun digitalRead(pin: Int): Boolean {
        val command = byteArrayOf(DIGITAL_READ, encodePin(pin))//pin.toByte())
        write(command)
        // Read the response
        val response = readBytes()
        if (isResponseOk(response)) {
            return response[0] == HIGH
        } else {
            // TODO: Retry, this should not fail
            println("[digital-read] Got bad response")
            return false
        }
    }

    fun analogRead(pin: Int): Int {
        val command = byteArrayOf(ANALOG_READ, encodePin(pin))
        write(command)

        Thread.sleep(100)

        val response = this.readln()
        println("[analog-read] Got response: $response")
        return response.toIntOrNull() ?: -1;
    }

    fun analogWrite(pin: Int, value: Int) {
        val command = byteArrayOf(ANALOG_WRITE, encodePin(pin), value.toByte())
        write(command)
    }

    fun blinkTwice(delay: Int) {
        val command = byteArrayOf(BLINK_TWICE, delay.toByte())
        write(command)
        // Wait for acknowledgment or implement error checking if needed
    }
}