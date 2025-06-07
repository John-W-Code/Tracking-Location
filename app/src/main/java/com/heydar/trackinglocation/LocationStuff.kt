package com.heydar.trackinglocation

import android.location.Location
import kotlin.math.sin

/// JW location
class MyLocation (var myLong : Double, var myLat : Double){
    fun isEmpty() : Boolean{
        return (myLong == 0.0) && (myLat == 0.0)
    }
    fun setLocation(location: Location?){
        if (location != null) {
            this.myLong = location.longitude
            this.myLat = location.latitude
        } else {
            this.myLong = 0.0
            this.myLat = 0.0
        }
    }
}

class MyCounting() {
    var startLocation = MyLocation(4.9150267, 52.3733383)
    var lastLocation  = MyLocation(0.0, 0.0)
    var currentLocation = MyLocation(0.0, 0.0)
    var numberOfRounds: Int = 0
    var running: Boolean = true // are we counting rounds
    var outside: Boolean = false  // were we outside maxdelta
    var distance = 0


    // move currentLocation to lastLocation
    // if running then check if next round is reached
    fun updateLocation(location: Location?){
        if (location != null) {
            lastLocation = currentLocation
            currentLocation.myLong = location.longitude
            currentLocation.myLat = location.latitude
            distance = differenceMeter(startLocation, currentLocation)
            if (running) {
                if (outside && distance < minDelta){
                    numberOfRounds += 1
                    outside = false
                }
                outside = outside or (distance > maxDelta)
            }
        }
    }
    fun setStartLocation() {
        startLocation = currentLocation
    }

}

// calculate distances
//
const val maxDelta = 30 // above this distance (meters) start next round
const val minDelta = 20 // below this distance (meters) count round

fun degreesToRadians(degrees : Double) : Double{
    return degrees * Math.PI / 180.0
}

fun differenceMeter(old: MyLocation?, new: MyLocation?) : Int{
    if ((old == null) || (new == null)) {
        return -1
    }
    else {
        val earthRadiusM = 6371000.0
        val dLat = degreesToRadians(old.myLat - new.myLat)
        val dLon = degreesToRadians(old.myLong - new.myLong)
        val lat1 = degreesToRadians(old.myLat)
        val lat2 = degreesToRadians(new.myLat)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * kotlin.math.cos(lat1) * kotlin.math.cos(lat2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadiusM * c).toInt()
    }
}

fun nextRound(old: MyLocation?, new: MyLocation?, check: Boolean): Triple<Boolean, Boolean, Int>{// return new round, outside marker, distance
    val distance = differenceMeter(old, new)
    if (check){ // been outside from marker
        return Triple(distance < minDelta, distance >= minDelta, distance)
    }
    else { // not yet outside from marker
        return Triple(false, distance > maxDelta, distance)
    }
}