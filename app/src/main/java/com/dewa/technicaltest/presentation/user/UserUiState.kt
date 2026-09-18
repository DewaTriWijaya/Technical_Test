package com.dewa.technicaltest.presentation.user

import com.dewa.technicaltest.data.model.User

sealed interface UserUiState {

    data object Loading : UserUiState

    data class Success(
        val users: List<User>
    ) : UserUiState

    data class Error(
        val message: String
    ) : UserUiState
}