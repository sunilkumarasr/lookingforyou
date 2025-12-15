package com.stranger_sparks.data_model

data class ProfileViewDetailsResponse(
    val message: String,
    val status: Boolean,
    // Add calltype here:
    val calltype: String? = null
)
