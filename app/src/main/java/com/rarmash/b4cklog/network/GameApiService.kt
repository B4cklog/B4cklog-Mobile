package com.rarmash.b4cklog.network

import com.rarmash.b4cklog.models.GameResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GameApiService {
    @GET("games")
    fun getGames(
        @Query("search") query: String,
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 20
    ): Call<GameResponse>
}