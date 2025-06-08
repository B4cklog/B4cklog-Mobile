package org.b4cklog.mobile.network

import org.b4cklog.mobile.models.Game
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GameApi {
    @GET("/games/get/all")
    fun getAllGames(): Call<List<Game>>

    @GET("/games/get/{id}")
    fun getGame(@Path("id") id: Int): Call<Game>

    @POST("/games/add")
    fun addGame(
        @Body game: Game
    ): Call<Game>

    @POST("/games/update")
    fun updateGame(
        @Body game: Game
    ): Call<Game>

    @GET("games/search")
    fun searchGames(@Query("q") query: String): Call<List<Game>>
}