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
    val userId: Int,  // Добавляем связь с пользователем
    val title: String,
    val initialCapital: Int,
    val targetAmount: Int
)

@Entity("expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,  // Добавляем связь с пользователем
    val category: String,
    val name: String,
    val amount: Int,
    val date: String
)

@Entity("incomes")
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,  // Добавляем связь с пользователем
    val category: String,
    val name: String,
    val amount: Int,
    val date: String
)

enum class CATEGORY : java.io.Serializable{
    FOOD, MEDICINE, RELAX
}


enum class CATEGORYS : java.io.Serializable{
    GIFT, WORK, WIN
}