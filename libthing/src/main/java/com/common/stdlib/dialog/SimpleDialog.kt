package com.common.stdlib.dialog

import android.app.Dialog
import android.content.Context
import com.v2x.thing.R

class SimpleDialog(context: Context) : Dialog(context) {
    init {
        window?.run {
            setBackgroundDrawableResource(R.drawable.transparent)
        }
    }
}