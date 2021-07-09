package com.common.stdlib.util

import android.text.TextUtils
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.regex.Pattern

/**
 * 输入数据check
 */
object StringUtil {
    private var numbersAndLetters: CharArray? = null
    private val initLock = Any()
    private var randGen: Random? = null

    /**
     * 判断email格式
     *
     * @param str
     * @return
     */
    fun isEmail(str: String): Boolean {
        val regularExpression = Regex("^([a-z0-9A-Z]+[-|//.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?//.)+[a-zA-Z]{2,}$")
        if (TextUtils.isEmpty(str)) {
            return false
        } else if (!str.matches(regularExpression)) {
            return false
        }
        return true
    }

    //判断是否为中文
    fun isChinese(s: String, minLength: Int, maxLength: Int): Boolean {
        val regex = Regex("[\u4E00-\u9FA5]+")
        return if (TextUtils.isEmpty(s)) {
            false
        } else s.matches(regex) && s.length >= minLength && s.length <= maxLength
    }

    //判断是否为中文
    fun isChinese(s: String): Boolean {
        val regex = Regex("[\u4E00-\u9FA5]+")
        return if (TextUtils.isEmpty(s)) {
            false
        } else s.matches(regex)
    }

    /**
     * 判断是否是URL
     *
     * @param str
     * @return
     */
    fun isURL(str: String?): Boolean {
        val regex = Regex("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
        val isMatch = str?.matches(regex)
        return isMatch ?: false
    }

    /**
     * 是否是手机号码
     *
     * @param phone
     * @return
     */
    fun isCellPhone(phone: String?): Boolean {
        val regex = Regex("^[1][3,4,5,7,8]+\\d{9}$")
        return phone?.matches(regex) ?: false
    }

    fun checkPassword(password: String?): Boolean {
        return Pattern.matches("^[0-9a-zA-Z]{6,18}$", password ?: "")
    }

    fun desensitizePhone(phone: String?): String {
        return phone?.replaceRange(IntRange(4, 7), "****") ?: ""
    }

    fun desensitizePassword(password: String?, replaceWith: String = "*"): String {
        return password?.run {
            replaceRange(IntRange(0, length - 1), replaceWith.repeat(length))
        } ?: ""
    }

    /**
     * 产生不重复随机数
     *
     * @param length
     * @return String类型随机数
     */
    fun randomString(length: Int): String? {
        if (length < 1) {
            return null
        }
        // Init of pseudo random number generator.
        if (randGen == null) {
            synchronized(initLock) {
                if (randGen == null) {
                    randGen = Random()
                    // Also initialize the numbersAndLetters array
                    numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz"
                            + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                        .toCharArray()
                }
            }
        }
        // Create a char buffer to put random letters and numbers in.
        val randBuffer = CharArray(length)
        for (i in randBuffer.indices) {
            randBuffer[i] = numbersAndLetters!![randGen!!.nextInt(71)]
        }
        return String(randBuffer)
    }

    fun getMD5(s: String?): String {
        if (s != null && s.isNotEmpty()) {
            try {
                val md = MessageDigest.getInstance("MD5")
                md.update(s.toByteArray())
                val b = md.digest()
                var i: Int
                val buf = StringBuffer("")
                for (offset in b.indices) {
                    i = b[offset].toInt()
                    if (i < 0) i += 256
                    if (i < 16) buf.append("0")
                    buf.append(Integer.toHexString(i))
                }
                return buf.toString().substring(0, 32)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        }
        return "md5o"
    }

    /**
     * 定义script的正则表达式
     */
    private const val REGEX_SCRIPT = "<script[^>]*?>[\\s\\S]*?<\\/script>"

    /**
     * 定义style的正则表达式
     */
    private const val REGEX_STYLE = "<style[^>]*?>[\\s\\S]*?<\\/style>"

    /**
     * 定义HTML标签的正则表达式
     */
    private const val REGEX_HTML = "<[^>]+>"

    /**
     * 定义空格回车换行符
     */
    private const val REGEX_SPACE = "\\s*|\t|\r|\n"
    fun filterHtmlTag(htmlStr: String): String {
        // 过滤script标签
        var htmlStr = htmlStr
        val p_script = Pattern.compile(REGEX_SCRIPT, Pattern.CASE_INSENSITIVE)
        val m_script = p_script.matcher(htmlStr)
        htmlStr = m_script.replaceAll("")
        // 过滤style标签
        val p_style = Pattern.compile(REGEX_STYLE, Pattern.CASE_INSENSITIVE)
        val m_style = p_style.matcher(htmlStr)
        htmlStr = m_style.replaceAll("")
        // 过滤html标签
        val p_html = Pattern.compile(REGEX_HTML, Pattern.CASE_INSENSITIVE)
        val m_html = p_html.matcher(htmlStr)
        htmlStr = m_html.replaceAll("")
        // 过滤空格回车标签
        val p_space = Pattern.compile(REGEX_SPACE, Pattern.CASE_INSENSITIVE)
        val m_space = p_space.matcher(htmlStr)
        htmlStr = m_space.replaceAll("")
        return htmlStr.trim { it <= ' ' } // 返回文本字符串
    }
}