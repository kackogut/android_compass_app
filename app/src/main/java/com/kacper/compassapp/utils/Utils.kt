package com.kacper.compassapp.utils

import android.app.Activity
import android.hardware.SensorEvent
import android.widget.Toast
import es.dmoral.toasty.Toasty

class Utils {
    companion object {
        fun getAzimuthValue(valueToParse: Float) =
            ((Math.toDegrees(valueToParse.toDouble()) + 360) % 360).toFloat()

        fun convertToSensorArray(array: FloatArray, sensorEvent: SensorEvent) =
            sensorEvent.values.slice(0..2).mapIndexed { index, value ->
                value * (1 - COMPASS_ALPHA) + COMPASS_ALPHA * array[index]
            }.toFloatArray()


    }
}

fun Activity.showToastError(stringResource: Int) {
    Toasty.error(this, getString(stringResource), Toast.LENGTH_SHORT, true).show()
}

fun Activity.showToastSuccess(stringResource: Int) {
    Toasty.success(this, getString(stringResource), Toast.LENGTH_SHORT, true).show()
}