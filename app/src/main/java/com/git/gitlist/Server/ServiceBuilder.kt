package com.git.gitlist.Server

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceBuilder {

    // Initialize the okhttp client
    var okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()

    // initialize the retrofit for making server request
    var retrofit: Retrofit? = null
    fun getClient(): Retrofit? {
        if (retrofit == null) {
            val gson = GsonBuilder().setLenient().create()
            retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/").addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build()
        }
        return retrofit
    }
}