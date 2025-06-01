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

@Entity("expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val name: String,
    val amount: Int,
    val date: String
)

enum class CATEGORY : java.io.Serializable{
    FOOD, MEDICINE, RELAX
}