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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var totalTime: TextView
    private lateinit var totalDistance: TextView
    private lateinit var totalSpeed: TextView
    private lateinit var buildVersionTV: TextView
    private lateinit var roundsRecyclerView: RecyclerView
    private lateinit var roundAdapter: RoundAdapter
    private lateinit var sortRoundBTN: Button
    private lateinit var sortTimeBTN: Button
    private lateinit var sortDistanceBTN: Button
    private lateinit var sortSpeedBTN: Button
    private var sortColumn: String = "number"
    private var sortAscending: Boolean = true

    private fun setSort(column: String) {
        if (sortColumn == column) {
            sortAscending = !sortAscending
        } else {
            sortColumn = column
            sortAscending = true
        }
        displayAll()
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

        // Update rounds list with sorting
        val sortedList = when (sortColumn) {
            "number" -> if (sortAscending) counting.roundsList.sortedBy { it.number } else counting.roundsList.sortedByDescending { it.number }
            "time" -> if (sortAscending) counting.roundsList.sortedBy { it.time } else counting.roundsList.sortedByDescending { it.time }
            "distance" -> if (sortAscending) counting.roundsList.sortedBy { it.distance } else counting.roundsList.sortedByDescending { it.distance }
            "speed" -> if (sortAscending) counting.roundsList.sortedBy { it.speed } else counting.roundsList.sortedByDescending { it.speed }
            else -> counting.roundsList
        }
        roundAdapter.updateData(sortedList)

        // Update sort indicators
        updateSortIndicator(sortRoundBTN, "number")
        updateSortIndicator(sortTimeBTN, "time")
        updateSortIndicator(sortDistanceBTN, "distance")
        updateSortIndicator(sortSpeedBTN, "speed")

        Log.d("JW: Location", "rounds.onCreate. distance: ${counting.distance}")
        Log.d("JW: Location", "rounds.onCreate. #laps: ${counting.numberOfRounds}")
    }

    private fun updateSortIndicator(button: Button, column: String) {
        val drawableRes = if (sortColumn == column) {
            if (sortAscending) R.drawable.ic_sort_down else R.drawable.ic_sort_up
        } else {
            R.drawable.ic_sort_both
        }
        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
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
        totalTime                   = view.findViewById(R.id.total_time_tv)
        totalDistance               = view.findViewById(R.id.total_distance_tv)
        totalSpeed                  = view.findViewById(R.id.total_speed_tv)
        buildVersionTV              = view.findViewById(R.id.build_version_tv)
        roundsRecyclerView           = view.findViewById(R.id.rounds_recycler_view)
        sortRoundBTN                = view.findViewById(R.id.sort_round)
        sortTimeBTN                 = view.findViewById(R.id.sort_time)
        sortDistanceBTN             = view.findViewById(R.id.sort_distance)
        sortSpeedBTN                = view.findViewById(R.id.sort_speed)

        // setup recycler view
        roundAdapter = RoundAdapter(emptyList())
        roundsRecyclerView.layoutManager = LinearLayoutManager(context)
        roundsRecyclerView.adapter = roundAdapter
        LinearSnapHelper().attachToRecyclerView(roundsRecyclerView)

        // setup sorting
        sortRoundBTN.setOnClickListener { setSort("number") }
        sortTimeBTN.setOnClickListener { setSort("time") }
        sortDistanceBTN.setOnClickListener { setSort("distance") }
        sortSpeedBTN.setOnClickListener { setSort("speed") }

        // build version
        buildVersionTV.text = getString(R.string.build_format, BuildConfig.BUILD_NUMBER)

        // hide / show some fields
        startReceiveLocationsBTN.isVisible = false
        markTV.isVisible     = false
        currentTV.isVisible  = false
        distanceTV.isVisible = true
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
            displayAll()
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