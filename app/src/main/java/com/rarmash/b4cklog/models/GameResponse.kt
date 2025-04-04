package com.rarmash.b4cklog.models

import com.google.gson.annotations.SerializedName

data class GameResponse(
    @SerializedName("results") val games: List<Game>
)