package com.stranger_sparks.view.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.stranger_sparks.R
import com.stranger_sparks.databinding.ActivitySplashScreenBinding
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.viewmodel.SplashViewModel

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    lateinit var observerEvents: Constants.ObserverEvents
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val viewModel: SplashViewModel =
            ViewModelProvider(this).get(SplashViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
        ConstantUtils.createFullScreen(this)

        viewModel.observerEvents.observe(this, Observer {it ->
            if(it != null){
                observerEvents = it
                askNotificationPermission(it)
                //validateViewModelEvents(it)
            }


        });


    }

    private fun askNotificationPermission(observerEvents: Constants.ObserverEvents) {
        //this.observerEvents = observerEvents
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
               validateViewModelEvents(observerEvents)
            } else {
//                Log.e(TAG, "NO_PERMISSION")
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }else{
            validateViewModelEvents(observerEvents)
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            askNotificationPermission(observerEvents)
        } else {
//            Toast.makeText(
//                this, "${getString(R.string.app_name)} can't post notifications without Notification permission",
//                Toast.LENGTH_LONG
//            ).show()

            Snackbar.make(
                binding.containermain,
                String.format(
                    String.format(
                        getString(R.string.txt_error_post_notification),
                        getString(R.string.app_name)
                    )
                ),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.goto_settings)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(settingsIntent)
                }
            }.show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)


    }

    fun validateViewModelEvents(observerEvents: Constants.ObserverEvents) {
        if (observerEvents === Constants.ObserverEvents.OPEN_SIGN_IN_SCREEN) {
            Intent(applicationContext, SignInSignUpActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }else if (observerEvents === Constants.ObserverEvents.OPEN_BASIC_PROFILE) {
            Intent(applicationContext, BasicProfileActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }else if (observerEvents === Constants.ObserverEvents.OPEN_HOME_SCREEN) {
            Intent(applicationContext, HomeActivity::class.java).also {
                startActivity(it)
                finish()
            }
        } /*else if (observerEvents === Constants.ObserverEvents.STOP_LOADER) {

        }*/
    }










}