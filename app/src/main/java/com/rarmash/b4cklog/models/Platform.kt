package com.rarmash.b4cklog.models

data class Platform (
    val id: Int,
    var name: String,
    var releaseDate: String
)

data class PlatformRequest(
    var name: String,
    var releaseDate: String
)