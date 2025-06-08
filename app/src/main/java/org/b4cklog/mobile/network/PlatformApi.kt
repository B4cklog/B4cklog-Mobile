package org.b4cklog.mobile.network

import org.b4cklog.mobile.models.Platform
import org.b4cklog.mobile.models.PlatformRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlatformApi {
    @GET("/platforms/get/all")
    fun getAllPlatforms(): Call<List<Platform>>

    @GET("/platforms/get/{id}")
    fun getPlatform(@Path("id") id: Int): Call<Platform>

    @POST("/platforms/add")
    fun addPlatform(
        @Body platform: PlatformRequest
    ): Call<Platform>

    @DELETE("/platforms/delete/{id}")
    fun deletePlatform(
        @Path("id") id: Int
    ): Call<Void>
}
