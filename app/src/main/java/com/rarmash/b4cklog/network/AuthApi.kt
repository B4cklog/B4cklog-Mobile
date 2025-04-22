package com.rarmash.b4cklog.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call


interface AuthApi {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>
}