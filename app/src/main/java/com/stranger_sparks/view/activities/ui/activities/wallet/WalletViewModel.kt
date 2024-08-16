package com.stranger_sparks.view.activities.ui.activities.wallet

import android.app.Application
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stranger_sparks.api_dragger_flow.repository.StrangerSparksRepository
import com.stranger_sparks.data_model.StatusMessageDataResponse
import com.stranger_sparks.data_model.StatusMessageResponse
import com.stranger_sparks.databinding.ActivitySettingsBinding
import com.stranger_sparks.databinding.ActivityWalletBinding
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.viewmodel.BaseViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class WalletViewModel@Inject constructor(private val strangerSparksRepository: StrangerSparksRepository) : ViewModel() {
    private val _inputSignal = MutableLiveData<String?>()
    val inputSignal: LiveData<String?> get() = _inputSignal

    private val _getWalletData = MutableLiveData<StatusMessageDataResponse?>()
    val getWalletData: LiveData<StatusMessageDataResponse?> get() = _getWalletData

    private val _addWalletAmount = MutableLiveData<StatusMessageResponse?>()
    val addWalletAmount: LiveData<StatusMessageResponse?> get() = _addWalletAmount

    private val _walletWithdrawal = MutableLiveData<StatusMessageResponse?>()
    val walletWithdrawal: LiveData<StatusMessageResponse?> get() = _walletWithdrawal

    fun getWalletAmount(user_id: String) {
        strangerSparksRepository.getWalletByUser(user_id).enqueue(object :
            Callback<StatusMessageDataResponse> {
            override fun onResponse(call: Call<StatusMessageDataResponse>, response: Response<StatusMessageDataResponse>) {
                if (response.isSuccessful) {
                    _getWalletData.value = response.body()
                } else {
                    _getWalletData.value =null
                }
            }

            override fun onFailure(call: Call<StatusMessageDataResponse>, t: Throwable) {
                _getWalletData.value = null
            }
        })
    }
    fun walletWithdrawal(user_id: String,
                         amount: String,
                         reason: String,
                         account_number: String,
                         confirm_account_number: String,
                         ifsc_code: String,branch: String,) {
        strangerSparksRepository.walletWithdrawal(user_id, amount, reason, account_number, confirm_account_number, ifsc_code,branch).enqueue(object :
            Callback<StatusMessageResponse> {
            override fun onResponse(call: Call<StatusMessageResponse>, response: Response<StatusMessageResponse>) {
                if (response.isSuccessful) {
                    _walletWithdrawal.value = response.body()
                } else {
                    _walletWithdrawal.value =null
                }
            }

            override fun onFailure(call: Call<StatusMessageResponse>, t: Throwable) {
                _walletWithdrawal.value = null
            }
        })
    }
    fun addWalletAmount(user_id: String, amount: String) {
        strangerSparksRepository.addWallet(user_id, amount).enqueue(object :
            Callback<StatusMessageResponse> {
            override fun onResponse(call: Call<StatusMessageResponse>, response: Response<StatusMessageResponse>) {
                if (response.isSuccessful) {
                    _addWalletAmount.value = response.body()
                } else {
                    _addWalletAmount.value =null
                }
            }

            override fun onFailure(call: Call<StatusMessageResponse>, t: Throwable) {
                _addWalletAmount.value = null
            }
        })
    }


}


/*(application: Application?) : BaseViewModel(application) {
    var binding: ActivityWalletBinding? = null
    fun backPressedClick() {
        observerEvents.setValue(Constants.ObserverEvents.BACK_PRESS)
    }
    fun closeWallet(){

        if(binding?.llMyWallet?.isVisible == true)
        observerEvents.setValue(Constants.ObserverEvents.CLOSE_WALLET_SCREEN)
        else if(binding?.llAddAmount?.isVisible == true || binding?.llWithDrawalAmount?.isVisible == true){
            binding?.llMyWallet?.visibility = View.VISIBLE
            binding?.llAddAmount?.visibility = View.GONE
            binding?.llWithDrawalAmount?.visibility = View.GONE
        }
    }
    fun addAmountContinue(){

    }fun withdrawAmountSubmit(){

    }
    fun openAddAmount(){
        binding?.llMyWallet?.visibility = View.GONE
        binding?.llAddAmount?.visibility = View.VISIBLE
        binding?.llWithDrawalAmount?.visibility = View.GONE
    }fun showTransactions(){
        observerEvents.setValue(Constants.ObserverEvents.SHOW_TRANSACTIONS)
    }
    fun openWithdraw(){
        binding?.llMyWallet?.visibility = View.GONE
        binding?.llAddAmount?.visibility = View.GONE
        binding?.llWithDrawalAmount?.visibility = View.VISIBLE
    }
}*/