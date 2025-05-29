package com.heydar.trackinglocation

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.heydar.trackinglocation.location.ForegroundUpdateLocationService
import com.heydar.trackinglocation.location.GPSBroadcastReceiver
import kotlin.math.abs

/// JW location
class MyLocation (var myLong : Double, var myLat : Double){
    fun isEmpty() : Boolean{
        return (myLong == 0.0) && (myLat == 0.0)
    }
    fun insert(location: Location){
        this.myLong = location.longitude
        this.myLat = location.latitude
    }
    fun distance(location: Location) : Double {
        return abs(location.longitude - this.myLong)
    }
}

class MainActivity() : AppCompatActivity() {
    private var gpsBroadcastReceiver: GPSBroadcastReceiver? = null
    private lateinit var locationViewModel: MainViewModel
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

    // JW vars
    var distance = 0.0
    var oldLocation = MyLocation(0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tvLocation = findViewById<TextView>(R.id.tv_location)
        if (!Utils.isLocationAvailable(this)) {
            val gpsIntent = Intent(this, EnableGPSActivity::class.java)
            startActivity(gpsIntent)
        }
        locationViewModel = ViewModelProviderSingleton.getLocationViewModel()
        locationViewModel.locationData.observe(this) { location ->
            tvLocation.text = String.format("%S  -  %S", location.latitude, location.longitude)
            if (oldLocation.isEmpty()) {
                oldLocation.insert(location)
            }
            Log.d("Location", "onCreate. distance: ${oldLocation.distance(location)}")
            oldLocation.insert(location)
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