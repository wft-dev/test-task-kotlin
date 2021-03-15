package com.git.gitlist.Database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.git.gitlist.Interface.GitUserDao
import com.git.gitlist.Modal.GitUser

@Database(
        entities = arrayOf(GitUser::class),
        version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun GitUserDao(): GitUserDao
    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
                AppDatabase::class.java, "git-list.db")
                .build()
    }

}