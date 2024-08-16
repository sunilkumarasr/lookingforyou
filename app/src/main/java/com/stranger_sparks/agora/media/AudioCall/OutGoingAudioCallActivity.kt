package com.stranger_sparks.agora.media.AudioCall

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.agora.media.AgoraViewModel
import com.stranger_sparks.agora.media.RtcTokenBuilder2
import com.stranger_sparks.databinding.ActivityAudioCallBinding
import com.stranger_sparks.utils.ConstantUtils
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import javax.inject.Inject


class OutGoingAudioCallActivity : AppCompatActivity() {

    private val appId = "409f045d1b02482ab8b3a4af3f567fc3"
    var appCertificate = "7acdda6505e943ecbddaccb719c2f31d"
    var expirationTimeInSeconds = 3600
    var channelName: String = "papayacoders"
    private var token : String? = null
//    private val token =
//        "007eJxTYDDp3SWzb5uimNhB/V+mL5lvLmFi/xjmUq06+8V/76bHa+YoMJimJVkamZlaWiamJpkkJRpbGiYbWRqZmCcnGyeZpZkbKnYuTG4IZGRgnyjPxMgAgSA+D0NBYkFiZWJyfkpqUTEDAwAi+yGx"
    private val uid = 0
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null


    //ring
    private lateinit var mediaPlayer: MediaPlayer

    //exit time
    private var userID = ""
    private var profile_id = ""
    var subscriptionAudioTime: String = ""
    var exitAudioTime: String = "00:00:00"
    @Inject
    lateinit var viewModel: AgoraViewModel

    //permission
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )


    //call end api run
    private var isApiCalled = false

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

    fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }

    //call time
    private var handler = android.os.Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var elapsedTime = 0L
    private var running = false


    private lateinit var binding: ActivityAudioCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityAudioCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ConstantUtils.changeNotificationBarColor(this, ContextCompat.getColor(this, R.color.black), false)


        userID = intent.getStringExtra("userID").toString()
        profile_id = intent.getStringExtra("profile_id").toString()
        //channelName = intent.getStringExtra("email").toString()
        subscriptionAudioTime = intent.getStringExtra("subscriptionAudioTime").toString()
        val tvName = intent.getStringExtra("tvName")
        binding.callerName.text = tvName

        val tokenBuilder = RtcTokenBuilder2()
        val timestamp = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()

        println("UID token")
        val result = tokenBuilder.buildTokenWithUid(
            appId, appCertificate,
            channelName, uid, RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp
        )
        println(result)

        token = result

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVideoSDKEngine();

        //call start
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(token, channelName, uid, options)

            //ring
            mediaPlayer = MediaPlayer.create(this, R.raw.outgoing_ring)
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.seekTo(0)
                mediaPlayer.start()
            }
        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT)
                .show()
        }

        binding.ivClose.setOnClickListener {
            //stop ring
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            exitApiRun()
        }


    }
    //call time
    private fun callTime() {
        if (!running) {
            startTime = System.currentTimeMillis() - elapsedTime
            handler.post(updateTimeTask)
            running = true
        }
    }
    private val updateTimeTask = object : Runnable {
        override fun run() {
            if (running) {
                elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000).toInt() % 60
                val minutes = (elapsedTime / (1000 * 60) % 60).toInt()
                val hours = (elapsedTime / (1000 * 60 * 60)).toInt()
                binding.callDuration.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                handler.postDelayed(this, 1000)
                exitAudioTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                if (exitAudioTime.equals(subscriptionAudioTime)){
                    showMessage("Sorry! Your Subscription Expired")
                    callLeft()
                }

            }
        }
    }


    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            //stop ring
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }

            //startTime
            callTime()

            // Set the remote video view
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined call $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Call End")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE

                //other person call cut
                exitApiRun()
            }


        }

    }

    private fun setupRemoteVideo(uid: Int) {
        remoteSurfaceView = SurfaceView(baseContext)
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        binding.remoteVideoViewContainer.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
        remoteSurfaceView!!.visibility = View.VISIBLE
    }

    private fun setupLocalVideo() {
        localSurfaceView = SurfaceView(baseContext)
        binding.localVideoViewContainer.addView(localSurfaceView)
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
    }

    fun joinChannel(view: View) {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()

            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(token, channelName, uid, options)
        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun leaveChannel(view: View) {
        callLeft()
    }

    private fun callLeft() {
        //stop ring
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        exitApiRun()

        if (!isJoined) {
            showMessage("Join a call first")
        } else {
            agoraEngine!!.leaveChannel()
            showMessage("You left the call")
            exitApiRun()
            if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
            if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
            isJoined = false
        }
    }

    private fun exitApiRun() {
        if (isApiCalled) return
        isApiCalled = true

        Log.e("exitApiRun_", "1")
        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Loading......")
        mProgressDialog.show()

        viewModel.userAudioCall(userID, profile_id, exitAudioTime)
        viewModel.userAudioCallLiveData.observe(this) { res ->
            res?.message?.let { ConstantUtils.showToast(this, it) }
            mProgressDialog.dismiss()
            if (res?.status == true) {
                finish()
            } else {
                finish()
            }
            isApiCalled = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //stop ring
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }

        exitApiRun()
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

}