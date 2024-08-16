package com.stranger_sparks.agora.media.chat

import com.stranger_sparks.R
import com.stranger_sparks.agora.media.RtcTokenBuilder2
import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.stranger_sparks.agora.media.fcm.FCMSend
import com.stranger_sparks.databinding.ActivityVideoCallBinding
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

class VideoCallActivity : AppCompatActivity() {

    private val appId = "409f045d1b02482ab8b3a4af3f567fc3"

    var appCertificate = "7acdda6505e943ecbddaccb719c2f31d"
    var expirationTimeInSeconds = 3600
    private var channelName = "papayacoders"

//    private var token : String? = null

    private var token =
        "007eJxTYDDp3SWzb5uimNhB/V+mL5lvLmFi/xjmUq06+8V/76bHa+YoMJimJVkamZlaWiamJpkkJRpbGiYbWRqZmCcnGyeZpZkbKnYuTG4IZGRgnyjPxMgAgSA+D0NBYkFiZWJyfkpqUTEDAwAi+yGx"

    private val uid = 0
    private var isJoined = false
    private var is_from_notify = false

    private var agoraEngine: RtcEngine? = null

    private var localSurfaceView: SurfaceView? = null

    private var remoteSurfaceView: SurfaceView? = null


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
            val v=TextView(applicationContext)

            joinChannel(v)
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }

    private lateinit var binding: ActivityVideoCallBinding

    var channel_id=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        channel_id=intent.getStringExtra("channel_id").toString()
        is_from_notify=intent.getBooleanExtra("is_from_notify",false)
Log.e("is_from_notify","is_from_notify $is_from_notify")
        channelName=channel_id
        val tokenBuilder = RtcTokenBuilder2()
        val timestamp = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()

        println("UID token")
        val result = tokenBuilder.buildTokenWithUid(
            appId, appCertificate,
            channel_id, uid, RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp
        )
        println(result)


        //token = result


        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        binding.ivClose.setOnClickListener {
            onBackPressed()
        }
        setupVideoSDKEngine();
    }

    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this@VideoCallActivity)
        builder.setMessage("Are you sure to Leave the Room?")
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.dismiss()
                    if (!isJoined) {
                        showMessage("Join a channel first")
                    } else {
                        agoraEngine!!.leaveChannel()
                        showMessage("You left the channel")
                        if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
                        if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
                        isJoined = false
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
                })


        builder.create()
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

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            // Set the remote video view
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }
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
            if(is_from_notify)
                setupRemoteVideo(1)
            else
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(token, channel_id, uid, options)

        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun leaveChannel(view: View) {
        if (!isJoined) {
            showMessage("Join a channel first")
        } else {
            agoraEngine!!.leaveChannel()
            showMessage("You left the channel")
            if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
            if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
            isJoined = false
        }
    }

}