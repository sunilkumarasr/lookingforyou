package com.stranger_sparks.view.activities.ui.activities.account.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.stranger_sparks.BuildConfig
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.databinding.ActivityGalleryBinding
import com.stranger_sparks.databinding.ActivityGalleryImageZoomBinding
import com.stranger_sparks.fcm.ZegoCallManager
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.SharedPreferenceManager
import javax.inject.Inject

class GalleryImageZoomActivity : AppCompatActivity() {

    lateinit var binding: ActivityGalleryImageZoomBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        ConstantUtils.darkModeDisable()
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryImageZoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //hide statusbar
        supportActionBar?.hide()
        ConstantUtils.changeNotificationBarColor(this, ContextCompat.getColor(this, R.color.app_red), false)

        val imageUrl = intent.getStringExtra("IMAGE_URL")

        Glide.with(this@GalleryImageZoomActivity)
            .load(imageUrl)
            .dontAnimate()
            .into(binding.photoView);


        binding.photoView.setOnClickListener {
            binding.photoView.scale = binding.photoView.scale * 1.5f
        }

        binding.ivClose.setOnClickListener {
            finish()
        }

        //incoming call received
        incomingCallReceived()

    }


    private fun incomingCallReceived() {
        val sharedPreferenceManager = SharedPreferenceManager(this)
        val userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.orEmpty()
        val userName = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.name.orEmpty()
        ZegoCallManager.initialize(application, userID, userName)
    }

    override fun onResume() {
        super.onResume()
        //incoming call received
        incomingCallReceived()
    }

}