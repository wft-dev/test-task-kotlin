package com.git.gitlist.Modal

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

// create table in android room database
@Entity(tableName = "GitUser")
data class GitUser(
        @PrimaryKey()
        var id: Int,
        @ColumnInfo(name = "login") var login: String? = "" ,
        @ColumnInfo(name = "avatar_url") var avatar_url: String? = "",
        @ColumnInfo(name = "avatar_url_bitmap") var avatar_url_bitmap: String? = "",
        @ColumnInfo(name = "type") var type: String? = "",
        @ColumnInfo(name = "name") var name: String? = "",
        @ColumnInfo(name = "company") var company: String? = "",
        @ColumnInfo(name = "followers") var followers: String? = "",
        @ColumnInfo(name = "following") var following: String? = "",
        @ColumnInfo(name = "blog") var blog: String? = "",
        @ColumnInfo(name = "notes") var notes: String? = ""
)