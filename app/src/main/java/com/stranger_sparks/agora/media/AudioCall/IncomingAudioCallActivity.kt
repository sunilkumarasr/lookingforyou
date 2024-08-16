package com.stranger_sparks.agora.media.AudioCall

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.stranger_sparks.R
import com.stranger_sparks.agora.media.RtcTokenBuilder2
import com.stranger_sparks.databinding.ActivityIncomingCallBinding
import com.stranger_sparks.utils.ConstantUtils
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class IncomingAudioCallActivity : AppCompatActivity() {


    private val appId = "409f045d1b02482ab8b3a4af3f567fc3"

    var appCertificate = "7acdda6505e943ecbddaccb719c2f31d"
    var expirationTimeInSeconds = 3600
    var channelName: String = ""
    private var token : String? = null

    private val uid = 0
    private var isJoined = false

    private var agoraEngine: RtcEngine? = null

    private var localSurfaceView: SurfaceView? = null

    private var remoteSurfaceView: SurfaceView? = null


    //ring
    private lateinit var mediaPlayer: MediaPlayer


    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

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


    private lateinit var binding: ActivityIncomingCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ConstantUtils.changeNotificationBarColor(this, ContextCompat.getColor(this, R.color.black), false)

        val title = intent.getStringExtra("title")
        //channelName = intent.getStringExtra("email").toString()
        channelName = "papayacoders"

        binding.callerName.text = title

        //ring
        mediaPlayer = MediaPlayer.create(this, R.raw.outgoing_ring)
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.seekTo(0)
            mediaPlayer.start()
        }

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

        binding.JoinButton.setOnClickListener {

            //call start
            if (checkSelfPermission()) {
                setupVideoSDKEngine();
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

        binding.ivClose.setOnClickListener {
            //stop ring
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            finish()
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
            }
        }
    }


    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            runOnUiThread {
                binding.JoinButton?.visibility = View.GONE

                // Stop the ring if mediaPlayer is playing
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }

                // Start time
                callTime()

                // Set the remote video view
                setupRemoteVideo(uid)
            }

        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            //showMessage("Joined call $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Call End")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }

            //other person call cut
            finish()
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


    fun leaveChannel(view: View) {
        //stop ring
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        finish()

        if (!isJoined) {
            showMessage("Join a call first")
        } else {
            agoraEngine!!.leaveChannel()
            showMessage("You left the call")
            if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
            if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
            isJoined = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //stop ring
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()

        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

}