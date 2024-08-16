package com.stranger_sparks.view.activities.ui.activities.display_user

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckedTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.stranger_sparks.BuildConfig
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.adapterrs.ProfileGalaryImagesAdapter
import com.stranger_sparks.agora.media.AudioCall.OutGoingAudioCallActivity
import com.stranger_sparks.agora.media.VideoCall.OutGoingVideoCallActivity
import com.stranger_sparks.agora.media.chat.VideoCallActivity
import com.stranger_sparks.data_model.GalleryImagesResponse
import com.stranger_sparks.databinding.ActivityDisplayUserBinding
import com.stranger_sparks.inerfaces.OnItemClickListenerProfilesGalleryImages
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.SharedPreferenceManager
import com.stranger_sparks.view.activities.ui.activities.account.gallery.GalleryImageZoomActivity
import com.stranger_sparks.view.activities.ui.activities.chat.chat_room.ChatRoom
import com.stranger_sparks.view.activities.ui.activities.manage_subcription.ManageSubscriptionViewModel
import com.stranger_sparks.viewmodel.SharedProfileViewModel
import javax.inject.Inject


class DisplayUserActivity : AppCompatActivity(), OnItemClickListenerProfilesGalleryImages {
    //var userData: UserProfileResponse.Data? = null
    lateinit var sharedProfileViewModel: SharedProfileViewModel

    lateinit var binding: ActivityDisplayUserBinding

    @Inject
    lateinit var viewModel: DisplayUserViewModel
    lateinit var userID: String
    lateinit var profile_id: String
    lateinit var profileGalaryImagesAdapter: ProfileGalaryImagesAdapter
    private var dataList: MutableList<GalleryImagesResponse.Data> = arrayListOf()
    var is_chart: Boolean = false
    var imgUrl: String = ""
    var token: String = ""

    var Email: String = ""
    var Mobile: String = ""
    var subscriptionAudio: Boolean = false
    var subscriptionAudioTime: String = ""
    var subscriptionVideo: Boolean = false
    var subscriptionVideoTime: String = ""
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityDisplayUserBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //permisstion
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        supportActionBar?.hide()
        ConstantUtils.changeNotificationBarColor(
            this,
            ContextCompat.getColor(this, R.color.app_red),
            false
        )
        profile_id = intent.extras?.getString("PROFILE_ID").toString()
        val sharedPreferenceManager = SharedPreferenceManager(this)
        userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.toString()


        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.ivClose.setOnClickListener {
            finish()
        }

        binding.rcvProfileGalleryImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        profileGalaryImagesAdapter = ProfileGalaryImagesAdapter(this, this)
        binding.rcvProfileGalleryImages.adapter = profileGalaryImagesAdapter

        /*if(userData != null){
            binding.tvName.text = userData!!.name
            binding.tvContent.text = userData!!.description
            binding.tvEmail.text = userData!!.email
            binding.tvHeight.text = userData!!.height
            binding.tvMobile.text = userData!!.phone
            binding.tvLocation.text = userData!!.location

           *//* Glide.with(applicationContext).load(userData!!.image)
                .error(R.drawable.img_placeholder)
                .transform(CenterCrop(), RoundedCorners(10))
                .into(binding.ivProfileImage)*//*
            viewModel.getUserProfileLiveData(userData!!.id)
        }*/

        viewModel.viewProfileLiveData(userID, profile_id)

