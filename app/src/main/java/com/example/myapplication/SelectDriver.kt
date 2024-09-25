package com.example.myapplication

import android.content.Context
import android.hardware.usb.UsbManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.serial.USBCommunicator
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber


@Composable
fun SelectDriver(driver: USBCommunicator) {

    val (availableDrivers, setAvailableDrivers) = remember {
        mutableStateOf(listOf<UsbSerialDriver>())
    }

    val context = LocalContext.current

    Column (Modifier.safeContentPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            getAllDrivers(context, setAvailableDrivers)
        }) {
            Text("Get all available drivers")
        }
        DeviceList(availableDrivers, driver)
    }
}


fun getAllDrivers(context: Context, setAvailableDrivers: (List<UsbSerialDriver>) -> Unit) {

    // Find all available drivers from attached devices.
    val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager;
    //    val manager = getActivity().getSystemService(Context.USB_SERVICE)
    val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

    if (availableDrivers.isEmpty()) {
        Toast.makeText(context, "No drivers found", Toast.LENGTH_SHORT).show()
    }

    setAvailableDrivers(availableDrivers);
}

@Composable
fun DeviceList(availableDrivers: List<UsbSerialDriver>, chosenDriver: USBCommunicator) {
    Column (verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Card (modifier = Modifier.fillMaxWidth()) {
            Column (Modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp).height(IntrinsicSize.Min)){
                Row (horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(text = "Unknown", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "ACME Inc.", fontWeight = FontWeight.Light)
                            Text(text = "/dev/something", fontWeight = FontWeight.Light)
                        }
                    }
                    Column (verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                        Image(imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "Arrow")
                    }
                }
            }
        }
        availableDrivers.forEach {
            driver -> DeviceRow(driver, chosenDriver)
        }
    }
}

@Composable
fun DeviceRow(driver: UsbSerialDriver, chosenDriver: USBCommunicator) {

    val ctx = LocalContext.current

    Card (modifier = Modifier.fillMaxWidth().clickable {
        chosenDriver.driver = driver
        Toast.makeText(ctx, "Driver selected ${driver.device.productName ?: driver.device.deviceName}", Toast.LENGTH_SHORT).show()
    }) {
        Column (Modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp).height(IntrinsicSize.Min)){
            Row (horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = driver.device.productName ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = driver.device.manufacturerName ?: "ACME Inc.", fontWeight = FontWeight.Light)
                        Text(text = driver.device.deviceName)
                    }
                }
                Column (verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                    Image(imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "Arrow")
                }
            }
        }
    }
}