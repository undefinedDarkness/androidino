package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.sensors.SensorDataSender
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.concurrent.thread

class SensorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val ctx = this
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier
                        .padding(innerPadding)
                        .safeContentPadding()
                        .fillMaxSize()) {
                        UI(ctx)
                    }
                }
            }
        }
    }

    lateinit  var sensorThread: Thread
    fun connect(websocketUrl: String) {
        val ctx = this
        sensorThread = thread {
            SensorDataSender(this, websocketUrl)
        }
    }

    @Composable
    fun UI(ctx: Context) {
        val websocketUrl = remember { mutableStateOf("") }

        Column {
            TextField(value = websocketUrl.value, onValueChange = { value ->
                websocketUrl.value = value
            })
            Button(onClick = { connect(websocketUrl.value) }) {
             Text(text = "Connect")
            }
        }
    }
}

