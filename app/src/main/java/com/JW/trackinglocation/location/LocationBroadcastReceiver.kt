package com.JW.trackinglocation.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationResult
import com.JW.trackinglocation.ViewModelProviderSingleton
import com.JW.trackinglocation.counting

class LocationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (LocationResult.hasResult(intent)) {
            val result: LocationResult? = LocationResult.extractResult(intent)
            if (result != null) {
                for (location in result.locations) {
                    updateLocation(location)
                }
            } else {
                Log.d("TAG", "onReceive: null")
            }
        }
    }

    private fun updateLocation(location: Location) {
        Log.d("TAG", "onLocationChanged: " + location.latitude + "  " + location.longitude)
        val viewModel = ViewModelProviderSingleton.getLocationViewModel()
        viewModel.updateLocation(location)
        counting.setLocation(location)
    }
}