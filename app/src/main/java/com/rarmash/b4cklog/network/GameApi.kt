package com.rarmash.b4cklog.network

import com.rarmash.b4cklog.models.Game
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GameApi {
    @GET("/games/get/all")
    fun getAllGames(): Call<List<Game>>

    @GET("/games/get/{id}")
    fun getGame(@Path("id") id: Int): Call<Game>
}