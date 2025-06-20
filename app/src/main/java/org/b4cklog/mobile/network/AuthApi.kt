package org.b4cklog.mobile.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface AuthApi {
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("auth/refresh")
    fun refresh(@Body request: RefreshRequest): Call<AuthResponse>

    @POST("auth/logout")
    fun logout(@Body request: LogoutRequest): Call<Void>
}