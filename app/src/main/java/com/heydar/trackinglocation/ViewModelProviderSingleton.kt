package com.heydar.trackinglocation

object ViewModelProviderSingleton {
    private var locationViewModel: MainViewModel? = null

    fun getLocationViewModel(): MainViewModel {
        if (locationViewModel == null) {
            locationViewModel = MainViewModel()
        }
        return locationViewModel!!
    }
}