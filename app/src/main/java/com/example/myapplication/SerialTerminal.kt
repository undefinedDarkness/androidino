package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.serial.USBCommunicator
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
//import java.lang.Exception
import java.nio.charset.StandardCharsets
import kotlin.Exception
import kotlin.concurrent.thread

class SerialTerminal(private val driver: USBCommunicator, private val activity: Activity) {

    private var updateSerialContent: (String) -> Unit = { }
    private var baud = 9600
    private val eventLoop: Handler = Handler(Looper.getMainLooper())


    private var buffer = StringBuilder()
    private var isRunning = false
    private lateinit var readThread: Thread

    fun startCommunication() {
        driver.startReading();
        isRunning = true;
        readThread = thread {
            while (isRunning) {
                buffer.append(driver.readln())
                eventLoop.post {
                    updateSerialContent(buffer.toString())
                }
                Thread.sleep(100)
            }
        }
    }


    fun stopCommunication() {
        isRunning = false;
        readThread.interrupt()
        // Leave other stuff open, it's USBCommunicator's problem
    }

    fun sendMessage(message: String?) {
        message ?: return;
//        driver.writeS(message)
        driver.write(message.toByteArray(StandardCharsets.UTF_8))
    }

    @Composable
    fun Render() {
        var (serialContent, setSerialContent) = remember {
            mutableStateOf("")
        }
        this.updateSerialContent = { newContent ->
            setSerialContent(newContent)
        }

        val ctx = LocalContext.current;

        var baudRate = remember { mutableStateOf("9600") }
        var serialInput = remember {
            mutableStateOf("")
        }

        val onBaudRateChange: (String) -> Unit = {  newRate ->
            baudRate.value = newRate
            this.baud = baudRate.value.toInt()
            driver.channel?.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        }

        Column (modifier = Modifier
            .safeContentPadding()
            .fillMaxHeight()
            .padding(8.dp, 4.dp, 8.dp, 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Row (horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(modifier = Modifier.widthIn(Dp.Unspecified, 100.dp), value = baudRate.value, onValueChange = onBaudRateChange, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
                Button(onClick = {
                    if (driver.openConnection(activity)) {
                        startCommunication()
                    } else {
                        Toast.makeText(ctx, "Failed to open connection, check if you selected a device and granted permissions", Toast.LENGTH_LONG).show();
                    }
                }) {
                    Text(text = "Connect")
                }
                Button(onClick = {
                    stopCommunication()
                }) {
                    Text(text = "Disconnect")
                }
            }
            Row {
                TextField(value = serialInput.value, onValueChange = { value -> serialInput.value = value })
                IconButton(onClick = { sendMessage(serialInput.value) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
            Text(
                text = serialContent,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(4.dp)
                    .background(color = Color.LightGray)

            )
        }
    }
}