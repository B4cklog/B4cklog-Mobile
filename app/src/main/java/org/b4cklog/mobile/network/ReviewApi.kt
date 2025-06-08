package org.b4cklog.mobile.network

import org.b4cklog.mobile.models.ReviewRequest
import org.b4cklog.mobile.models.ReviewResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewApi {
    @GET("/reviews/game/{gameId}/average")
    fun getAverageRating(@Path("gameId") gameId: Int): Call<Double>

    @POST("/reviews/add")
    fun submitReview(@Body reviewRequest: ReviewRequest): Call<Void>

    @GET("/reviews/user/{userId}/game/{gameId}")
    fun getUserReview(
        @Path("userId") userId: Int,
        @Path("gameId") gameId: Int
    ): Call<ReviewResponse>

}
