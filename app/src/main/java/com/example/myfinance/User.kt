package com.example.myfinance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val login: String,
    val email: String,
    val password: String
)

@Entity("goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val initialCapital: Int,
    val targetAmount: Int
)