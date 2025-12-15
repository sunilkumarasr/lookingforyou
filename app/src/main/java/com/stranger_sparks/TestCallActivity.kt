package com.stranger_sparks

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.stranger_sparks.utils.ConstantUtils
import com.tencent.mmkv.MMKV

class TestCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ConstantUtils.darkModeDisable()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_call)


        //addCallFragment();


        MMKV.initialize(this@TestCallActivity)

        val userIDInput = findViewById<EditText>(R.id.user_id)
        val userNameInput = findViewById<EditText>(R.id.user_name)
        val userLogin = findViewById<Button>(R.id.user_login)


        userIDInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Append manufacturer to the userName field
                userNameInput.setText(s.toString() + "_" + android.os.Build.MANUFACTURER.lowercase())
            }
        })

        // Default manufacturer value for userID
        userIDInput.setText(android.os.Build.MANUFACTURER.lowercase())

        // Handle the login button click
        userLogin.setOnClickListener {
            val userID = userIDInput.text.toString()
            val userName = userNameInput.text.toString()

            signIn(userID, userName)
        }


    }

    private fun signIn(userID: String, userName: String) {
        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
            return
        }

        val progress = findViewById<CircularProgressIndicator>(R.id.progress_circular)
        progress.visibility = View.VISIBLE

        // Fake login process with a delay
        val fakeLoginProcess = Handler(Looper.getMainLooper())
        fakeLoginProcess.postDelayed({
            progress.visibility = View.GONE
            // Save user details to MMKV
            MMKV.defaultMMKV().putString("userID", userID)
            MMKV.defaultMMKV().putString("userName", userName)

            // Navigate to the main activity
            val intent = Intent(this@TestCallActivity, TestCallMainActivity::class.java)
            intent.putExtra("userID", userID)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }, 1000) // Fake delay
    }



}