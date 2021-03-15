package com.git.gitlist.Interface

import com.git.gitlist.Modal.GitUser
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitEndPoints{

    @GET("/users")
    fun getGitList(@Query("since") since: Int): Call<List<GitUser>>

    @GET("/users/{username}")
    fun getUserDetails(@Path("username") username: String): Call<GitUser>

}