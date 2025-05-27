package com.rarmash.b4cklog.models

data class User(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val age: String,
    val isAdmin: Boolean,
    val backlogWantToPlay: List<Game>,
    val backlogPlaying: List<Game>,
    val backlogPlayed: List<Game>,
    val backlogCompleted: List<Game>,
    val backlogCompleted100: List<Game>
)