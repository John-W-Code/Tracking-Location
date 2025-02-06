package com.heydar.trackinglocation

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton


class EnableGPSActivity : AppCompatActivity() {
    private var enableGPS: MaterialButton? = null
    private lateinit var viewModel: GpsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_gps_activity)
        viewModel = ViewModelProvider(this)[GpsViewModel::class.java]
        enableGPS = findViewById(R.id.btn_enable_gps)
        viewModel.isGpsEnabled.observe(this) { isEnabled ->
            if (isEnabled) {
                finish()
            }
        }
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.setFinishOnTouchOutside(false)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        enableGPS!!.setOnClickListener {
            if (!Utils.isLocationAvailable(this)) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Utils.isLocationAvailable(this)) {
            finish()
        }
    }
}