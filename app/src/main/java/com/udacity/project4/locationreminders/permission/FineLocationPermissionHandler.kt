package com.udacity.project4.locationreminders.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class FineLocationPermissionHandler(private val context: Context) : PermissionHandler {
    override val requestCode: Int
        get() = 42
    override val permissionName: String
        get() = Manifest.permission.ACCESS_FINE_LOCATION

    override fun hasPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permissionName
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(requestPermissions: (permissions: Array<String?>, requestCode: Int) -> Unit) {
        if (hasPermission().not()) {
            requestPermissions(
                arrayOf(permissionName),
                requestCode
            )
        }
    }

    override fun isApproved(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        var isApproved = false
        if (requestCode == this.requestCode) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                isApproved = true
            }
        }
        return isApproved
    }
}
