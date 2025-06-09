package com.JW.trackinglocation

import android.content.Context
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.sql.Time
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt


/// JW location
class MyLocation (lat : Double, long : Double){
    var myLong = long
    var myLat = lat

    fun setLocation(location: Location?){
        if (location != null) {
            myLong = location.longitude
            myLat = location.latitude
        } else {
            myLong = 0.0
            myLat = 0.0
        }
    }
    fun toText() : String {
        return String.format("%S  -  %S", myLat, myLong)
    }
    fun copyLocation(from : MyLocation) {
        this.myLong = from.myLong
        this.myLat  = from.myLat
    }
}

class MyCounting() {
    val startLocation = MyLocation(0.0, 0.0)
    val lastLocation  = MyLocation(0.0, 0.0)
    val currentLocation = MyLocation(0.0, 0.0)
    var numberOfRounds: Int = 0
    var running:  Boolean = false // are we counting rounds
    var outside:  Boolean = false // are we outside maxDelta
    var startSet: Boolean = false // Is the startLocation set
    var distance = 0
    var distanceTotal = 0
    var speedTotal = 0.0
    var recordings = ArrayList<Int>() // the list with all recorded numbers
    var maxRecordings: Int = 0
    var startTime = LocalDateTime.now()
    var currentTime = LocalDateTime.now()
    var duration: Duration = Duration.ZERO

    // move currentLocation to lastLocation
    // if running then check if next round is reached
    fun setLocation(location: Location?){
        if (location != null) {
            lastLocation.copyLocation(currentLocation)
            currentLocation.setLocation(location)
            distance = differenceMeter(startLocation, currentLocation)
            if (running) {
                if (outside && (distance < minDelta)){
                    numberOfRounds += 1
                    outside = false
                    playRound(numberOfRounds)
                }
                outside = outside or (distance > maxDelta)
                duration = Duration.between(startTime, LocalDateTime.now())
                distanceTotal += differenceMeter(currentLocation, lastLocation)
                speedTotal = (distanceTotal / duration.toSeconds().toDouble()) * 3.6

            }
        }
    }

    fun setStartLocation() {
        startLocation.copyLocation(currentLocation)
        outside = false // we start on the start location
        startSet = true // the startLocation is set
    }

    fun toggleCounting() : Boolean {
        running = !running
        if (running) startTime = LocalDateTime.now()
        return running
    }

    fun resetCount (){
        numberOfRounds = 0
        outside = false
        duration = Duration.ZERO
        startTime = LocalDateTime.now()
        distanceTotal = 0
        speedTotal = 0.0
    }
    // Sound stuff
    fun initSounds(){
        recordings.clear()
        recordings.add(R.raw.du_round)
        recordings.add(R.raw.du_001)
        recordings.add(R.raw.du_002)
        recordings.add(R.raw.du_003)
        recordings.add(R.raw.du_004)
        recordings.add(R.raw.du_005)
        recordings.add(R.raw.du_006)
        recordings.add(R.raw.du_007)
        recordings.add(R.raw.du_008)
        recordings.add(R.raw.du_009)
        recordings.add(R.raw.du_010)
        maxRecordings = 10
    }
    var myContext: Context? = null
    fun setContext(context: Context?){
        myContext = context
    }

    fun playRound(round : Int){
        var mediaPlayer = MediaPlayer.create(myContext, R.raw.du_round)
        mediaPlayer.setVolume(DEFAULT_VOLUME, DEFAULT_VOLUME)

        fun mediaEnded(){
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
            Log.d(TAG, "media klaar")
        }
        fun callRound(number: Int) {
            val numb = number - maxRecordings * number.div(maxRecordings)
            mediaPlayer.reset()
            if (numb <= maxRecordings) {
                mediaPlayer = MediaPlayer.create(myContext, recordings[numb])
            } else {
                mediaPlayer = MediaPlayer.create(myContext, R.raw.du_last_)
            }
            mediaPlayer.setVolume(DEFAULT_VOLUME, DEFAULT_VOLUME)
            mediaPlayer?.start()
            mediaPlayer.setOnCompletionListener { mediaEnded() }
        }

        mediaPlayer?.start()
        mediaPlayer.setOnCompletionListener { callRound(round) }
}

    companion object {
        private const val DEFAULT_VOLUME = 1.0F
        private const val TAG = "ForegroundOnlyLocationService"
        /*
        private const val PACKAGE_NAME = "com.example.android.whileinuselocation"
        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
        internal const val EXTRA_MARK = "$PACKAGE_NAME.extra.MARK"
        internal const val EXTRA_DISTANCE = "$PACKAGE_NAME.extra.DISTANCE"
        internal const val EXTRA_COUNT = "$PACKAGE_NAME.extra.COUNT"
        internal const val EXTRA_STORED = "$PACKAGE_NAME.extra.STORED"
        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
        private const val NOTIFICATION_ID = 12345678
        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
        */
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
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadiusM * c).toInt()
    }
}

// init all sound files
