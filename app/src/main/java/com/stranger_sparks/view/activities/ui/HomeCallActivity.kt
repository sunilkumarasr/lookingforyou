package com.stranger_sparks.view.activities.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.stranger_sparks.R
import com.stranger_sparks.fcm.CallService
import com.stranger_sparks.utils.SharedPreferenceManager

class HomeCallActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Make the activity show over lock screen and turn screen on
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_home_call)

        val callerName = intent.getStringExtra("caller_name") ?: "Unknown"
        val callerEmail = intent.getStringExtra("caller_email") ?: ""

        val tvCaller = findViewById<TextView>(R.id.tvCaller)
        val btnAccept = findViewById<Button>(R.id.btnAccept)
        val btnReject = findViewById<Button>(R.id.btnReject)

        tvCaller.text = "Incoming call from $callerName"

        btnAccept.setOnClickListener {
            // Handle accepting call (start ZegoCallManager or call screen)
            finish()
        }

        btnReject.setOnClickListener {
            // Handle rejecting call
            finish()
        }
    }
}