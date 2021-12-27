package com.example.senseyoursensors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

class MainViewModel : ViewModel() {

    private val _state = MutableLiveData("")
    val state: LiveData<String> = _state

    fun onTextChanged(newText: String) {
        _state.value = newText
    }

    private var _acceleration = MutableLiveData<String>()
    val acceleration: LiveData<String> = _acceleration

    fun updateSensorState(sensorValue: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _state.value = sensorValue
        }
    }

    fun updateAcceleration(x: Float, y: Float, z: Float) {
        viewModelScope.launch(Dispatchers.Main) {
            val totAcceleration = sqrt(x * x + y * y + z * z)
            if (totAcceleration > 1.0f ) {
                _acceleration.value = formatString(totAcceleration)
            }
            else{
                _acceleration.value = "0.0 m/s2"
            }
        }
    }

    private fun formatString(num: Float): String {
        val absValue = abs(num)
        return if (absValue > 10) {
            absValue.toString().substring(0, 4) + UNIT
        } else {
            absValue.toString().substring(0, 3) + UNIT
        }
    }

    companion object {
        const val UNIT = " m/s2"
        const val ZERO = "0.0"
    }

}