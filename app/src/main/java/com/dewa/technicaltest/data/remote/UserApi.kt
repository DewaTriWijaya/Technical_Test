package com.dewa.technicaltest.data.remote

import com.dewa.technicaltest.data.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): UserResponse
}