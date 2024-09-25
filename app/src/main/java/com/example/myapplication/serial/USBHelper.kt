package com.example.myapplication.serial

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import android.widget.Toast
import com.example.myapplication.util.ByteRingBuffer
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


class USBCommunicator {
    val manager: UsbManager;
    var driver: UsbSerialDriver? = null;
    var connection: UsbDeviceConnection? = null;
    val ctx: Context;
    var channel: UsbSerialPort? = null;
    constructor(ctx: Context) {
        this.ctx = ctx
        manager = ctx.getSystemService(Service.USB_SERVICE) as UsbManager;
    }

    fun getChannel(baud: Int): UsbSerialPort? {
        val driver = this.driver;
        val port = driver?.ports?.get(0)

        if (port == null) {
            Toast.makeText(ctx, "USBHelper: Failed to get port :(", Toast.LENGTH_SHORT).show();
            throw Exception("Can't get port, it's probably occupied!")
        }

        if (connection == null)
            openConnection(ctx)

        port?.open(connection)
        port?.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        this.channel = port
        return port
    }

    fun getDrivers(): MutableList<UsbSerialDriver> {
        return UsbSerialProber.getDefaultProber().findAllDrivers(manager)
    }

    fun openConnection(activity: Context): Boolean {
        val INTENT_ACTION_GRANT_USB = ctx.packageName + ".GRANT_USB";
        val driver = this.driver
        if (this.connection != null || driver == null) return this.connection != null;

        this.connection = this.manager.openDevice(driver.device);
        if (this.connection == null) {
            val flags = PendingIntent.FLAG_MUTABLE;
            val intent: Intent = Intent(INTENT_ACTION_GRANT_USB)
            intent.setPackage(this.ctx.packageName)
            val usbPermissionIntent = PendingIntent.getBroadcast(this.ctx, 0, intent, flags)
            this.manager.requestPermission(driver.device, usbPermissionIntent);
            Toast.makeText(activity, "Failed due to permissions - Try again after granting!", Toast.LENGTH_SHORT).show()
            return false;
        }

        Toast.makeText(activity, "Connection opened", Toast.LENGTH_SHORT).show()
        return true;
    }

    private val READ_TIMEOUT_MILLIS = 100
    private val BUFFER_SIZE = 1024
    private val running = AtomicBoolean(false)
    private val buffer =
        ByteRingBuffer(BUFFER_SIZE)

    fun readBytes(): ByteArray {
        synchronized(lock) {
            val content = ByteArray(buffer.getUsed())
            val bytesRead = buffer.read(content)
            return content
        }
    }

    fun available(): Int {
        synchronized(lock) {
            return buffer.getUsed()
        }
    }

    private val lock = Any()
    fun startReading() {

        if (!running.get()) {
            running.set(true)
        } else {
            Log.w("USBHELPER", "Asked to start reading when already reading")
            return;
        }
        channel = getChannel(9600)
        /*
        * Eventually might be possible to have an event-listening system here
        * */
        thread(start = true) {
            val tempBuffer = ByteArray(64)
            while (running.get()) {
                try {
                    val bytesRead = channel?.read(tempBuffer, READ_TIMEOUT_MILLIS) ?: 0
                    if (bytesRead > 0) {
                        synchronized(lock) {
                            buffer.write(tempBuffer, 0, bytesRead)
                        }
                    }
                } catch (e: java.lang.Exception) {
                    Log.e("USBHELPER", e.toString())
                    // Handle exception (e.g., log it)
                }
//                Thread.sleep(100)
            }
        }
    }

    fun write(data: ByteArray) {
        try {
            channel?.write(data, READ_TIMEOUT_MILLIS)
        } catch (e: java.lang.Exception) {
            Log.e("USBSERIAL", e.toString())
            // Handle write exception (e.g., log it)
        }
    }

    val tempBuffer = ByteArray(BUFFER_SIZE)
    fun readln(): String {

        if (!this.running.get()) {
            Log.w("USBHELPER", "readln() called but not running")
            return "";
        }

        var availableBytes = 0;

        synchronized(lock) {
            availableBytes = buffer.getUsed()
            buffer.read(tempBuffer, 0, availableBytes)
        }

        val newlineIndex = tempBuffer.indexOf('\n'.code.toByte())

        return if (newlineIndex != -1) {
            // Found a newline, extract the line and write back the rest
            val line = String(tempBuffer, 0, newlineIndex, StandardCharsets.UTF_8)
            if (newlineIndex < availableBytes - 1) {
                synchronized(lock) {
                    buffer.write(tempBuffer, newlineIndex + 1, availableBytes - newlineIndex - 1)
                }
            }
            line
        } else {
            // No newline found, write everything back and return empty string
            synchronized(lock) {
                buffer.write(tempBuffer, 0, availableBytes)
            }
            ""
        }

    }

}