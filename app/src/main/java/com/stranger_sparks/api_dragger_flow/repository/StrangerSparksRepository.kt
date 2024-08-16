package com.stranger_sparks.api_dragger_flow.repository

import com.stranger_sparks.api_dragger_flow.retrofit.StrangerSparksApiInterface
import com.stranger_sparks.data_model.ChatResponse
import com.stranger_sparks.data_model.CitysListResponse
import com.stranger_sparks.data_model.GalleryImagesResponse
import com.stranger_sparks.data_model.GetAboutTermsPrivacyDTO
import com.stranger_sparks.data_model.GetUserProfileResponse
import com.stranger_sparks.data_model.LikeLikedResponse
import com.stranger_sparks.data_model.LoginResponse
import com.stranger_sparks.data_model.ManageSubscriptionResponse
import com.stranger_sparks.data_model.NotificationsResponse
import com.stranger_sparks.data_model.ProfileViewDetailsResponse
import com.stranger_sparks.data_model.StatusMessageDataResponse
import com.stranger_sparks.data_model.StatusMessageResponse
import com.stranger_sparks.data_model.SubscriptionPlansDTO
import com.stranger_sparks.data_model.SuggestionCityResponse
import com.stranger_sparks.data_model.UserProfileResponse
import com.stranger_sparks.data_model.WalletTransectionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Part
import javax.inject.Inject


class StrangerSparksRepository @Inject constructor(private val apiService: StrangerSparksApiInterface) {

    fun getStaticData(pageNumber: String): Call<GetAboutTermsPrivacyDTO> {
        return apiService.getStaticData(pageNumber)
    }

    fun getSubscriptionList(): Call<SubscriptionPlansDTO> {
        return apiService.getSubscriptionList()
    }

    fun getSubscriptionV2List(type: String): Call<SubscriptionPlansDTO> {
        return apiService.getSubscriptionV2List(type)
    }


    fun getUserLogin(phoneNumber: String): Call<StatusMessageResponse> {
        return apiService.getUserLogin(phoneNumber)
    }

    fun validateOtp(phone: String, enteredOtp: String, device_id: String): Call<LoginResponse> {
        return apiService.validateOtp(phone, enteredOtp, device_id)
    }

    fun updateProfile(
        full_name: RequestBody,
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
        image_outlet: MultipartBody.Part
    ): Call<LoginResponse> {
        return apiService.updateProfile(
            full_name,
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
            image_outlet
        )
    }



    fun updateProfilewithoutpic(
        full_name: RequestBody,
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
    ): Call<LoginResponse> {
        return apiService.updateProfilewithoutpic(
            full_name,
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
            languages
        )
    }

    fun galleryImage(
        user_id: RequestBody,
        image_outlet: Array<MultipartBody.Part>
    ): Call<StatusMessageResponse> {
        return apiService.galleryImage(user_id, image_outlet)
    }

    fun contactUs(
        name: String,
        email: String,
        phone: String,
        subject: String,
        message: String,
    ): Call<StatusMessageResponse> {
        return apiService.contactUs(name, email, phone, subject, message)
    }

    fun notificationList(
        user_id: String,per_page: String,page_number: Int
    ): Call<NotificationsResponse> {
        return apiService.notificationList(user_id, per_page, page_number)
    }

    fun addWallet(
        user_id: String,
        amount: String
    ): Call<StatusMessageResponse> {
        return apiService.addWallet(user_id, amount)
    }

    fun getWalletByUser(
        user_id: String
    ): Call<StatusMessageDataResponse> {
        return apiService.getWalletByUser(user_id)
    }

    fun walletTransactionsList(
        user_id: String,
        status_type: String,
    ): Call<WalletTransectionResponse> {
        return apiService.walletTransactionsList(user_id, status_type)
    }

    fun getGalleryProfile(
        user_id: String
    ): Call<GalleryImagesResponse> {
        return apiService.getGalleryProfile(user_id)
    }

    fun galleryImageDelete(
        id: String
    ): Call<StatusMessageResponse> {
        return apiService.galleryImageDelete(id)
    }

    fun citySuggestion(cityName: String): Call<SuggestionCityResponse> {
        return apiService.citySuggestion(cityName)
    }

