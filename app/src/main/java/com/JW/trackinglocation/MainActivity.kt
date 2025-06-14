package com.JW.trackinglocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.JW.trackinglocation.location.ForegroundUpdateLocationService
import com.JW.trackinglocation.location.GPSBroadcastReceiver


class MainActivity() : AppCompatActivity() {
    private var gpsBroadcastReceiver: GPSBroadcastReceiver? = null
    // (moved to rounds.kt Fragment) private lateinit var locationViewModel: MainViewModel
    private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startLocationService()
        } else {
            openAppSettings()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val requestNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            openNotificationSettings()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val roundsFragment = Rounds()

        //JW
        Log.d("JW", "MainActivity.onCreate")
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mainframe, roundsFragment)
            commit()
        }

        if (!Utils.isLocationAvailable(this)) {
            val gpsIntent = Intent(this, EnableGPSActivity::class.java)
            startActivity(gpsIntent)
        }

        gpsBroadcastReceiver = GPSBroadcastReceiver()
        registerGpsReceiver()
        requestPermissions()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startLocationService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun startLocationService() {
        val startServiceIntent = Intent(this, ForegroundUpdateLocationService::class.java)
        startService(startServiceIntent)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    private fun registerGpsReceiver() {
        val filter = IntentFilter()
        filter.addAction("android.location.PROVIDERS_CHANGED")
        registerReceiver(gpsBroadcastReceiver, filter)
    }
}