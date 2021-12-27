package com.example.senseyoursensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.senseyoursensors.ui.theme.Accent
import com.example.senseyoursensors.ui.theme.LightPrimary
import com.example.senseyoursensors.ui.theme.Primary
import com.example.senseyoursensors.ui.theme.RippleCustomTheme

class ListActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensors: List<Sensor>
    private var linearAcceleration: Sensor? = null
    private lateinit var viewModel: MainViewModel
    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {

        viewModel = MainViewModel()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        super.onCreate(savedInstanceState)
        setContent {
            MainApplication()
        }
    }

    @Composable
    fun MainApplication() {
        navController = rememberNavController()
        NavHost(navController, "main") {
            composable("main") {
                MainScreen()
            }
            composable(
                "another/{name}",
                arguments = listOf(navArgument("name") {
                    type = NavType.StringType
                })
            ) {
                AnotherScreen(it.arguments!!.getString("name") ?: "")
            }
        }
    }


    @Composable
    fun MainScreen() {
        Scaffold(topBar = { AppBar("Home") }) {
            Surface(
                color = LightPrimary,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn() {
                    items(sensors) { sensor ->
                        SensorCard(sensor.name)
                    }
                }
            }
        }
    }


    @Composable
    fun AnotherScreen(name: String) {
        resetSensorListener(name)
        val state = viewModel.state.observeAsState("")

        Scaffold(topBar = { AppBar(name, true) }) {
            Surface(
                color = LightPrimary,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.value, color = Primary)
                }
            }
        }

    }

    @Composable
    private fun AppBar(name: String, isDetail: Boolean = false) {
        val iconId = if (isDetail) R.drawable.ic_back else R.drawable.ic_home
        TopAppBar(
            navigationIcon = {
                Icon(
                    painter = painterResource(id = iconId),
                    "",
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .clickable {
                            if (isDetail) {
                                navController.navigate("main")
                            }
                        },
                    tint = Color.White,
                )
            },
            title = {
                Text(name, color = Color.White)
            },
            backgroundColor = Primary
        )
    }

    @Composable
    private fun SensorCard(sensorName: String) {

        CompositionLocalProvider(LocalRippleTheme provides RippleCustomTheme) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentHeight(align = Alignment.Top),
                elevation = 8.dp,
                backgroundColor = Color.White,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SensorContent(sensorName)
                    IconButton(
                        onClick = {
                            navController.navigate(
                                "another/${
                                    sensorName.replace(
                                        " ",
                                        ""
                                    )
                                }"
                            )
                        }, modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_forward),
                            "",
                            modifier = Modifier.padding(start = 12.dp),
                            tint = Accent,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SensorContent(sensorName: String) {
        val shortenedText = if (sensorName.length > 30) {
            sensorName.substring(0, 30) + "..."
        } else {
            sensorName
        }
        Text(shortenedText, modifier = Modifier.padding(16.dp))
    }


    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MainScreen()
    }

    private fun resetSensorListener(sensorName: String) {
        sensorManager.unregisterListener(this)
        val sensor = sensors.filter { it -> it.name.replace(" ", "") == sensorName }[0]
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(p0: SensorEvent?) {

        val sensorValue = (p0?.values?.get(0) ?: 0.0f).toString()
        viewModel.updateSensorState(sensorValue)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

}