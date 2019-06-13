package com.kacper.compassapp.app.compass


class CompassState(
    var step: CompassStateSuccess? = null,

    var error: Int? = null
)

sealed class CompassStateSuccess {
    object OnAzimuthChange : CompassStateSuccess()
    object OnLocationRequest : CompassStateSuccess()
}