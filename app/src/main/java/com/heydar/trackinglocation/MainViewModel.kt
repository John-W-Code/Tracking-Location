package com.heydar.trackinglocation

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _locationData = MutableLiveData<Location>()
    val locationData: LiveData<Location> get() = _locationData

    fun updateLocation(location: Location) {
        _locationData.value = location
    }
}