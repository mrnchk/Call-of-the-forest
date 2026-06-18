package com.cotf.network

import com.cotf.session.UserSession
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton для создания Retrofit-клиента.
 * Base URL: http://10.0.2.2:8080 — стандартный адрес хост-машины из Android-эмулятора.
 * Для реального устройства заменить на IP сервера.
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private var retrofit: Retrofit? = null

    fun <T> create(service: Class<T>, userSession: UserSession): T {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    val loggingInterceptor = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }

                    val okHttpClient = OkHttpClient.Builder()
                        .addInterceptor(AuthInterceptor(userSession))
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()

                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
        }
        return retrofit!!.create(service)
    }
}
