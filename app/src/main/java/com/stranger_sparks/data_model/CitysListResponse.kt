package com.stranger_sparks.data_model

data class CitysListResponse(
    val data: List<Data>,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val id: String,
        val name: String,
    )
}
