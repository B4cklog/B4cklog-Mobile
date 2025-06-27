package org.b4cklog.mobile.models

import com.google.gson.annotations.SerializedName

data class Game(
    val id: Int,
    val name: String,
    val summary: String? = null,
    val cover: Cover? = null,
    @SerializedName("first_release_date")
    val firstReleaseDate: Long? = null,
    val platforms: List<Platform>? = null,
    val genres: List<Genre>? = null,
    val screenshots: List<Screenshot>? = null
) {
    fun getCoverUrl(): String {
        return cover?.getCoverUrl() ?: ""
    }
    
    fun getReleaseDate(): String {
        return firstReleaseDate?.let { timestamp ->
            val date = java.time.Instant.ofEpochSecond(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        } ?: ""
    }
}

data class Cover(
    val id: Int,
    val url: String
) {
    fun getCoverUrl(): String {
        val processedUrl = url.replace("t_thumb", "t_cover_big")
        return if (processedUrl.startsWith("//")) {
            "https:$processedUrl"
        } else {
            processedUrl
        }
    }
}

data class Genre(
    val id: Int,
    val name: String
)

data class Screenshot(
    val id: Int,
    val url: String
)