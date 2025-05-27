package com.rarmash.b4cklog.network

import com.rarmash.b4cklog.models.User
import retrofit2.Call
import retrofit2.http.GET

interface ProfileApi {
    @GET("users/profile")
    fun getUserProfile(): Call<User>
}