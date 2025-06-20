package org.b4cklog.mobile.network

import android.content.Context
import org.b4cklog.mobile.util.AuthPrefs
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        var request = chain.request()
        val accessToken = AuthPrefs.getAccessToken(appContext)
        if (!accessToken.isNullOrEmpty()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }
        var response = chain.proceed(request)
        if (response.code == 401) {
            response.close()
            val refreshToken = AuthPrefs.getRefreshToken(appContext)
            if (!refreshToken.isNullOrEmpty()) {
                try {
                    val refreshResponse = authApi.refresh(RefreshRequest(refreshToken)).execute()
                    if (refreshResponse.isSuccessful) {
                        val newAccessToken = refreshResponse.body()?.accessToken
                        val newRefreshToken = refreshResponse.body()?.refreshToken
                        if (!newAccessToken.isNullOrEmpty() && !newRefreshToken.isNullOrEmpty()) {
                            AuthPrefs.saveTokens(appContext, newAccessToken, newRefreshToken)
                            val newRequest = request.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", "Bearer $newAccessToken")
                                .build()
                            return@Interceptor chain.proceed(newRequest)
                        }
                    }
                } catch (_: Exception) {}
            }
            AuthPrefs.clearTokens(appContext)
        }
        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)
    val gameApi: GameApi = retrofit.create(GameApi::class.java)
    val platformApi: PlatformApi = retrofit.create(PlatformApi::class.java)
    val reviewApi: ReviewApi = retrofit.create(ReviewApi::class.java)
}