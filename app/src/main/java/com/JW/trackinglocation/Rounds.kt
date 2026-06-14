package com.JW.trackinglocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.JW.trackinglocation.location.ForegroundUpdateLocationService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.time.Duration
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//JW
@SuppressLint("StaticFieldLeak")
val counting = MyCounting()

/**
 * A simple [Fragment] subclass.
 * Use the [Rounds.newInstance] factory method to
 * create an instance of this fragment.
 */
class Rounds : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: Int = 0
    private var param2: String? = null
    //JW
    private lateinit var locationViewModel: MainViewModel
    private lateinit var startReceiveLocationsBTN: Button
    private lateinit var markStartLocationBTN: Button
    private lateinit var startCountingBTN: Button
    private lateinit var zeroCountBTN: Button
    private lateinit var markTV: TextView
    private lateinit var currentTV: TextView
    private lateinit var distanceTV: TextView
    private lateinit var countTV: TextView
    private lateinit var infoTable: TableLayout
    private lateinit var totalTime: TextView
    private lateinit var totalDistance: TextView
    private lateinit var totalSpeed: TextView
    private lateinit var lapTime: TextView
    private lateinit var lapDistance: TextView
    private lateinit var lapSpeed: TextView
    private lateinit var lapRow: TableRow
    private lateinit var bestLapTime: TextView
    private lateinit var bestLapDistance: TextView
    private lateinit var bestLapSpeed: TextView
    private lateinit var bestLapRow: TableRow

    fun getCellAt(table: TableLayout, rowIndex: Int, columnIndex: Int): View {
        val row = table.getChildAt(rowIndex) as TableRow
        return row.getChildAt(columnIndex)
    }

    @SuppressLint("DefaultLocale")
    private fun displayAll() {
        currentTV.text = counting.currentLocation.toText()
        distanceTV.text = if (counting.startSet) String.format(Locale("nl", "NL"), "%,d m", counting.distance) else "- m"
        markTV.text = counting.startLocation.toText()
        countTV.text = counting.numberOfRounds.toString()
        if (counting.running)  {countTV.setBackgroundResource(R.drawable.circle_green)}
        else                   {countTV.setBackgroundResource(R.drawable.circle_grey)}
        if (counting.startSet) {
            startCountingBTN.isEnabled = true
            markStartLocationBTN.text = getString(R.string.mark_start_location).plus(" \u2714")
        }
        else {
            startCountingBTN.isEnabled = false
        }
        // display overview

        totalTime.text     = String.format("%02d:%02d", counting.duration.toMinutesPart(), counting.duration.toSecondsPart())
        totalDistance.text = String.format(Locale("nl", "NL"), "%,.3f km",   counting.distanceTotal / 1000.0)
        totalSpeed.text    = String.format(Locale("nl", "NL"), "%,.2f km/h", counting.speedTotal)

        if (counting.numberOfRounds > 0) {
            lapRow.isVisible = true
            lapTime.text     = String.format("%02d:%02d", counting.lapTime.toMinutesPart(), counting.lapTime.toSecondsPart())
            lapDistance.text = String.format(Locale("nl", "NL"), "%,.3f km",   counting.lapDistance / 1000.0)
            lapSpeed.text    = String.format(Locale("nl", "NL"), "%,.2f km/h", counting.lapSpeed)
        }
        else lapRow.isVisible = false

        if (counting.bestLapTime != Duration.ZERO) {
            bestLapRow.isVisible = true
            bestLapTime.text     = String.format("%02d:%02d", counting.bestLapTime.toMinutesPart(), counting.bestLapTime.toSecondsPart())
            bestLapDistance.text = String.format(Locale("nl", "NL"), "%,.3f km",   counting.bestLapDistance / 1000.0)
            val bSpeed = if (counting.bestLapTime.toSeconds() > 0) (counting.bestLapDistance / counting.bestLapTime.toSeconds()) * 3.6 else 0.0
            bestLapSpeed.text    = String.format(Locale("nl", "NL"), "%,.2f km/h", bSpeed)
        }
        else bestLapRow.isVisible = false

        Log.d("JW: Location", "rounds.onCreate. distance: ${counting.distance}")
        Log.d("JW: Location", "rounds.onCreate. #laps: ${counting.numberOfRounds}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rounds, container, false)
        counting.setContext(this.context)
        counting.initSounds()

        //location
        startReceiveLocationsBTN    = view.findViewById(R.id.start_receive_locations_btn)
        markStartLocationBTN        = view.findViewById(R.id.mark_start_location_btn)
        startCountingBTN            = view.findViewById(R.id.start_counting_rounds_btn)
        zeroCountBTN                = view.findViewById(R.id.zero_rounds_btn)
        markTV                      = view.findViewById(R.id.marking_point_text_view)
        currentTV                   = view.findViewById(R.id.current_position_text_view)
        distanceTV                  = view.findViewById(R.id.distance_text_view)
        countTV                     = view.findViewById(R.id.round_count_text_view)
        infoTable                   = view.findViewById(R.id.tableLayout)
        lapRow                      = view.findViewById(R.id.lap_row)
        totalTime                   = getCellAt(infoTable, 1, 1) as TextView
        totalDistance               = getCellAt(infoTable, 1, 2) as TextView
        totalSpeed                  = getCellAt(infoTable, 1, 3) as TextView
        lapTime                     = getCellAt(infoTable, 2, 1) as TextView
        lapDistance                 = getCellAt(infoTable, 2, 2) as TextView
        lapSpeed                    = getCellAt(infoTable, 2, 3) as TextView
        bestLapRow                  = view.findViewById(R.id.best_lap_row)
        bestLapTime                 = view.findViewById(R.id.best_lap_time)
        bestLapDistance             = view.findViewById(R.id.best_lap_distance)
        bestLapSpeed                = view.findViewById(R.id.best_lap_speed)

        // hide / show some fields
        startReceiveLocationsBTN.isVisible = false
        markTV.isVisible     = false
        currentTV.isVisible  = false
        distanceTV.isVisible = true
        infoTable.isVisible  = true
        lapRow.isVisible     = false
        bestLapRow.isVisible = false
        countTV.setBackgroundResource(R.drawable.circle_grey)
        startCountingBTN.isEnabled = false

        // set the button events
        markStartLocationBTN.setOnClickListener {
            markStartLocationBTN.text = getString(R.string.mark_start_location).plus(" ~")
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.getFusedLocationProviderClient(requireContext())
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        location?.let {
                            counting.setLocation(it)
                            counting.setStartLocation()
                            locationViewModel.updateLocation(it)
                            markStartLocationBTN.text = getString(R.string.mark_start_location).plus(" \u2714")
                        }
                    }
            }
            Log.d("JW", "markStartLocationBTN pressed")
        }

        startCountingBTN.setOnClickListener {
            val serviceIntent = Intent(context, ForegroundUpdateLocationService::class.java)
            if (counting.toggleCounting()) { // we are counting (again)
                startCountingBTN.text = getString(R.string.pause_counting_rounds)
                Log.d("JW", "startCountingBTN pressed --> ON")
                context?.startService(serviceIntent)
            } else {
                startCountingBTN.text = getString(R.string.start_counting_rounds)
                Log.d("JW", "startCountingBTN pressed --> OFF")
                context?.stopService(serviceIntent)
            }
        }

        zeroCountBTN.setOnClickListener {
            counting.resetCount()
            displayAll()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationViewModel = ViewModelProviderSingleton.getLocationViewModel()
        Log.d("JW", "rounds.onViewCreated")
        distanceTV.text = "- m"
        countTV.text = "---"

        locationViewModel.locationData.observe(viewLifecycleOwner) { location ->
            /* not needed, will be triggered in LocationBroadcastReceiver
            Log.d("JW: TEST", this.toString())
            counting.setLocation(location)
            */
            displayAll()
        }
    }

    override fun onPause() {
        super.onPause()
        //locationViewModel.locationData.removeObservers(viewLifecycleOwner)
        Log.d("JW: onPause", viewLifecycleOwner.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationViewModel.locationData.removeObservers(viewLifecycleOwner)
        Log.d("JW: onDestroy", viewLifecycleOwner.toString())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment rounds.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int, param2: String) =
            Rounds().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}