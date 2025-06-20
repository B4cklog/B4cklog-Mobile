package org.b4cklog.mobile.network

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

data class RefreshRequest(
    val refreshToken: String,
    val sessionId: String
)

data class LogoutRequest(
    val refreshToken: String,
    val sessionId: String
)

data class AuthResponse (
    val accessToken: String,
    val refreshToken: String,
    val sessionId: String
)