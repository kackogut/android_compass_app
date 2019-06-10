package com.kacper.compassapp.app.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.databinding.ObservableFloat
import androidx.lifecycle.ViewModel
import com.kacper.compassapp.utils.COMPASS_ALPHA
import com.kacper.compassapp.utils.Utils

class CompassViewModel : ViewModel() {
    var gravityArray = FloatArray(3)
    var geomagneticArray = FloatArray(3)

    var azimut = ObservableFloat(0F)
    var currentAzimuth: Float = 0F

    fun onSensorValueChange(sensorEvent: SensorEvent) {
        if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravityArray = sensorEvent.values.slice(0..2).mapIndexed { index, value ->
                value * (1 - COMPASS_ALPHA) + COMPASS_ALPHA * gravityArray[index]
            }.toFloatArray()
        }

        if (sensorEvent.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticArray = sensorEvent.values.slice(0..2).mapIndexed { index, value ->
                value * (1 - COMPASS_ALPHA) + COMPASS_ALPHA * geomagneticArray[index]
            }.toFloatArray()
        }


        val R = FloatArray(9)
        val I = FloatArray(9)

        if (SensorManager.getRotationMatrix(R, I, gravityArray, geomagneticArray)) {

            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)

            azimut.set(Utils.getAzimutValue(orientation[0]))
            currentAzimuth = azimut.get()

        }

    }
}