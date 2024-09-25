package com.example.myapplication

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.lua.AndroidLuaInterface
import com.example.myapplication.lua.ArduinoControlInterface
import com.example.myapplication.serial.USBCommunicator
import org.luaj.vm2.Globals
import org.luaj.vm2.lib.jse.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.Timer
import java.util.TimerTask



class LuaRunnerWithSerial(private val usb_comm: USBCommunicator, private val ctx: Context) {
    lateinit var appendToLog: (String) -> Unit
    lateinit var incrementElapsedTime: () -> Unit

    @Composable
    fun Render() {
        var applicationLog = remember {
            mutableStateOf("")
        }

        this.appendToLog = { str ->
            applicationLog.value += str
        }

        var elapsedTime = remember {
            mutableStateOf("0.0 s")
        }

        this.incrementElapsedTime = {
            val currentTimeInTenths = (elapsedTime.value.removeSuffix(" s").toFloat() * 10).toInt()
            val newTimeInTenths = currentTimeInTenths + 1
            val seconds = newTimeInTenths / 10
            val tenths = newTimeInTenths % 10
            elapsedTime.value = String.format("%d.%d s", seconds, tenths)
        }

        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxHeight()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        elapsedTime.value = "0.0 s"
                        System.out.println("[LUA] Running.")
                        Run()
                    }) {
                        Text(text = "RUN")
                    }

                    Button(onClick = {
                        applicationLog.value = ""
//                        luaThread.interrupt()
                    }) {
                        Text(text = "CLEAR")
                    }

                    Button(onClick = {
                        luaThread.interrupt()
                        elapsedTime.value = "0.0 s"
                    }) {
                        Text(text = "END", color = Color.LightGray)
                    }
                }

                Text(text = elapsedTime.value)
            }

            Text(
                text = applicationLog.value, modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(
                        rememberScrollState()
                    )
            )
        }
    }

    //    lateinit var luaEnviroment: Globals; // JsePlatform.standardGlobals()
    // NOTE: Main problem with this approach is that isn't live update
    val eventLoop: Handler = Handler(Looper.getMainLooper())

    val captureBuffer = ByteArrayOutputStream()
    val captureStream = PrintStream(captureBuffer)


    val androidInterface = CoerceJavaToLua.coerce(AndroidLuaInterface(ctx, eventLoop))
//    val serialInterface = CoerceJavaToLua.coerce(SerialInterface(ctx, usb_comm, eventLoop))
    val arduinoControlInterface = CoerceJavaToLua.coerce(ArduinoControlInterface(ctx, usb_comm, eventLoop))
    private fun hookLuaEnv(luaEnviroment: Globals) {
        luaEnviroment.set("Android", androidInterface)
        luaEnviroment.set("Serial", arduinoControlInterface)
        luaEnviroment.set("Ctrl", arduinoControlInterface)
        luaEnviroment.STDOUT = captureStream
    }

    lateinit var luaThread: Thread;
    lateinit var timer: Timer;
    private fun Run() {
//        captureBuffer.reset()
        appendToLog(" -- APPLICATION STARTED --\n---\n")

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                incrementElapsedTime()
            }
        }, 0, 100)

        val luaSourceCode =
            ctx.resources.openRawResource(R.raw.a1).bufferedReader().use { it.readText() }
//        val chunk = luaEnviroment.load(luaSourceCode)
//        chunk.call()


        /*
        * For some reason, the while loop here
        * overflows the Lua / Java stack very quickly, I don't know what I've done wrong
        * and I've not been able to find any example or anything similar online :(
        * */
        luaThread = Thread(null, {
            Looper.prepare()
            val env = JsePlatform.standardGlobals()
            hookLuaEnv(env)
            env.load(luaSourceCode).call()

            val setupFn = env.get("setup")
            val loopFn = env.get("loop")

            try {
                setupFn.call()
                while (!Thread.currentThread().isInterrupted) {
                    loopFn.call()
//                    if (captureBuffer.size()) {
                        appendToLog(captureBuffer.toString())
                        captureBuffer.reset()
//                    }
                    Thread.sleep(100)
                }
            } catch (e: Exception) {
                appendToLog("-- APPLICATION TERMINATED BECAUSE OF ERROR --")
                appendToLog(e.toString())
                println(e.toString())
            } finally {

                appendToLog(captureBuffer.toString())
                appendToLog("\n-- APPLICATION FINISHED --")
                timer.cancel()
            }

        }, "myThread", 16000)
        luaThread.start()

        // Append the Lua output to the log

    }
}