package com.udacity.project4.locationreminders.permission

interface PermissionHandler {
    val requestCode: Int
    val permissionName: String
    fun hasPermission(): Boolean
    fun requestPermission(requestPermissions: (permissions: Array<String?>, requestCode: Int) -> Unit)
    fun isApproved(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) : Boolean
}