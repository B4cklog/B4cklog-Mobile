package com.rarmash.b4cklog.models

import com.google.gson.annotations.SerializedName

data class Game(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("released") val releaseDate: String?,
    @SerializedName("background_image") val imageUrl: String?
)