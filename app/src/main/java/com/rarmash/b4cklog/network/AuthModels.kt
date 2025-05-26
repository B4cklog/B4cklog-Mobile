package com.rarmash.b4cklog.network

data class LoginRequest (
    val username: String,
    val password: String
)

data class RegisterRequest (
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val age: Int
)

data class AuthResponse (
    val token: String
)