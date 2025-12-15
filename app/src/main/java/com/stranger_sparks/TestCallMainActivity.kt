package com.stranger_sparks

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.stranger_sparks.utils.ConstantUtils
import com.tencent.mmkv.MMKV
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import im.zego.zim.entity.ZIMPushConfig
import java.util.Collections

class TestCallMainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        ConstantUtils.darkModeDisable()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_call_main)


        val yourUserID: TextView = findViewById(R.id.your_user_id)
        val yourUserName: TextView = findViewById(R.id.your_user_name)

        val userID = MMKV.defaultMMKV().getString("userID", "")
        val userName = MMKV.defaultMMKV().getString("userName", "")


        yourUserID.text = "Your User ID :$userID"
        yourUserName.text = "Your User Name :$userName"


        val appID: Long = 488664616
        val appSign: String = "bbc77be6fd6f4009df065819096427fc1db2f7a8086b0810b9da07f7604ed72c"

        initCallInviteService(appID, appSign, userID ?: "", userName ?: "")

        initVoiceButton()
        initVideoButton()


        findViewById<View>(R.id.user_logout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure to Sign Out?")
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    ZegoUIKitPrebuiltCallService.unInit()
                    finish()
                }
                .create()
                .show()
        }


    }

    private fun initCallInviteService(
        appID: Long,
        appSign: String,
        userID: String,
        userName: String
    ) {
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        ZegoUIKitPrebuiltCallService.init(
            application,
            appID,
            appSign,
            userID,
            userName,
            callInvitationConfig
        )
    }

    private fun initVoiceButton() {

        val newVoiceCall: ZegoSendCallInvitationButton = findViewById(R.id.new_voice_call)
        newVoiceCall.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val inputLayout: EditText = findViewById(R.id.target_user_id)
                val targetUserIDs = inputLayout.text.toString()

                if (targetUserIDs.isNotEmpty()) {
                    val userIdList = targetUserIDs.split(",")
                    val invitees = mutableListOf<ZegoUIKitUser>()

                    for (userID in userIdList) {
                        val userName = "${userID}_name"
                        invitees.add(ZegoUIKitUser(userID.trim(), userName))
                    }

                    // Set up invitation parameters
                    newVoiceCall.setIsVideoCall(false)
                    newVoiceCall.setResourceID("zego_uikit_call") // Match your ZEGOCLOUD Console setting
                    newVoiceCall.setInvitees(invitees)
                } else {
                    Toast.makeText(
                        this@TestCallMainActivity,
                        "Please enter valid user IDs",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }


    private fun initVideoButton() {
        val newVideoCall: ZegoSendCallInvitationButton = findViewById(R.id.new_video_call)

        newVideoCall.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val inputLayout: EditText = findViewById(R.id.target_user_id)
                val targetUserIDs = inputLayout.text.toString()

                if (targetUserIDs.isNotEmpty()) {
                    val userIdList = targetUserIDs.split(",")
                    val invitees = mutableListOf<ZegoUIKitUser>()

                    for (userID in userIdList) {
                        val userName = "${userID}_name"
                        invitees.add(ZegoUIKitUser(userID.trim(), userName))
                    }

                    // Set up invitation parameters
                    newVideoCall.setIsVideoCall(true)
                    newVideoCall.setResourceID("zego_uikit_call") // Match your ZEGOCLOUD Console setting
                    newVideoCall.setInvitees(invitees)


                } else {
                    Toast.makeText(
                        this@TestCallMainActivity,
                        "Please enter valid user IDs",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallService.unInit()
    }

}
