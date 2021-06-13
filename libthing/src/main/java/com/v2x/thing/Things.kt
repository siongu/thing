package com.v2x.thing

import android.content.Context
import com.common.stdlib.storage.KV

object Things {
    fun init(context: Context) {
        KV.initMMKV(context.applicationContext)
    }
}