package com.common.stdlib.system

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.view.WindowManager

/**
 * convert px to its equivalent dp
 *
 *
 * 将px转换为与之相等的dp
 */
fun px2dp(context: Context, pxValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

/**
 * convert dp to its equivalent px
 *
 *
 * 将dp转换为与之相等的px
 */
fun dp2px(context: Context, dipValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dipValue * scale + 0.5f).toInt()
}

/**
 * convert px to its equivalent sp
 *
 *
 * 将px转换为sp
 */
fun px2sp(context: Context, pxValue: Float): Int {
    val fontScale = context.resources.displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}

/**
 * convert sp to its equivalent px
 *
 *
 * 将sp转换为px
 */
fun sp2px(context: Context, spValue: Float): Int {
    val fontScale = context.resources.displayMetrics.scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}

/**
 * 获取是否存在NavigationBar
 *
 * @param context
 * @return
 */
fun checkDeviceHasNavigationBar(context: Context): Boolean {
    var hasNavigationBar = false
    val rs = context.resources
    val id = rs.getIdentifier("config_showNavigationBar", "bool", "android")
    if (id > 0) {
        hasNavigationBar = rs.getBoolean(id)
    }
    try {
        val systemPropertiesClass = Class.forName("android.os.SystemProperties")
        val m = systemPropertiesClass.getMethod("get", String::class.java)
        val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
        if ("1" == navBarOverride) {
            hasNavigationBar = false
        } else if ("0" == navBarOverride) {
            hasNavigationBar = true
        }
    } catch (e: Exception) {
    }
    return hasNavigationBar
}

/**
 * 获取虚拟功能键高度
 *
 * @param context
 * @return
 */
fun getVirtualBarHeight(context: Context): Int {
    var vh = 0
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val dm = DisplayMetrics()
    try {
        val c = Class.forName("android.view.Display")
        val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
        method.invoke(display, dm)
        vh = dm.heightPixels - windowManager.defaultDisplay.height
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return vh
}

fun isNavigationBarShow(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val display = context.display
        val size = Point()
        val realSize = Point()
        display!!.getSize(size)
        display.getRealSize(realSize)
        realSize.y != size.y
    } else {
        val menu = ViewConfiguration.get(context).hasPermanentMenuKey()
        val back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        if (menu || back) {
            false
        } else {
            true
        }
    }
}