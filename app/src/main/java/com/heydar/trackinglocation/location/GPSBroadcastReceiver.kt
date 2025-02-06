package com.heydar.trackinglocation.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.heydar.trackinglocation.EnableGPSActivity


class GPSBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val gpsIntent = Intent(context, EnableGPSActivity::class.java)
        gpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (!isGpsEnabled(context)) {
            context.startActivity(gpsIntent)
        }
    }

    private fun isGpsEnabled(context: Context): Boolean {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return false
        }
    }
}