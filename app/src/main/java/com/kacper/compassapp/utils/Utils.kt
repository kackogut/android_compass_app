package com.kacper.compassapp.utils

class Utils {
    companion object {
        fun getAzimutValue(valueToParse: Float) =
            ((Math.toDegrees(valueToParse.toDouble()) + 360) % 360).toFloat()


    }
}