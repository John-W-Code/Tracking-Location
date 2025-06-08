package com.heydar.trackinglocation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//JW
private lateinit var startReceiveLocationsBTN: Button
private lateinit var markStartLocationBTN: Button
private lateinit var startCountingBTN: Button
private lateinit var zeroCountBTN: Button
private lateinit var markTV: TextView
private lateinit var currentTV: TextView
private lateinit var distanceTV: TextView
private lateinit var countTV: TextView

/**
 * A simple [Fragment] subclass.
 * Use the [rounds.newInstance] factory method to
 * create an instance of this fragment.
 */
class rounds : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var locationViewModel: MainViewModel

    // JW vars
    var oldLocation = MyLocation(0.0, 0.0)
    val counting = MyCounting()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //location
        locationViewModel = ViewModelProviderSingleton.getLocationViewModel()
        locationViewModel.locationData.observe(this) { location ->
            counting.updateLocation(location)
            currentTV.text = counting.currentLocation.toText()
            distanceTV.text = counting.distance.toString()
            markTV.text = counting.startLocation.toText()
            Log.d("Location", "rounds.onCreate. distance: ${counting.distance}")
            Log.d("Location", "rounds.onCreate. current: ${counting.currentLocation.toText()}")
            Log.d("Location", "rounds.onCreate. start  : ${counting.startLocation.toText()}")
            Log.d("Location", "rounds.onCreate. old    : ${counting.lastLocation.toText()}")
        }

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rounds, container, false)
        startReceiveLocationsBTN    = view.findViewById(R.id.start_receive_locations_btn)
        markStartLocationBTN        = view.findViewById(R.id.mark_start_location_btn)
        startCountingBTN            = view.findViewById(R.id.start_counting_rounds_btn)
        zeroCountBTN                = view.findViewById(R.id.zero_rounds_btn)
        markTV                      = view.findViewById(R.id.marking_point_text_view)
        currentTV                   = view.findViewById(R.id.current_position_text_view)
        distanceTV                  = view.findViewById(R.id.distance_text_view)
        countTV                     = view.findViewById(R.id.round_count_text_view)

        // hide some fields
        startReceiveLocationsBTN.isVisible = true
        markTV.isVisible     = true
        currentTV.isVisible  = true
        distanceTV.isVisible = true
        countTV.setBackgroundResource(R.drawable.circle_grey)

        // set the button events
        markStartLocationBTN.setOnClickListener {
            counting.setStartLocation()
            Toast.makeText(activity, "Button clicked!", Toast.LENGTH_SHORT).show()
            Log.d("JW", "markStartLocationBTN pressed")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("JW", "rounds.onViewCreated")
        distanceTV.text = "ONE"
        countTV.text = "---"
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
        fun newInstance(param1: String, param2: String) =
            rounds().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}