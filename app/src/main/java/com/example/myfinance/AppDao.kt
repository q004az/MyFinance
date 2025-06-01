package com.example.myfinance

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
@Dao
interface AppDao  {
    @Insert
    suspend fun insert(user: User)

    @Insert
    suspend fun insert(goal: Goal)

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE login = :login AND password = :password)")
    suspend fun isUserExists(login: String, password: String): Boolean

    @Query("SELECT * FROM goals ORDER BY id DESC LIMIT 1")
    suspend fun getLastGoal(): Goal?

    @Insert
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE category = 'Еда'")
    suspend fun getFoodExpenses(): List<Expense>

    @Query("DELETE FROM expenses WHERE id = :foodId")
    suspend fun deleteById(foodId: Int)

    @Query("SELECT * FROM expenses WHERE category = 'Мед'")
    suspend fun getMedicineExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE category = 'Отдых'")
    suspend fun getRelaxExpenses(): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE category = 'Еда'")
    suspend fun getFoodExpensesSum(): Int?

    @Query("SELECT SUM(amount) FROM expenses WHERE category = 'Мед'")
    suspend fun getMedicineExpensesSum(): Int?

    @Query("SELECT SUM(amount) FROM expenses WHERE category = 'Отдых'")
    suspend fun getRelaxExpensesSum(): Int?

}

@Database(entities = [User::class, Goal::class, Expense::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): AppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(

                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

