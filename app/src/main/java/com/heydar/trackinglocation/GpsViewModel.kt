package com.heydar.trackinglocation

import android.app.Application
import android.content.Context
import android.database.ContentObserver
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class GpsViewModel(application: Application) : AndroidViewModel(application) {
    private val _isGpsEnabled = MutableLiveData<Boolean>()
    val isGpsEnabled: LiveData<Boolean> = _isGpsEnabled

    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val gpsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            _isGpsEnabled.postValue(isGpsOn())
        }
    }

    init {
        val resolver = application.contentResolver
        resolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED), false, gpsObserver)
        _isGpsEnabled.value = isGpsOn()
    }

    private fun isGpsOn(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(gpsObserver)
    }
}