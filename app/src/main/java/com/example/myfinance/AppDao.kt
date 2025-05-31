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
}

@Database(entities = [User::class, Goal::class], version = 1)
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

