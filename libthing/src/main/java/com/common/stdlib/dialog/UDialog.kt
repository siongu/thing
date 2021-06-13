package com.common.stdlib.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity

class UDialog(context: Context?) : AlertDialog(context) {

    fun setNegativeButton(text: CharSequence, onClickListener: DialogInterface.OnClickListener): UDialog {
        this.setButton(BUTTON_NEGATIVE, text, onClickListener)
        return this
    }

    fun setPositiveButton(text: CharSequence, onClickListener: DialogInterface.OnClickListener): UDialog {
        this.setButton(DialogInterface.BUTTON_POSITIVE, text, onClickListener)
        return this
    }

    fun setNeutralButton(text: CharSequence, onClickListener: DialogInterface.OnClickListener): UDialog {
        this.setButton(DialogInterface.BUTTON_NEUTRAL, text, onClickListener)
        return this
    }

    fun setContent(content: CharSequence): UDialog {
        setMessage(content)
        return this
    }

    fun setTitle(title: CharSequence): UDialog {
        super.setTitle(title)
        return this
    }

    fun enableCancel(cancelable: Boolean): UDialog {
        this.setCancelable(cancelable)
        return this
    }

    override fun show() {
        super.show()
        window?.run {
            attributes.width = (windowManager.defaultDisplay.width * 0.95).toInt()
            attributes.gravity = Gravity.CENTER
            this.attributes = attributes
        }
    }
}