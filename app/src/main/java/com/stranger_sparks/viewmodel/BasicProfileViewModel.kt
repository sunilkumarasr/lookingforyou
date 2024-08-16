package com.stranger_sparks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stranger_sparks.api_dragger_flow.repository.StrangerSparksRepository
import com.stranger_sparks.data_model.CitysListResponse
import com.stranger_sparks.data_model.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class BasicProfileViewModel@Inject constructor(private val strangerSparksRepository: StrangerSparksRepository) : ViewModel() {
    private val _inputSignal = MutableLiveData<String?>()

    val inputSignal: LiveData<String?> get() = _inputSignal

    private val _citysListData = MutableLiveData<CitysListResponse?>()
    val cityListLiveData: LiveData<CitysListResponse?> get() = _citysListData


    private val _upDateProfileLiveData = MutableLiveData<LoginResponse?>()
    val upDateProfileLiveData: LiveData<LoginResponse?> get() = _upDateProfileLiveData

    fun updateProfile(full_name: RequestBody,
                      age: RequestBody,
                      location: RequestBody,
                      email: RequestBody,
                      phone: RequestBody,
                      height: RequestBody,
                      description: RequestBody,
                      user_id: RequestBody,
                      gender: RequestBody,
                      hobbies: RequestBody,
                      alterNativeNumber: RequestBody,
                      marital: RequestBody,
                      languages: RequestBody,
                      image_outlet: MultipartBody.Part) {
        strangerSparksRepository.updateProfile(full_name,
            age,
            location,
            email,
            phone,
            height,
            description,
            user_id,
            gender,
            hobbies,
            alterNativeNumber,
            marital,
            languages,
            image_outlet).enqueue(object :
            Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    _upDateProfileLiveData.value = response.body()
                } else {
                    _upDateProfileLiveData.value =null
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _upDateProfileLiveData.value = null
            }
        })
    }





    fun getCitysList() {
        strangerSparksRepository.getCitysList().enqueue(object :
            Callback<CitysListResponse> {
            override fun onResponse(call: Call<CitysListResponse>, response: Response<CitysListResponse>) {
                if (response.isSuccessful) {
                    _citysListData.value = response.body()
                } else {
                    _citysListData.value =null
                }
            }
            override fun onFailure(call: Call<CitysListResponse>, t: Throwable) {
                _citysListData.value = null
            }
        })
    }








}