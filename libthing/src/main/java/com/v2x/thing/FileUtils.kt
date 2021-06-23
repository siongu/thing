package com.v2x.thing

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter

object FileUtils {

    fun saveToFile(context: Context, filename: String, data: String) {
        if (filename.isNullOrBlank()) {
            println("请输入要保存的文件名")
            return
        }
        val fileDir = getFilesDir(context)
        val path = fileDir + File.separator + filename + ".txt"
        println("path:$path")
        var writer: FileWriter? = null
        try {
            val file = File(path)
            if (!file.exists()) {
                file.createNewFile()
            }
            writer = FileWriter(file, true)
            writer.write(data + "\r\n")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            writer?.close()
        }
    }

    private fun getFilesDir(context: Context): String? {
        val filesDir: String =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !Environment.isExternalStorageRemovable()
            ) {
                //外部存储可用
                val file = context.getExternalFilesDir(null)
                if (file == null) {
                    context.filesDir.path
                } else {
                    file.path
                }
            } else {
                //外部存储不可用
                context.filesDir.path
            }
        println("filesDir:$filesDir")
        return filesDir
    }
}