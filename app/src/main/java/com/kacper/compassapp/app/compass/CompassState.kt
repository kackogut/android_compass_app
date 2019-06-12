package com.kacper.compassapp.app.compass

class CompassState(
    var step : CompassStateValue
)

sealed class CompassStateValue {
    object OnAzimuthChange : CompassStateValue()
    object OnLocationRequest : CompassStateValue()
}