        // binding.cbLiked.isChecked = true
        viewModel.getUserProfileLiveData(userID, profile_id)
        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Loading......")
        mProgressDialog.show()
        Log.e("userID_", userID)
        Log.e("profile_id_", profile_id)
        viewModel.getUserProfileLiveData.observe(this) { resp ->
            mProgressDialog.dismiss()
            if (resp != null) {
                binding.tvName.text = resp.data.name
                binding.tvTitle.text = resp.data.hobbies

                //call
                subscriptionAudio = resp.data.subscription_audio
                subscriptionAudioTime = resp.data.subscription_audio_time
                subscriptionVideo = resp.data.subscription_video
                subscriptionVideoTime = resp.data.subscription_video_time

                Email = resp.data.email
                Mobile = resp.data.phone

                if (resp.profile_view_status){
                    binding.btnView.visibility = View.GONE
                    binding.tvEmail.text = Email
                    binding.tvMobile.text = Mobile
                }else{
                    binding.btnView.visibility = View.VISIBLE
                    val maskedPhoneNumber = maskPhoneNumber(resp.data.phone)
                    binding.tvMobile.text = maskedPhoneNumber
                }

                binding.tvHeight.text = resp.data.height
                binding.tvLocation.text = resp.data.location
                binding.tvAge.text = "Age: " + resp.data.age + " Years"
                binding.tvDescription.text = resp.data.description
                is_chart = resp.data.is_chart
                if (resp.data.liked_status) {
                    binding.cbLiked.isChecked = true
                } else {
                    binding.cbLiked.isChecked = false
                }
                binding.tvLikedCount.text = resp.data.liked_count.toString()
                imgUrl = resp.data.profile_pic
                Glide.with(applicationContext).load(imgUrl)
                    .error(R.drawable.img_placeholder)
                    .transform(CenterCrop(), RoundedCorners(10))
                    .into(binding.ivProfileImage)
            }
        }
        viewModel.viewProfileLiveData.observe(this) {
            if (it?.status == true) {
                Log.v("Purushotham", "View Profile Status true")
            } else {
                Log.v("Purushotham", "View Profile Status false")
            }
        }

//        //Audio plan details
//        viewModel.manageSubscriptionsV2(userID,"2")
//        viewModel.manageSubscriptionV2LiveData.observe(this){
//            if(it?.status == true){
//                subscriptionAudio = true
//                subscriptionAudioTime = it.data.available_balance
//                binding.tvAudioCallration.visibility = View.VISIBLE
//                binding.tvAudioCallration.text = "Available Audio Call Duration: "+it.data.available_balance
//            }
//        }

//        //Video plan details
//        viewModel.manageSubscriptionsV2(userID,"1")
//        viewModel.manageSubscriptionV2LiveData.observe(this){
//            if(it?.status == true){
//                subscriptionVideo = true
//                subscriptionVideoTime = it.data.available_balance
//                binding.tvVideoCallration.visibility = View.VISIBLE
//                binding.tvVideoCallration.text =  "Available Video Call Duration: "+it.data.available_balance
//            }
//        }

        binding.btnView.setOnClickListener {
            viewModel.profileViewDetails(userID, profile_id)
        }
        viewModel.profileViewDetailsLiveData.observe(this) {
            if (it?.status == true) {
                binding.btnView.visibility = View.GONE
                binding.tvEmail.text = Email
                binding.tvMobile.text = Mobile
            } else {
                ConstantUtils.showToast(applicationContext, "No Subscription")
            }
        }

        binding.cbLiked.setOnClickListener {
            if (!binding.cbLiked.isChecked) {
                (it as CheckedTextView).toggle()
                viewModel.likedProfile(userID, profile_id)
            } else {
                ConstantUtils.showToast(
                    applicationContext,
                    "You already liked " + binding.tvName.text
                )
            }
        }
        viewModel.likeHitChangeLiveData.observe(this) {
            if (it?.status == true) {
                it?.message?.let { it1 -> ConstantUtils.showSuccessToast(this, it1) }
                viewModel.getUserProfileLiveData(userID, profile_id)
            } else {
                it?.message?.let { it1 -> ConstantUtils.showSuccessToast(this, it1) }
            }
        }
        binding.ivMessage.setOnClickListener {
            if (is_chart) {
                Intent(applicationContext, ChatRoom::class.java).also {
                    it.putExtra("PROFILE_ID", profile_id)
                    startActivity(it)
                }
            } else {
                ConstantUtils.showToast(applicationContext, "No Subscription")
            }
        }

