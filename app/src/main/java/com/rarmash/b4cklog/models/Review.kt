package com.rarmash.b4cklog.models

data class ReviewRequest(
    val userId: Int,
    val gameId: Int,
    val rating: Int,
    val comment: String?
)

data class ReviewResponse(
    val rating: Int,
    val comment: String?
)