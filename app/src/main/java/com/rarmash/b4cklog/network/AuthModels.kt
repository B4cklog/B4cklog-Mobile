package com.rarmash.b4cklog.network

data class LoginRequest (
    val login: String,
    val password: String
)

data class RegisterRequest (
    val login: String,
    val password: String,
    val email: String
)

data class AuthResponse (
    val token: String
)