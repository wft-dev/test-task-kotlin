package com.git.gitlist.ImgaeCache

import android.graphics.Bitmap
import androidx.collection.LruCache
import kotlinx.coroutines.Deferred

class MyCache private constructor() {

    private object HOLDER {
        val INSTANCE = MyCache()
    }

    companion object {
        val instance: MyCache by lazy { HOLDER.INSTANCE }
    }
    val lru: LruCache<Any, Any>

    init {

        lru = LruCache(1024)

    }

    fun saveBitmapToCahche(key: String, bitmap: Bitmap) {

        try {
            MyCache.instance.lru.put(key, bitmap)
        } catch (e: Exception) {
        }

    }

    fun retrieveBitmapFromCache(key: String): Bitmap {

        return MyCache.instance.lru.get(key) as Bitmap

    }

}