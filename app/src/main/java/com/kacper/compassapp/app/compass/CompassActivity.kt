package com.kacper.compassapp.app.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProviders
import com.kacper.compassapp.R
import com.kacper.compassapp.databinding.ActivityCompassBinding
import com.kacper.compassapp.utils.setCompassAnimation

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityCompassBinding

    private lateinit var sensorManager: SensorManager

    private val compassViewModel by lazy {
        ViewModelProviders.of(this).get(CompassViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_compass)

        sensorManager = (getSystemService(SENSOR_SERVICE) as SensorManager)

        compassViewModel.azimut.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                binding.ivCompassBase.setCompassAnimation(
                    -compassViewModel.currentAzimuth,
                    -compassViewModel.azimut.get()
                )
            }
        })

    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent ?: return

        compassViewModel.onSensorValueChange(sensorEvent)
    }
}
