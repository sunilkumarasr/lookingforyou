package com.stranger_sparks.agora.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stranger_sparks.api_dragger_flow.repository.StrangerSparksRepository
import com.stranger_sparks.data_model.GalleryImagesResponse
import com.stranger_sparks.data_model.GetUserProfileResponse
import com.stranger_sparks.data_model.ProfileViewDetailsResponse
import com.stranger_sparks.data_model.StatusMessageResponse
import com.stranger_sparks.databinding.ActivitySettingsBinding
import com.stranger_sparks.databinding.OutgoingVideoCallBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AgoraViewModel   @Inject constructor(private val strangerSparksRepository: StrangerSparksRepository) :
    ViewModel() {
    var bindingVideo: OutgoingVideoCallBinding? = null

    private val _userAudioCallLiveData = MutableLiveData<StatusMessageResponse?>()
    val userAudioCallLiveData: LiveData<StatusMessageResponse?> get() = _userAudioCallLiveData

    private val _userVideoCallLiveData = MutableLiveData<StatusMessageResponse?>()
    val userVideoCallLiveData: LiveData<StatusMessageResponse?> get() = _userVideoCallLiveData

    fun userAudioCall(user_id: String, profile_id: String, exitTime: String) {
        strangerSparksRepository.userAudioCall(user_id, profile_id, exitTime).enqueue(object :
            Callback<StatusMessageResponse> {
            override fun onResponse(
                call: Call<StatusMessageResponse>,
                response: Response<StatusMessageResponse>
            ) {
                if (response.isSuccessful) {
                    _userAudioCallLiveData.value = response.body()
                } else {
                    _userAudioCallLiveData.value = null
                }
            }

            override fun onFailure(call: Call<StatusMessageResponse>, t: Throwable) {
                _userAudioCallLiveData.value = null
            }
        })
    }


    fun userVideoCall(user_id: String, profile_id: String, exitTime: String) {
        strangerSparksRepository.userVideoCall(user_id, profile_id,exitTime).enqueue(object :
            Callback<StatusMessageResponse> {
            override fun onResponse(
                call: Call<StatusMessageResponse>,
                response: Response<StatusMessageResponse>
            ) {
                if (response.isSuccessful) {
                    _userVideoCallLiveData.value = response.body()
                } else {
                    _userVideoCallLiveData.value = null
                }
            }

            override fun onFailure(call: Call<StatusMessageResponse>, t: Throwable) {
                _userVideoCallLiveData.value = null
            }
        })
    }

}