package org.b4cklog.mobile.network

import org.b4cklog.mobile.models.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApi {
    @GET("users/profile")
    fun getUserProfile(): Call<User>

    @POST("users/{userId}/addGameToList")
    fun addGameToList(
        @Path("userId") userId: Int,
        @Query("gameId") gameId: Int,
        @Query("listName") listName: String
    ): Call<Void>

    @DELETE("/users/{userId}/removeGameFromAllLists")
    fun removeGameFromAllLists(
        @Path("userId") userId: Int,
        @Query("gameId") gameId: Int
    ): Call<Void>

    @PATCH("users/updateEmail")
    suspend fun updateEmail(@Query("newEmail") newEmail: String): Response<Unit>

    @PATCH("users/updatePassword")
    suspend fun updatePassword(@Query("newPassword") newPassword: String): Response<Unit>
}