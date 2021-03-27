package com.udacity.project4.locationreminders.permission

import android.content.Context

class PermissionProvider(ctx: Context) {

    var backgroundLocation: PermissionHandler =
        BackgroundLocationPermissionHandler(ctx)
    var fineLocation: PermissionHandler =
        FineLocationPermissionHandler(ctx)

    fun hasPermissions(): Boolean {
        return backgroundLocation.hasPermission() && fineLocation.hasPermission()
    }
}
