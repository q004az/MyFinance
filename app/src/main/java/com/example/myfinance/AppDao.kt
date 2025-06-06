package com.example.myfinance

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase


@Dao
interface AppDao {
    // User operations
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    suspend fun getUserByLogin(login: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE login = :login AND password = :password)")
    suspend fun isUserExists(login: String, password: String): Boolean

    // Goal operations
    @Insert
    suspend fun insert(goal: Goal)

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun getLastGoal(userId: Int): Goal?

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllGoals(userId: Int)

    // Expense operations
    @Insert
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    suspend fun getAllExpenses(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND id = :id")
    suspend fun getExpenseById(id: Long, userId: Int): Expense?

    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = 'Еда'")
    suspend fun getFoodExpenses(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = 'Мед'")
    suspend fun getMedicineExpenses(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = 'Отдых'")
    suspend fun getRelaxExpenses(userId: Int): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND category = 'Еда'")
    suspend fun getFoodExpensesSum(userId: Int): Int?

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND category = 'Мед'")
    suspend fun getMedicineExpensesSum(userId: Int): Int?

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND category = 'Отдых'")
    suspend fun getRelaxExpensesSum(userId: Int): Int?

    @Query("DELETE FROM expenses WHERE userId = :userId AND id = :foodId")
    suspend fun deleteById(foodId: Int, userId: Int)

    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun deleteAllExpenses(userId: Int)

    // Income operations
    @Insert
    suspend fun insert(income: Income)

    @Query("SELECT * FROM incomes WHERE userId = :userId")
    suspend fun getAllIncome(userId: Int): List<Income>

    @Query("SELECT * FROM incomes WHERE userId = :userId AND id = :id")
    suspend fun getIncomeById(id: Long, userId: Int): Income?

    @Query("DELETE FROM incomes WHERE userId = :userId AND id = :incomeId")
    suspend fun deleteByIdIncome(incomeId: Int, userId: Int)

    @Query("SELECT * FROM incomes WHERE userId = :userId AND category = 'Гифт'")
    suspend fun getGiftIncome(userId: Int): List<Income>

    @Query("SELECT * FROM incomes WHERE userId = :userId AND category = 'Работа'")
    suspend fun getWorkIncome(userId: Int): List<Income>

    @Query("SELECT * FROM incomes WHERE userId = :userId AND category = 'Лотерея'")
    suspend fun getWinIncome(userId: Int): List<Income>

    @Query("SELECT SUM(amount) FROM incomes WHERE userId = :userId AND category = 'Гифт'")
    suspend fun getGiftIncomeSum(userId: Int): Int?

    @Query("SELECT SUM(amount) FROM incomes WHERE userId = :userId AND category = 'Работа'")
    suspend fun getWorkIncomeSum(userId: Int): Int?

    @Query("SELECT SUM(amount) FROM incomes WHERE userId = :userId AND category = 'Лотерея'")
    suspend fun getWinIncomeSum(userId: Int): Int?

    @Query("DELETE FROM incomes WHERE userId = :userId")
    suspend fun deleteAllIncomes(userId: Int)
}

@Database(entities = [User::class, Goal::class, Expense::class, Income::class], version = 1)
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



