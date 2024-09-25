package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val ctx = this
        setContent {
            MyApplicationTheme {
                Scaffold (
                    floatingActionButton = { FloatingActionButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add a new sketch", )
                    }},
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = { Text("Droidino") })
                    }
                ) { innerPadding ->
                    Column (
                        Modifier
                            .padding(innerPadding)
                            .padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card (modifier = Modifier.fillMaxWidth()) {
                            Row (horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "GPIO Example",
                                    modifier = Modifier
                                        .background(Color.Transparent)
                                        .padding(10.dp)
                                )
                                IconButton(onClick = { ctx.startActivity(Intent(ctx, RunSketch::class.java)) }) {
                                    Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, "")
                                }
                            }
                        }

                        Card (modifier = Modifier.fillMaxWidth()) {
                            Row (horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "MediaPipe Example", Modifier.padding(10.dp))
                                IconButton(onClick = { ctx.startActivity(Intent(ctx, CameraPreviewActivity::class.java)) }) {
                                    Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, "")
                                }
                            }
                        }

                        Card (modifier = Modifier.fillMaxWidth()) {
                            Row (horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "OpenCV Example", Modifier.padding(10.dp))
                                IconButton(onClick = { }) {
                                    Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, "")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

