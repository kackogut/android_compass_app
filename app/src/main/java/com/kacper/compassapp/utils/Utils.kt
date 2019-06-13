package com.kacper.compassapp.utils

import android.app.Activity
import android.widget.Toast
import es.dmoral.toasty.Toasty

class Utils {
    companion object {
        fun getAzimuthValue(valueToParse: Float) =
            ((Math.toDegrees(valueToParse.toDouble()) + 360) % 360).toFloat()

    }
}

fun Activity.showToastError(stringResource: Int) {
    Toasty.error(this, getString(stringResource), Toast.LENGTH_SHORT, true).show()
}