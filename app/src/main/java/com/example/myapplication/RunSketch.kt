package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.myapplication.serial.USBCommunicator
import com.example.myapplication.ui.theme.MyApplicationTheme


class RunSketch : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationUI()
        }
    }

    lateinit  var serialTerminal: SerialTerminal;// = SerialTerminal(driver, this)
    lateinit var  runner: LuaRunnerWithSerial;


    @Composable
    private fun ApplicationUI() {

        var (driver, setDriver) = remember {
            mutableStateOf(USBCommunicator(this))
        }

        serialTerminal = SerialTerminal(driver, this);
        runner = LuaRunnerWithSerial(driver, this)

        var (currentView, setCurrentView) = remember {
            mutableIntStateOf(0)
        }

        MyApplicationTheme {
            Scaffold(modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    Navbar(setCurrentView)
                }

            ) { innerPadding ->
                Column (Modifier.fillMaxHeight()) {
                    when (currentView) {
                        0 -> SelectDriver(driver)
                        1 -> serialTerminal.Render()
                        2 -> runner.Render()
                    }
                }

            }
        }
    }

    @Composable
    private fun Navbar(setCurrentView: (Int) -> Unit) {

        return NavigationBar {
            NavigationBarItem(
                selected = false,
                onClick = { setCurrentView(0) },
                icon = { Icon(painter = painterResource(id = R.drawable.baseline_power_24), contentDescription = "Plug socket") })
            NavigationBarItem(selected = false, onClick = { setCurrentView(1) }, icon = { Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "call"
            ) })
            NavigationBarItem(selected = false, onClick = {setCurrentView(2) }, icon = { Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Build"
            ) })
        }
    }
}
