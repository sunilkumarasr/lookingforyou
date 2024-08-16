package com.stranger_sparks.view.activities

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.databinding.ActivityOtpVerificationBinding
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.utils.SharedPreferenceManager
import com.stranger_sparks.viewmodel.OtpVerificationViewModel
import com.stranger_sparks.viewmodel.SignInSignUpViewModel
import `in`.aabhasjindal.otptextview.OTPListener
import javax.inject.Inject


class OtpVerificationActivity : AppCompatActivity() {
    private lateinit var phoneNumber: String
    lateinit var binding: ActivityOtpVerificationBinding
    lateinit var enteredOtp: String
    lateinit var token: String
    lateinit var otp: String
    @Inject
    lateinit var viewModel: OtpVerificationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        val intent = intent
        if (intent.hasExtra("phone_number")) {
            phoneNumber = intent.extras?.getString("phone_number").toString()
            binding.tvTileOfText.text = "Enter the four digit code we sent to you +91 ${phoneNumber}"
        } else {
            phoneNumber = ""
        }

        otp= intent.getStringExtra("otp_").toString()
        binding.txtOTP.text = otp
        binding.txtOTP.visibility=View.INVISIBLE
        enteredOtp = ""
        token = FirebaseInstanceId.getInstance().token.toString()
        Log.d(ContentValues.TAG, "Token : $token")

        binding.lifecycleOwner = this
        setContentView(binding.root)
        ConstantUtils.createFullScreen(this)
        binding.otpView?.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }
            override fun onOTPComplete(otp: String) {
                enteredOtp = otp
            }
        }

        binding.btnVerifyOtp.setOnClickListener {

            if(enteredOtp.isNotBlank()){

                binding.progressLay.progressBar.visibility = View.VISIBLE
                if(token==null||token.isEmpty())
                token = FirebaseInstanceId.getInstance().token.toString()
                viewModel.validateOtp(phoneNumber, enteredOtp,token)

            }else{
                ConstantUtils.showToast(this, "Please Enter OTP")
            }
        }
        viewModel.loginLiveData.observe(this) { resp ->
            if (resp?.status == true) {
                binding.progressLay.progressBar.visibility = View.GONE
                resp?.message?.let { //ConstantUtils.showToast(this, it)
                     }
                val sharedPreferenceManager = SharedPreferenceManager(this)
                if (resp != null) {
                    sharedPreferenceManager.saveLoginResponse(resp)
                }
                if (sharedPreferenceManager.getSavedLoginResponseUser()?.data?.profile_completed == "0") {
                    Intent(applicationContext, BasicProfileActivity::class.java).also {
                        startActivity(it)
                    }
                } else {
                    Intent(applicationContext, HomeActivity::class.java).also {
                        startActivity(it)
                    }
                }
            }else {
                resp?.message?.let { ConstantUtils.showToast(this, it) }
            }


        }

    }

    fun validateViewModelEvents(observerEvents: Constants.ObserverEvents) {
        if (observerEvents === Constants.ObserverEvents.BACK_PRESS) {

            finish()
        } else if (observerEvents === Constants.ObserverEvents.GOTO_SCUBSCRIPTION) {
            Intent(applicationContext, Subcription::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }


}