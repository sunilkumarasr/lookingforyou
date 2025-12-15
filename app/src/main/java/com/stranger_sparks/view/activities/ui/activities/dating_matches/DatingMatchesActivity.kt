package com.stranger_sparks.view.activities.ui.activities.dating_matches

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.stranger_sparks.R
import com.stranger_sparks.adapterrs.SectionsPagerAdapter
import com.stranger_sparks.databinding.ActivityDatingMatchesBinding
import com.stranger_sparks.fcm.ZegoCallManager
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.utils.SharedPreferenceManager

class DatingMatchesActivity : AppCompatActivity() {
    lateinit var binding: ActivityDatingMatchesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        ConstantUtils.darkModeDisable()
        super.onCreate(savedInstanceState)
        binding = ActivityDatingMatchesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ConstantUtils.changeNotificationBarColor(this, ContextCompat.getColor(this, R.color.notification_bar_color_two), false)

        val viewModel: DatingMatchesViewModel =
            ViewModelProvider(this).get(DatingMatchesViewModel::class.java);
        viewModel.binding = binding
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.viewModel!!.observerEvents.observe(this) { observerEvents ->
            observerEvents?.let { validateViewModelEvents(observerEvents) }
        }

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        //incoming call received
        incomingCallReceived()

    }

    private fun incomingCallReceived() {
        //incoming call received
        val sharedPreferenceManager = SharedPreferenceManager(this)
        val userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.orEmpty()
        val userName = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.name.orEmpty()
        ZegoCallManager.initialize(application, userID, userName)
    }

    private fun validateViewModelEvents(observerEvents: Constants.ObserverEvents) {
        if (observerEvents === Constants.ObserverEvents.CLOSE_NOTIFICATION_SCREEN) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        //incoming call received
        incomingCallReceived()
    }

}