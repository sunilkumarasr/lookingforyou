package com.stranger_sparks.data_model

data class LoginResponse(
    var data: Data,
    var message: String,
    var status: Boolean
) {
    data class Data(
        var age: String,
        var created_at: String,
        var description: String,
        var device_id: String,
        var email: String,
        var gender: String,
        var marital: String,
        var languages: String,
        var height: String,
        var hobbies: String,
        var id: String,
        var image: String,
        var is_subscription: String,
        var location: String,
        var name: String,
        var otp: String,
        var phone: String,
        var profile_completed: String,
        var profile_count: String,
        var status: String,
        var updated_at: String,
        var user_id: String,
        var wallet: String,
        var alternative_phone: String,
        var is_online: Int
    )
}