package com.common.stdlib.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.util.*

fun randomUid(): String {
    return UUID.randomUUID().toString()
}

fun putTextIntoClip(context: Context, text: String?) {
    val clipboardManager: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    //创建ClipData对象
    val clipData = ClipData.newPlainText("HSFAppDemoClip", text)
    //添加ClipData对象到剪切板中
    clipboardManager.setPrimaryClip(clipData)
}

fun getTextFromClip(context: Context): String {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    //判断剪切版时候有内容
    if (!clipboardManager.hasPrimaryClip()) return ""
    val clipData = clipboardManager.primaryClip
    //获取 ClipDescription
    val clipDescription = clipboardManager.primaryClipDescription
    //获取 lable
    val lable = clipDescription!!.label.toString()
    //获取 text
    val text = clipData!!.getItemAt(0).text.toString()
    return text
}