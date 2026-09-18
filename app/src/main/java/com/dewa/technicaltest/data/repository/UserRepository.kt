package com.dewa.technicaltest.data.repository

import com.dewa.technicaltest.data.model.User
import com.dewa.technicaltest.data.model.UserResponse
import com.dewa.technicaltest.data.remote.UserApi

class UserRepository(
    private val api: UserApi
) {
    suspend fun getUserById(id: Int): UserResponse {
        return api.getUserById(id)
    }

    suspend fun getUsers(): List<User> {
        return listOf(
            User(
                id = 1,
                name = "Andi",
                age = 25,
                isActive = true
            ),
            User(
                id = 2,
                name = "Budi",
                age = 17,
                isActive = true
            ),
            User(
                id = 3,
                name = "Citra",
                age = 30,
                isActive = false
            ),
            User(
                id = 4,
                name = "Deni",
                age = 22,
                isActive = true
            )
        )
    }
}