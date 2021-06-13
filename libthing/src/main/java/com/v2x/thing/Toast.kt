package com.v2x.thing

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(this@toast, msg, duration).show()
    }
}