        //call start
        var calltype: String = ""
        binding.ivAudioCall.setOnClickListener {
            calltype = "Audio"
            if (subscriptionAudio) {
                if (checkSelfPermission()) {
                    viewModel.userstartcall(userID, profile_id, "Audio")
                } else {
                    ConstantUtils.showToast(applicationContext, "Permissions was not granted")
                }
            } else {
                ConstantUtils.showToast(applicationContext, "No Subscription")
            }
        }

        binding.ivVideoCall.setOnClickListener {
            calltype = "Video"
            if (subscriptionVideo) {
                if (checkSelfPermission()) {
                    viewModel.userstartcall(userID, profile_id, calltype)
                } else {
                    ConstantUtils.showToast(applicationContext, "Permissions was not granted")
                }
            } else {
                ConstantUtils.showToast(applicationContext, "No Subscription")
            }
        }
        viewModel.userstartcallData.observe(this) {
            if (it?.status == true) {
                if (calltype.equals("Audio")) {
                    Intent(applicationContext, OutGoingAudioCallActivity::class.java).also {
                        it.putExtra("userID", userID)
                        it.putExtra("profile_id", profile_id)
                        it.putExtra("email", Email)
                        it.putExtra("tvName", binding.tvName.text)
                        it.putExtra("subscriptionAudioTime", subscriptionAudioTime)
                        startActivity(it)
                    }
                } else {
                    Intent(applicationContext, OutGoingVideoCallActivity::class.java).also {
                        it.putExtra("userID", userID)
                        it.putExtra("profile_id", profile_id)
                        it.putExtra("email", Email)
                        it.putExtra("tvName", binding.tvName.text)
                        it.putExtra("subscriptionVideoTime", subscriptionVideoTime)
                        startActivity(it)
                    }
                }
            } else {
                ConstantUtils.showToast(applicationContext, "Connection Failed Please Try again")
            }
        }

        viewModel.savedGalleryImages(profile_id)
        viewModel.savedGalleryImages.observe(this) {
            if (it?.status == true) {
                // binding.tvNoRecordsDefault.visibility = View.GONE
                dataList.clear()
                dataList.addAll(it.data)
                binding.rcvProfileGalleryImages.visibility = View.VISIBLE
                it?.data?.let { it1 -> profileGalaryImagesAdapter.setDataList(it1) }
                profileGalaryImagesAdapter.notifyDataSetChanged()
            } else {
                // binding.tvNoRecordsDefault.visibility = View.VISIBLE
                binding.rcvProfileGalleryImages.visibility = View.GONE
                binding.noDataTv.visibility = View.VISIBLE
            }
        }

        binding.ivProfileImage.setOnClickListener {
            Intent(applicationContext, GalleryImageZoomActivity::class.java).also {
                it.putExtra("IMAGE_URL", imgUrl)
                startActivity(it)
            }
        }


    }

    fun maskPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length < 4) {
            throw IllegalArgumentException("Phone number must have at least 4 digits")
        }
        val lastFourDigits = phoneNumber.takeLast(4)
        val maskedSection = "*".repeat(phoneNumber.length - 4)
        return maskedSection + lastFourDigits
    }

    override fun clickOnCurrentPositionListener(item: GalleryImagesResponse.Data, position: Int) {
        Intent(applicationContext, GalleryImageZoomActivity::class.java).also {
            it.putExtra("IMAGE_URL", BuildConfig.API_URL + "" + dataList[position].image)
            startActivity(it)
        }
    }

    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            this,
            REQUESTED_PERMISSIONS[0]
        ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    REQUESTED_PERMISSIONS[1]
                ) != PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

//        if(checkSelfPermission())
//        {
//            callVideoChat()
//        }
    }

    fun callVideoChat() {

//        val fcmsend= FCMSend()
//        fcmsend.send("Text","Message",applicationContext)

        Intent(applicationContext, VideoCallActivity::class.java).also {
            it.putExtra("PROFILE_ID", profile_id)
            it.putExtra("channel_id", "1234")
            startActivity(it)
        }

        //startActivity(Intent(applicationContext,VideoCallActivity::class.java))

    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserProfileLiveData(userID, profile_id)
    }

}