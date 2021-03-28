package com.udacity.project4.locationreminders.permission

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

@TargetApi(29)
class BackgroundLocationPermissionHandler(private val context: Context) : PermissionHandler {

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val requestCodeQOrLater = 29

    override val requestCode: Int
        get() = 21

    override val permissionName: String
        get() = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    override fun hasPermission(): Boolean {
        return if (runningQOrLater) {
            ActivityCompat.checkSelfPermission(
                context, permissionName
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun requestPermission(requestPermissions: (permissions: Array<String?>, requestCode: Int) -> Unit) {
        if (hasPermission().not()) {
            if (runningQOrLater) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), requestCodeQOrLater
                )
            } else {
                requestPermissions(
                    arrayOf(permissionName),
                    requestCode
                )
            }
        }
    }

    override fun isApproved(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        var isApproved = false

        if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            isApproved = true
        }
        return isApproved
    }
}
