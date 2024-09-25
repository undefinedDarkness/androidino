package com.example.myapplication.lua

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.example.myapplication.serial.USBCommunicator
import com.example.myapplication.util.ByteRingBuffer
import com.hoho.android.usbserial.driver.UsbSerialPort
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread



interface LuaInterface {}



class AndroidLuaInterface (val ctx: Context, val eventLoop: Handler): LuaInterface {
    fun toastMessage(x: String): Unit {
        eventLoop.post {
            Toast.makeText(ctx,  x, Toast.LENGTH_LONG).show()
        }
    }

    fun sleepNs(ms: Int, ns: Int): Unit {
        Thread.sleep(ms.toLong(), ns)
    }

    fun sleepMs(x: Int): Unit {
        Thread.sleep(x.toLong())
    }
}



open class SerialInterface(private val ctx: Context, private val usb: USBCommunicator, private val eventLoop: Handler) {
//    private var channel: UsbSerialPort? = null


    var connected = false;

    fun begin(baud: Int) {
        connected = true;
        usb.startReading()
    }



    fun stop() {
//        usb.stopReading()
    }

    fun readln(): String {
        return usb.readln();
    }

    // Write raw data to the serial port


    // Write string data followed by a newline
    fun writeln(data: String) {
        val dataWithNewline = data + "\n"
        usb.write(dataWithNewline.toByteArray(StandardCharsets.UTF_8))
    }

    fun writeStr(data: String) {
        usb.write(data.toByteArray(StandardCharsets.UTF_8))
    }

    fun read(): String {
        return String(usb.readBytes(), StandardCharsets.UTF_8)
    }

    fun write(data: ByteArray) {
        usb.write(data)
    }

    fun readBytes(): ByteArray {
        return usb.readBytes()
    }



    fun isResponseOk(x: ByteArray): Boolean {
        return x[0] == 0x00.toByte()
    }


}