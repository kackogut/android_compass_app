package com.kacper.compassapp.utils

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kacper.compassapp.R

object LocationPermission {

    private const val REQUEST_PERMISSION_FINE_LOCATION = 999

    private val REQUIRED_PERMISSIONS = arrayOf(permission.ACCESS_FINE_LOCATION)

    fun isLocationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true else ContextCompat.checkSelfPermission(
            context,
            permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission.ACCESS_FINE_LOCATION
            )
        ) {
            showRationale(activity)
        } else {
            showRequestPermission(activity)
        }
    }

    private fun showRequestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            REQUEST_PERMISSION_FINE_LOCATION
        )
    }

    private fun showRationale(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
            .setMessage(R.string.location_permission_rationale_description)
            .setPositiveButton(R.string.ok) { _, _ -> showRequestPermission(activity) }

        val alert = builder.create()
        alert.show()
    }
}

fun Activity.checkLocationPermission(shouldRequestLocation: Boolean) =
    if (!LocationPermission.isLocationPermissionGranted(this)) {
        if (shouldRequestLocation) {
            LocationPermission.requestLocationPermission(this)
        }
        false
    } else {
        true
    }
