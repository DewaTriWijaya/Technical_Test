package com.dewa.technicaltest.utils

import android.app.Activity
import com.dewa.technicaltest.data.model.User
import kotlin.collections.filter

fun getActiveAdultUserNames(
    users: List<User>
): List<String> {
    return users
        .filter { it.isActive && it.age >= 18 }
        .sortedBy { it.name }
        .map { it.name }
}

fun searchUsers(
    users: List<User>,
    keyword: String
): List<User> {
    return users
        .filter { keyword.isBlank() || it.name.contains(keyword, ignoreCase = true) }
        .sortedBy { it.name }
}



