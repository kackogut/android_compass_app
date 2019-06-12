package com.kacper.compassapp.app.compass

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationRequest
import com.kacper.compassapp.utils.COMPASS_ALPHA
import com.kacper.compassapp.utils.Utils
import com.patloew.rxlocation.RxLocation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class CompassViewModel(
    private val rxLocation: RxLocation
) : ViewModel() {
    var gravityArray = FloatArray(3)
    var geomagneticArray = FloatArray(3)

    var azimut : Float = 0F
    var currentAzimuth: Float = 0F
    var destinationBearing: Float = 0F

    private var compositeDisposable = CompositeDisposable()

    var destinationLat = ObservableField<String>()
    var destinationLon = ObservableField<String>()
    var currentDestination = ObservableField<Location?>()

    val state = PublishSubject.create<CompassState>()

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

            azimut = Utils.getAzimutValue(orientation[0])
            state.onNext(CompassState(CompassStateValue.OnAzimuthChange))
            currentAzimuth = azimut

        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000)

        compositeDisposable.add(
            rxLocation.location().updates(locationRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { })

    }

    fun startNavigation(){
        if(destinationLat.get().isNullOrEmpty() || destinationLon.get().isNullOrEmpty()){
            //state.onError()
        } else {
            currentDestination.set(Location(LocationManager.GPS_PROVIDER))
            currentDestination.get()?.let {
                it.latitude = destinationLat.get()!!.toDouble()
                it.longitude = destinationLon.get()!!.toDouble()
            }
            //TODO make state easier to use
            state.onNext(CompassState(CompassStateValue.OnLocationRequest))
        }
    }


}