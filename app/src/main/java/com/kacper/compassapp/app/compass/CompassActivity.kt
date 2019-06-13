package com.kacper.compassapp.app.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.kacper.compassapp.R
import com.kacper.compassapp.databinding.ActivityCompassBinding
import com.kacper.compassapp.di.viewModel.getViewModel
import com.kacper.compassapp.utils.AnimationHelpers
import com.kacper.compassapp.utils.checkLocationPermission
import com.kacper.compassapp.utils.showToastError
import com.kacper.compassapp.utils.showToastSuccess
import com.patloew.rxlocation.RxLocation
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private val compassViewModel by lazy {
        getViewModel {
            CompassViewModel(RxLocation(this))
        }
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityCompassBinding: ActivityCompassBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_compass)

        activityCompassBinding.compassViewModel = compassViewModel
        sensorManager = (getSystemService(SENSOR_SERVICE) as SensorManager)

        initViewModelListener(activityCompassBinding)

    }

    private fun initViewModelListener(activityCompassBinding: ActivityCompassBinding) {

        compositeDisposable +=
            compassViewModel.state
                .subscribe { compassViewModelState ->

                    compassViewModelState.error?.let {
                        showToastError(it)
                    } ?: when (compassViewModelState.step) {

                        CompassStateSuccess.OnAzimuthChange -> {
                            animateCompassBorder(activityCompassBinding)
                            animateCompassMiddle(activityCompassBinding)
                        }

                        CompassStateSuccess.OnLocationRequest -> {
                            if (checkLocationPermission(shouldRequestLocation = true)) {
                                compassViewModel.requestLocation()
                            }
                        }

                        CompassStateSuccess.OnDestinationReached -> {
                            showToastSuccess(R.string.on_destination_reached_message)
                        }
                    }
                }
    }

    private fun animateCompassBorder(activityCompassBinding: ActivityCompassBinding) {
        val animation = AnimationHelpers.getRotateAnimation(
            -compassViewModel.currentAzimuth,
            -compassViewModel.azimuth
        )
        activityCompassBinding.ivCompassBase.startAnimation(animation)
    }

    private fun animateCompassMiddle(activityCompassBinding: ActivityCompassBinding) {
        val arrowAnimation = AnimationHelpers.getRotateAnimation(
            -(compassViewModel.currentAzimuth + compassViewModel.destinationBearing),
            -(compassViewModel.azimuth + compassViewModel.destinationBearing)
        )
        activityCompassBinding.ivCompassDestinationArrow.startAnimation(arrowAnimation)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkLocationPermission(shouldRequestLocation = false)) {
            compassViewModel.requestLocation()
        }
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

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
