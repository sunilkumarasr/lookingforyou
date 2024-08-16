package com.stranger_sparks.view.activities.ui.activities.wallet

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.databinding.ActivityWalletBinding
import com.stranger_sparks.databinding.ActivityWalletTransectionsBinding
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.utils.SharedPreferenceManager
import com.stranger_sparks.view.activities.SignInSignUpActivity
import com.stranger_sparks.view.activities.ui.activities.account.profile.ProfileViewModel
import com.stranger_sparks.view.activities.ui.activities.notifications.Notifications
import com.stranger_sparks.view.activities.ui.activities.wallet.transections.WalletTransactionsActivity
import com.stranger_sparks.view.activities.ui.activities.wallet.transections.WalletTransactionsViewModel
import javax.inject.Inject

class WalletActivity : AppCompatActivity() {
    lateinit var binding: ActivityWalletBinding
    @Inject
    lateinit var viewModel: WalletViewModel
    lateinit var userID: String
    lateinit var fullName: String
    lateinit var gender: String
    lateinit var hobbies: String
    lateinit var height: String
    lateinit var location: String
    lateinit var email: String
    lateinit var image: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ConstantUtils.changeNotificationBarColor(this, ContextCompat.getColor(this, R.color.black), false)


        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val sharedPreferenceManager = SharedPreferenceManager(this)
        userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.toString()


        viewModel.getWalletAmount(userID)
        viewModel.getWalletData.observe(this){
            binding.tvWalletBalance.text = it?.data.toString()
        }

        viewModel.walletWithdrawal.observe(this){

            if(it?.status == true){
                ConstantUtils.showToast(applicationContext, it.message)
                binding.llMyWallet.visibility = View.VISIBLE
                binding.llAddAmount.visibility = View.GONE
                binding.llWithDrawalAmount.visibility = View.GONE
                viewModel.getWalletAmount(userID)
            }else{
                it?.message?.let { it1 -> ConstantUtils.showToast(applicationContext, it1) }
            }
        }

        Glide.with(applicationContext).load(sharedPreferenceManager.getSavedLoginResponseUser()?.data?.image)
            .error(R.drawable.img_placeholder)
            .transform(CenterCrop(), RoundedCorners(10))
            .into(binding.ivProfileImage)

        viewModel.inputSignal.observe(this) {

            if (it != null) {
                validateViewModelEvents(it)

            }
        }
        binding.ivLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to logout?")
            builder.setTitle("Alert !")
            builder.setCancelable(false)
            builder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    if(SharedPreferenceManager(this).clearAllData()){
                        Intent(applicationContext, SignInSignUpActivity::class.java).also {
                            startActivity(it)
                            finish()
                        }
                    }
                } as DialogInterface.OnClickListener)
            builder.setNegativeButton("No",
                DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                    // If user click no then dialog box is canceled.
                    dialog.cancel()
                } as DialogInterface.OnClickListener)
            val alertDialog = builder.create()
            alertDialog.show()
        }
        binding.btnContinue.setOnClickListener {
            if(checkAmountValidation()){
                viewModel.addWalletAmount(userID, binding.etAmount.text.trim().toString())

            }
        }
        binding.btnSubmit.setOnClickListener {
            if(checkWithdrawalValidation()){
                viewModel.walletWithdrawal(userID, binding.etWithdrawAmount.text.trim().toString(),
                    binding.etReason.text.trim().toString(),
                    binding.etAccountNumber.text.trim().toString(),
                    binding.etConfirmAccountNumber.text.trim().toString(),
                    binding.etIFSCode.text.trim().toString(),binding.etBranch.text.trim().toString())

            }
        }
        viewModel.walletWithdrawal.observe(this){
            if(it?.status == true){
                it?.message?.let { it1 -> ConstantUtils.showSuccessToast(this, it1) }
                binding.etWithdrawAmount.setText("")
                binding.etReason.setText("")
                binding.etAccountNumber.setText("")
                binding.etConfirmAccountNumber.setText("")
                binding.etBranch.setText("")
                binding.etIFSCode.setText("")
                binding?.llMyWallet?.visibility = View.VISIBLE
                binding?.llAddAmount?.visibility = View.GONE
                binding?.llWithDrawalAmount?.visibility = View.GONE
                viewModel.getWalletAmount(userID)

            }else{
                it?.message?.let { it1 -> ConstantUtils.showToast(this, it1) }
            }
        }
        viewModel.addWalletAmount.observe(this){
            if(it?.status == true){
                it?.message?.let { it1 -> ConstantUtils.showSuccessToast(this, it1) }
                binding?.llMyWallet?.visibility = View.VISIBLE
                binding?.llAddAmount?.visibility = View.GONE
                binding?.llWithDrawalAmount?.visibility = View.GONE
                viewModel.getWalletAmount(userID)

            }else{
                it?.message?.let { it1 -> ConstantUtils.showToast(this, it1) }
            }
        }
        binding.ivClose.setOnClickListener {
            if(binding.llMyWallet.isVisible == true)
                finish()
            else if(binding?.llAddAmount?.isVisible == true || binding?.llWithDrawalAmount?.isVisible == true){
                binding?.llMyWallet?.visibility = View.VISIBLE
                binding?.llAddAmount?.visibility = View.GONE
                binding?.llWithDrawalAmount?.visibility = View.GONE
            }
        }
        binding.btnAddAmount.setOnClickListener {
            binding.llMyWallet.visibility = View.GONE
            binding.llAddAmount.visibility = View.VISIBLE
            binding.llWithDrawalAmount.visibility = View.GONE
        }
        binding.btnTransection.setOnClickListener {
            Intent(applicationContext, WalletTransactionsActivity::class.java).also {
                startActivity(it)

            }
        }

        binding.tvWithdrawal.setOnClickListener {

            binding?.llMyWallet?.visibility = View.GONE
            binding?.llAddAmount?.visibility = View.GONE
            binding?.llWithDrawalAmount?.visibility = View.VISIBLE
        }

    }



    fun checkAmountValidation(): Boolean {
        var ret = true
        if (!ConstantUtils.hasEditText(binding.etAmount, "Please Enter Amount")) ret = false
        return ret
    }
    fun checkWithdrawalValidation(): Boolean {
        var ret = true

        if (!ConstantUtils.hasEditText(binding.etWithdrawAmount, "Please Enter Withdraw Amount")) ret = false
        if (!ConstantUtils.hasEditText(binding.etAccountNumber, "Please Enter Account Number")) ret = false
        if (!ConstantUtils.hasEditText(binding.etBranch, "Please Enter Branch Name")) ret = false
        if (!ConstantUtils.hasEditText(binding.etConfirmAccountNumber, "Please Enter Confirm Account Number")) ret = false
        if (!ConstantUtils.hasEditText(binding.etReason, "Please Enter Reason")) ret = false
        if (!ConstantUtils.hasEditText(binding.etIFSCode, "Please Enter IFSC code")) ret = false
        if (binding.etAccountNumber.text.toString() != binding.etConfirmAccountNumber.text.toString()
        ) {
            binding.etAccountNumber.setError("Please Enter Account Number and Conform Account number Same")
            binding.etConfirmAccountNumber.setError("Please Enter Account Number and Conform Account number Same")
            ret = false}

        return ret
    }

    private fun validateViewModelEvents(observerEvents: String) {
        if (observerEvents === Constants.ObserverEvents.CLOSE_NOTIFICATION_SCREEN.toString()) {

            finish()
        }
    }
}