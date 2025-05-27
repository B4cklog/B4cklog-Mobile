package com.rarmash.b4cklog.models

data class Game(
    val id: Int,
    var name: String,
    var summary: String,
    var cover: String,
    var releaseDate: String,
    var platforms: MutableList<Platform>
)