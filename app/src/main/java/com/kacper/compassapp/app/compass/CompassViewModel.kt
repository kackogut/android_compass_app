package com.kacper.compassapp.app.compass

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationRequest
import com.kacper.compassapp.R
import com.kacper.compassapp.utils.LOCATION_REQUEST_INTERVAL
import com.kacper.compassapp.utils.Utils
import com.patloew.rxlocation.RxLocation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class CompassViewModel(
    private val rxLocation: RxLocation
) : ViewModel() {

    val state = PublishSubject.create<CompassState>()

    var gravityArray = FloatArray(3)
    var geomagneticArray = FloatArray(3)

    var azimuth: Float = 0F
    var currentAzimuth: Float = 0F
    var destinationBearing: Float = 0F

    private var compositeDisposable = CompositeDisposable()

    var destinationLat = ObservableField<String>()
    var destinationLon = ObservableField<String>()
    var currentDestination = ObservableField<Location?>()

    var isNavigationStarted = ObservableBoolean(false)

    fun onSensorValueChange(sensorEvent: SensorEvent) {
        if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravityArray = Utils.convertToSensorArray(gravityArray, sensorEvent)
        }

        if (sensorEvent.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticArray = Utils.convertToSensorArray(geomagneticArray, sensorEvent)
        }


        val rotationArray = FloatArray(9)
        val inclinationArray = FloatArray(9)

        if (SensorManager.getRotationMatrix(
                rotationArray,
                inclinationArray,
                gravityArray,
                geomagneticArray
            )
        ) {

            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationArray, orientation)

            azimuth = Utils.getAzimuthValue(orientation[0])
            state.onNext(CompassState(CompassStateSuccess.OnAzimuthChange))
            currentAzimuth = azimuth

        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        isNavigationStarted.set(true)

        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(LOCATION_REQUEST_INTERVAL)

        compositeDisposable +=
            rxLocation.location().updates(locationRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ currentLocation ->

                    if (currentLocation.latitude == currentDestination.get()?.latitude
                        && currentLocation.longitude == currentDestination.get()?.longitude
                    ) {
                        resetDestination()
                        state.onNext(CompassState(CompassStateSuccess.OnLocationRequest))

                    } else {
                        destinationBearing =
                            currentLocation.bearingTo(currentDestination.get())

                    }
                }, {
                    Timber.e(it)
                })

    }

    private fun resetDestination() {
        isNavigationStarted.set(false)
        destinationLat.set("")
        destinationLon.set("")
    }

    fun onNavigateClick() {
        if (isNavigationStarted.get()) {
            resetDestination()
        } else {
            if (destinationLat.get().isNullOrEmpty() || destinationLon.get().isNullOrEmpty()) {
                state.onNext(CompassState(error = R.string.error_invalid_latitude_longitude))
            } else {
                currentDestination.set(Location(LocationManager.GPS_PROVIDER))
                currentDestination.get()?.let {
                    it.latitude = destinationLat.get()!!.toDouble()
                    it.longitude = destinationLon.get()!!.toDouble()
                }
                state.onNext(CompassState(CompassStateSuccess.OnLocationRequest))
            }
        }
    }


}