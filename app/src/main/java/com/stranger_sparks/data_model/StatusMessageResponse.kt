package com.stranger_sparks.data_model

data class StatusMessageResponse(
    val message: String,
    val status: Boolean,
    val otp: Int
)