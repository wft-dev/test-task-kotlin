package com.git.gitlist.Interface

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.git.gitlist.Modal.GitUser

@Dao
interface GitUserDao {
    @Query("SELECT * FROM gituser")
    fun getAll(): List<GitUser>

    @Query("SELECT * FROM gituser where id = :ids")
    fun getSingleData(ids: Int): GitUser


    @Insert
    fun insertSingleData(todo: GitUser)

    @Update
    fun UpdateSingleData(todo: GitUser)

    @Insert
    fun insertAllData(todo: List<GitUser>)

}