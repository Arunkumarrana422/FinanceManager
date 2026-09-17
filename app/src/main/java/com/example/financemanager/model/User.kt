package com.example.financemanager.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val currency: String = "INR",
    val createdAt: Long = System.currentTimeMillis()
)
