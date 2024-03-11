package com.ruuvi.commissioning

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class PermissionsInteractor (val context: Context) {

    fun permissionsNeeded() = getNeededPermissions().isNotEmpty()

    fun getNeededPermissions(): List<String> {
        val permissionNeeded = if (isApi31Behaviour)
            Manifest.permission.BLUETOOTH_SCAN
        else Manifest.permission.ACCESS_COARSE_LOCATION

        val checkPermission = ContextCompat.checkSelfPermission(context, permissionNeeded)

        val listPermissionsNeeded = ArrayList<String>()

        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(permissionNeeded)
        }

        return listPermissionsNeeded
    }

    fun showPermissionDialog(activity: AppCompatActivity): Boolean {
        val listPermissionsNeeded = getNeededPermissions()

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toTypedArray(), 10)
        }

        return !listPermissionsNeeded.isEmpty()
    }

    fun showPermissionSnackbar(activity: AppCompatActivity) {
        val snackbar = Snackbar.make(activity.window.decorView.rootView, getPermissionsMissedMessage(), Snackbar.LENGTH_LONG)
        snackbar.setAction("Settings") {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
        snackbar.show()
    }
    private val isApi31Behaviour: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun getPermissionsMissedMessage(): String {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            "Please enable location permission in Settings to see sensors"
        } else {
            "Please allow to show nearby devices in Settings to see sensors."
        }
    }

    fun checkRequestPermissionRationale(activity: AppCompatActivity): Boolean {
        return if (isApi31Behaviour) ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_SCAN)
        else ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)

    }
}