    fun searchUserByLocation(cityName: String, user_id: String): Call<UserProfileResponse> {
        return apiService.searchUserByLocation(cityName, user_id)
    }
    fun searchUserByLocationHome(cityName: String, user_id: String): Call<UserProfileResponse> {
        return apiService.searchUserByLocation(cityName, user_id)
    }

    fun getCitysList(): Call<CitysListResponse> {
        return apiService.getCitysList()
    }

    fun getUserProfile(user_id: String, profile_id: String): Call<GetUserProfileResponse> {
        return apiService.getUserProfile(user_id, profile_id)
    }

    fun profileViewDetails(user_id: String, profile_id: String): Call<ProfileViewDetailsResponse> {
        return apiService.profileViewDetails(user_id, profile_id)
    }

    fun userstartcall(user_id: String, profile_id: String, type: String): Call<ProfileViewDetailsResponse> {
        return apiService.userstartcall(user_id, profile_id,type)
    }

    fun likedProfile(
        user_id: String,
        profile_id: String
    ): Call<StatusMessageResponse> {
        return apiService.likedProfile(user_id, profile_id)
    }

    fun manageSubscriptions(
        user_id: String,
    ): Call<ManageSubscriptionResponse> {
        return apiService.manageSubscriptions(user_id)
    }

    fun manage_subscriptions_v2(
        user_id: String, type: String,
    ): Call<ManageSubscriptionResponse> {
        return apiService.manage_subscriptions_v2(user_id,type)
    }

    fun addPayment(
        user_id: String,
        subscription_id: String,
    ): Call<LoginResponse> {
        return apiService.addPayment(user_id, subscription_id)
    }

    fun datingMatches(
        user_id: String,
        type: String
    ): Call<LikeLikedResponse> {
        return apiService.datingMatches(user_id, type)
    }

    fun viewProfile(
        user_id: String,
        profile_id: String
    ): Call<StatusMessageResponse> {
        return apiService.viewProfile(user_id, profile_id)
    }

    fun walletWithdrawal(
        user_id: String,
        amount: String,
        reason: String,
        account_number: String,
        confirm_account_number: String,
        ifsc_code: String,
        branch: String,
    ): Call<StatusMessageResponse> {
        return apiService.walletWithdrawal(
            user_id,
            amount,
            reason,
            account_number,
            confirm_account_number,
            ifsc_code,
            branch
        )
    }

    fun sendMessage(
        sender_id: String,
        receiver_id: String,
        message: String
    ): Call<StatusMessageResponse> {
        return apiService.sendMessage(sender_id, receiver_id, message)
    }


    fun searchChat( sender_id: String,
                    receiver_id: String,
                    message: String,
    ): Call<ChatResponse>{
        return apiService.searchChat(sender_id, receiver_id, message)
    }

    fun delete_only_for_you_post(@Field("sender_id") sender_id: String,
                   @Field("id") id: String
    ): Call<StatusMessageResponse>{
        return apiService.delete_only_for_you_post(sender_id, id)
    }

    fun delete_only_for_both_post(@Field("sender_id") sender_id: String,
                                 @Field("id") id: String
    ): Call<StatusMessageResponse>{
        return apiService.delete_only_for_both_post(sender_id, id)
    }

    fun getMessage( sender_id: String,
                    receiver_id: String
    ): Call<ChatResponse>{
        return apiService.getMessage(sender_id, receiver_id)
    }

    fun sendChartImage(
       sender_id: RequestBody,
        receiver_id: RequestBody,
       image: MultipartBody.Part
    ): Call<StatusMessageResponse> {
        return apiService.sendChartImage(
            sender_id, receiver_id, image
        )
    }

    fun logout(user_id: String
    ): Call<StatusMessageResponse>{
        return apiService.logout(user_id)
    }

    fun userAudioCall(user_id: String, profile_id: String, exitTime: String): Call<StatusMessageResponse> {
        return apiService.userAudioCall(user_id, profile_id, exitTime)
    }

    fun userVideoCall(user_id: String, profile_id: String, exitTime: String): Call<StatusMessageResponse> {
        return apiService.userVideoCall(user_id, profile_id, exitTime)
    }
}