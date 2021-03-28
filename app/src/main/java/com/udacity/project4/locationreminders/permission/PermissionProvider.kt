package com.udacity.project4.locationreminders.permission

import android.content.Context

class PermissionProvider(context: Context) {

    var backgroundLocation: PermissionHandler =
        BackgroundLocationPermissionHandler(context)
    var fineLocation: PermissionHandler =
        FineLocationPermissionHandler(context)

    fun hasPermissions(): Boolean {
        return backgroundLocation.hasPermission() && fineLocation.hasPermission()
    }
}
