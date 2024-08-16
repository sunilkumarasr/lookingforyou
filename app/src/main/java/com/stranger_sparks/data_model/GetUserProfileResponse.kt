package com.stranger_sparks.data_model

data class GetUserProfileResponse(
    val `data`: Data,
    val message: String,
    val status: Boolean,
    val profile_view_status: Boolean
) {
    data class Data(
        val age: String,
        val description: String,
        val email: String,
        val gender: String,
        val height: String,
        val hobbies: String,
        val id: String,
        val liked_count: Int,
        val liked_status: Boolean,
        val location: String,
        val name: String,
        val phone: String,
        val profile_completed: String,
        val profile_image: ProfileImage,
        val profile_pic: String,
        val alternative_phone: String,
        val is_chart: Boolean,
        val subscription_value: String,
        val subscription_profile: Boolean,
        val subscription_audio: Boolean,
        val subscription_audio_time: String,
        val subscription_video: Boolean,
        val subscription_video_time: String
    ) {
        data class ProfileImage(
            val imageDate: List<String>,
            val image_count: Int
        )
    }
}