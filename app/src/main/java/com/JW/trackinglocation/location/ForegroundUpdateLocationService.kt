package com.JW.trackinglocation.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.JW.trackinglocation.R

class ForegroundUpdateLocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPendingIntent: PendingIntent
    private var gpsBroadcastReceiver: GPSBroadcastReceiver? = null
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val filter = IntentFilter()
        filter.addAction("android.location.PROVIDERS_CHANGED")
        gpsBroadcastReceiver = GPSBroadcastReceiver()
        registerReceiver(gpsBroadcastReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 50)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(20)
            .setMaxUpdateDelayMillis(50)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return START_NOT_STICKY
        }

        val serviceIntent = Intent(this, LocationBroadcastReceiver::class.java)
        locationPendingIntent = PendingIntent.getBroadcast(this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        fusedLocationClient.requestLocationUpdates(locationRequest, locationPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_ID, "Channel title", NotificationManager.IMPORTANCE_HIGH)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, NOTIFICATION_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Location Tracking")
                .setContentText("Location service is active")
                .setAutoCancel(true)
                .build()
            startForeground(1, notification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpsBroadcastReceiver)
    }

    companion object {
        private const val NOTIFICATION_ID = "location_channel"
    }
}