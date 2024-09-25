package com.example.myapplication.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

class SensorDataSender(context: Context, websocketUrl: String) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Initialize WebSocket client
    private val webSocketClient: WebSocketClient = object : WebSocketClient(URI(websocketUrl)) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            println("WebSocket connection opened")
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            println("WebSocket connection closed")
        }

        override fun onMessage(message: String?) {
            println("Received message: $message")
        }

        override fun onError(ex: Exception?) {
            println("WebSocket error: ${ex?.message}")
        }
    }

    init {

        // Connect to WebSocket server
        webSocketClient.connect()

        // Register listeners for all available sensors
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensors) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorData = JSONObject().apply {
            put("sensorType", event.sensor.type)
            put("sensorName", event.sensor.name)
            put("timestamp", event.timestamp)
            put("accuracy", event.accuracy)
            put("values", JSONObject().apply {
                for (i in event.values.indices) {
                    put("value$i", event.values[i])
                }
            })
        }

        // Send sensor data over WebSocket
        GlobalScope.launch(Dispatchers.IO) {
            webSocketClient.send(sensorData.toString())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        webSocketClient.close()
    }
}