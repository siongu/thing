package com.common.stdlib.storage

import android.content.Context
import com.tencent.mmkv.MMKV

object KV {
    private val kv by lazy { MMKV.defaultMMKV()!! }
    internal fun initMMKV(context: Context) {
        val rootDir = MMKV.initialize(context)
        println("mmkv root: $rootDir")
    }

    fun encode(key: String, value: String?) {
        kv.encode(key, value)
    }

    fun decodeString(key: String, value: String? = null): String? {
        return kv.decodeString(key, value)
    }
}



