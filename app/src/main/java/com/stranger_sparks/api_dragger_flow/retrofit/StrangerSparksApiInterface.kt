package com.stranger_sparks.api_dragger_flow.retrofit

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
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface StrangerSparksApiInterface {
    @GET("page/{pageNumber}")
    fun getStaticData(@Path("pageNumber") pageNumber: String): Call<GetAboutTermsPrivacyDTO>

    @GET("subscription_list")
    fun getSubscriptionList(): Call<SubscriptionPlansDTO>


    @FormUrlEncoded
    @POST("subscription_list_v2")
    fun getSubscriptionV2List(@Field("type") phone: String): Call<SubscriptionPlansDTO>


    @FormUrlEncoded
    @POST("login")
    fun getUserLogin(@Field("phone") phone: String): Call<StatusMessageResponse>

    @FormUrlEncoded
    @POST("otp_verify")
    fun validateOtp(
        @Field("phone") phone: String,
        @Field("otp") otp: String,
        @Field("device_id") device_id: String,
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("add_wallet")
    fun addWallet(
        @Field("user_id") user_id: String,
        @Field("amount") amount: String
    ): Call<StatusMessageResponse>

    @FormUrlEncoded
    @POST("get_wallet_by_user")
    fun getWalletByUser(
        @Field("user_id") user_id: String
    ): Call<StatusMessageDataResponse>

    @FormUrlEncoded
    @POST("wallet_transactions_list")
    fun walletTransactionsList(
        @Field("user_id") user_id: String,
        @Field("status_type") status_type: String,
    ): Call<WalletTransectionResponse>

    @FormUrlEncoded
    @POST("get_gallery_profile")
    fun getGalleryProfile(
        @Field("user_id") user_id: String
    ): Call<GalleryImagesResponse>

    @FormUrlEncoded
    @POST("gallery_image_delete")
    fun galleryImageDelete(
        @Field("id") id: String
    ): Call<StatusMessageResponse>

    @Multipart
    @POST("update_profile")
    fun updateProfile(
        @Part("full_name") full_name: RequestBody,
        @Part("age") age: RequestBody,
        @Part("location") location: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("height") height: RequestBody,
        @Part("description") description: RequestBody,
        @Part("user_id") user_id: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("hobbies") hobbies: RequestBody,
        @Part("alternative_phone") alterNativeNumber: RequestBody,
        @Part("marital") marital: RequestBody,
        @Part("languages") languages: RequestBody,
        @Part image_outlet: MultipartBody.Part
    ): Call<LoginResponse>

    @Multipart
    @POST("update_profile")
    fun updateProfilewithoutpic(
        @Part("full_name") full_name: RequestBody,
        @Part("age") age: RequestBody,
        @Part("location") location: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("height") height: RequestBody,
        @Part("description") description: RequestBody,
        @Part("user_id") user_id: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("hobbies") hobbies: RequestBody,
        @Part("alternative_phone") alterNativeNumber: RequestBody,
        @Part("marital") marital: RequestBody,
        @Part("languages") languages: RequestBody,
    ): Call<LoginResponse>


    @Multipart
    @POST("gallery_image")
    fun galleryImage(
        @Part("user_id") user_id: RequestBody,
        @Part image_outlet: Array<MultipartBody.Part>
    ): Call<StatusMessageResponse>


    @FormUrlEncoded
    @POST("contact_us")
    fun contactUs(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("subject") subject: String,
        @Field("message") message: String,
    ): Call<StatusMessageResponse>

    @FormUrlEncoded
    @POST("notification_list_v2")
    fun notificationList(
        @Field("user_id") user_id: String, @Field("per_page") per_page: String, @Field("page_number") page_number: Int
    ): Call<NotificationsResponse>

    @FormUrlEncoded
    @POST("city_suggestion")
    fun citySuggestion(@Field("location") cityName: String): Call<SuggestionCityResponse>

    @FormUrlEncoded
    @POST("search_user_by_location")
    fun searchUserByLocation(@Field("location") cityName: String,
                             @Field("user_id") user_id: String): Call<UserProfileResponse>

    @GET("city")
    fun getCitysList(): Call<CitysListResponse>


    @FormUrlEncoded
    @POST("get_user_profile_v2")
    fun getUserProfile(@Field("user_id") user_id: String,
        @Field("profile_id") profile_id: String): Call<GetUserProfileResponse>


    @FormUrlEncoded
    @POST("profile_view_details_v2")
    fun profileViewDetails(@Field("user_id") user_id: String,
                       @Field("profile_id") profile_id: String): Call<ProfileViewDetailsResponse>

    @FormUrlEncoded
    @POST("user_start_call")
    fun userstartcall(@Field("user_id") user_id: String,
                           @Field("profile_id") profile_id: String,
                                        @Field("type") type: String): Call<ProfileViewDetailsResponse>

    @FormUrlEncoded
    @POST("liked_profile")
    fun likedProfile(@Field("user_id") user_id: String,
                     @Field("profile_id") profile_id: String): Call<StatusMessageResponse>

    @FormUrlEncoded
    @POST("manage_subscriptions")
    fun manageSubscriptions(@Field("user_id") user_id: String,
                     ): Call<ManageSubscriptionResponse>

    @FormUrlEncoded
    @POST("manage_subscriptions_v2")
    fun manage_subscriptions_v2(@Field("user_id") user_id: String,@Field("type") type: String,
    ): Call<ManageSubscriptionResponse>

    @FormUrlEncoded
    @POST("add_payment")
    fun addPayment(@Field("user_id") user_id: String,
                   @Field("subscription_id") subscription_id: String): Call<LoginResponse>

    @FormUrlEncoded
    @POST("dating_matches")
    fun datingMatches(@Field("user_id") user_id: String,
                   @Field("type") type: String): Call<LikeLikedResponse>
    @FormUrlEncoded
    @POST("view_profile")
    fun viewProfile(@Field("user_id") user_id: String,
                   @Field("profile_id") profile_id: String): Call<StatusMessageResponse>

    @FormUrlEncoded
    @POST("wallet_withdrawal")
    fun walletWithdrawal(@Field("user_id") user_id: String,
                   @Field("amount") amount: String,
                   @Field("reason") reason: String,
                   @Field("account_number") account_number: String,
                   @Field("confirm_account_number") confirm_account_number: String,
                   @Field("ifsc_code") ifsc_code: String,
                         @Field("branch") branch: String,
                         ): Call<StatusMessageResponse>
    @FormUrlEncoded
    @POST("send_message")
    fun sendMessage(@Field("sender_id") sender_id: String,
                   @Field("receiver_id") receiver_id: String,
                   @Field("message") message: String
                         ): Call<StatusMessageResponse>

    @FormUrlEncoded
    @POST("search_chat")
    fun searchChat(@Field("sender_id") sender_id: String,
                   @Field("receiver_id") receiver_id: String,
                   @Field("message") message: String,
                         ): Call<ChatResponse>

    @FormUrlEncoded
    @POST("delete_only_for_you")
    fun delete_only_for_you_post(@Field("sender_id") sender_id: String,
                   @Field("id") id: String
                         ): Call<StatusMessageResponse>


    @FormUrlEncoded
    @POST("delete_only_for_both")
    fun delete_only_for_both_post(@Field("sender_id") sender_id: String,
                                 @Field("id") id: String
    ): Call<StatusMessageResponse>

    @FormUrlEncoded
    @POST("get_message")
    fun getMessage(@Field("sender_id") sender_id: String,
                   @Field("receiver_id") receiver_id: String
                         ): Call<ChatResponse>


    @FormUrlEncoded
    @POST("logout")
    fun logout(@Field("user_id") user_id: String
                         ): Call<StatusMessageResponse>


    @Multipart
    @POST("send_chart_image")
    fun sendChartImage(
        @Part("sender_id") sender_id: RequestBody,
        @Part("receiver_id") receiver_id: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<StatusMessageResponse>


    @FormUrlEncoded
    @POST("user_audio_call")
    fun userAudioCall(@Field("user_id") user_id: String,
                       @Field("profile_id") profile_id: String,
                       @Field("end_time") exitTime: String): Call<StatusMessageResponse>

    @FormUrlEncoded
    @POST("user_video_call")
    fun userVideoCall(@Field("user_id") user_id: String,
                       @Field("profile_id") profile_id: String,
                       @Field("end_time") exitTime: String): Call<StatusMessageResponse>


}