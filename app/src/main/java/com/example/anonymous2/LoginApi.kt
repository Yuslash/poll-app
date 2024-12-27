package com.example.anonymous2

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}