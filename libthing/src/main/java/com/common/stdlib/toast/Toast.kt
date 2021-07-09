package com.common.stdlib.toast

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.v2x.thing.R
import org.jetbrains.anko.toast

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT, customView: ViewGroup?) {
    val toast = Toast(this)
    toast.duration = duration
    if (customView != null) {
        try {
            (customView.getChildAt(0) as TextView).text = msg
        } catch (e: Exception) {
            throw IllegalArgumentException("the first child of customView must be a type of TextView")
        }
        toast.view = customView
    } else {
        toast.setText(msg)
    }
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT, resId: Int) {
    val toast = Toast(this)
    toast.duration = duration
    val view = LayoutInflater.from(this).inflate(resId, null)
    if (view != null) {
        println("width=${view.width},height=${view.height}")
        try {
            view.findViewById<TextView>(R.id.toast).text = msg
        } catch (e: Exception) {
            throw IllegalArgumentException("the view must have a child with type of TextView and id 'toast'")
        }
        toast.view = view
    } else {
        toast.setText(msg)
    }
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}

fun Context.toast(duration: Int = Toast.LENGTH_SHORT, resId: Int) {
    val toast = Toast(this)
    toast.duration = duration
    val view = LayoutInflater.from(this).inflate(resId, null)
    if (view != null) {
        println("width=${view.width},height=${view.height}")
        toast.view = view
    } else {
        throw IllegalArgumentException("the resource with id='$resId' not found.")
    }
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}

fun Context.toast(msg: CharSequence) {
    toast(msg)
}