package com.JW.trackinglocation

import android.annotation.SuppressLint
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
import androidx.core.view.isVisible


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//JW
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

    fun getCellAt(table: TableLayout, rowIndex: Int, columnIndex: Int): View {
        val row = table.getChildAt(rowIndex) as TableRow
        return row.getChildAt(columnIndex)
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
        totalTime                   = getCellAt(infoTable, 1, 1) as TextView
        totalDistance               = getCellAt(infoTable, 1, 2) as TextView
        totalSpeed                  = getCellAt(infoTable, 1, 3) as TextView


        // hide / show some fields
        startReceiveLocationsBTN.isVisible = false
        markTV.isVisible     = false
        currentTV.isVisible  = false
        distanceTV.isVisible = true
        infoTable.isVisible  = true
        countTV.setBackgroundResource(R.drawable.circle_grey)
        startCountingBTN.isEnabled = false

        // set the button events
        markStartLocationBTN.setOnClickListener {
            counting.setStartLocation()
            //Toast.makeText(activity, "Button clicked!", Toast.LENGTH_SHORT).show()
            Log.d("JW", "markStartLocationBTN pressed")
        }

        startCountingBTN.setOnClickListener {
            if (counting.toggleCounting()) { // we are counting (again)
                startCountingBTN.text = getString(R.string.pause_counting_rounds)
            } else {
                startCountingBTN.text = getString(R.string.start_counting_rounds)
            }
        }

        zeroCountBTN.setOnClickListener {
            counting.resetCount()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("JW", "rounds.onViewCreated")
        distanceTV.text = "- m"
        countTV.text = "---"


        @SuppressLint("DefaultLocale")
        fun displayAll() {
            currentTV.text = counting.currentLocation.toText()
            distanceTV.text = String.format("%S m", counting.distance.toString())
            markTV.text = counting.startLocation.toText()
            countTV.text = counting.numberOfRounds.toString()
            if (counting.running)  {countTV.setBackgroundResource(R.drawable.circle_red)}
            else                   {countTV.setBackgroundResource(R.drawable.circle_grey)}
            if (counting.startSet) {
                startCountingBTN.isEnabled = true
                markStartLocationBTN.text = getString(R.string.mark_start_location).plus(" /")
            }
            else {
                startCountingBTN.isEnabled = false
            }
            // display overview

            totalTime.text     = String.format("%d:%d", counting.duration.toMinutesPart(), counting.duration.toSecondsPart())
            totalSpeed.text    = String.format("%.2f", counting.speedTotal)
            totalDistance.text = String.format("%.3f", counting.distanceTotal.toLong() / 1000.0)

            Log.d("Location", "rounds.onCreate. distance: ${counting.distance}")
            Log.d("Location", "rounds.onCreate. #laps: ${counting.numberOfRounds}")
            //Log.d("Location", "rounds.onCreate. current: ${counting.currentLocation.toText()}")
            //Log.d("Location", "rounds.onCreate. start  : ${counting.startLocation.toText()}")
            //Log.d("Location", "rounds.onCreate. old    : ${counting.lastLocation.toText()}")
        }

        locationViewModel = ViewModelProviderSingleton.getLocationViewModel()
        locationViewModel.locationData.observe(viewLifecycleOwner) { location ->
            Log.d("TEST", this.toString())
            counting.setLocation(location)
            displayAll()

        }
    }

    override fun onPause() {
        super.onPause()
        //locationViewModel.locationData.removeObservers(viewLifecycleOwner)
        Log.d("onPause", viewLifecycleOwner.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationViewModel.locationData.removeObservers(viewLifecycleOwner)
        Log.d("onDestroy", viewLifecycleOwner.toString())
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