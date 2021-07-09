package com.common.stdlib.view

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import java.lang.Runnable

class CountDownButton @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) :
    AppCompatButton(
        context!!, attrs, defStyle
    ) {
    private var mTicker: Runnable? = null
    private var mHandler: Handler? = null
    private var mTickerStopped = false

    // 监听回调
    private var onCountDownListener: OnCountDownListener? = null

    //文字更新
    private var updateTextListener: OnUpdateTextListener? = null
    private var count = 0 // 倒计时的步数

    // 原始文字
    private var originalText: CharSequence? = null

    fun sendText(text: CharSequence): CharSequence {
        return text
    }

    private fun init() {
        originalText = text
        text = originalText
        isAllCaps = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mTickerStopped = true
    }

    override fun onAttachedToWindow() {
        mTickerStopped = false
        mHandler = Handler()
        super.onAttachedToWindow()
    }

    private fun runCountButton() {
        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = Runnable {
            if (mTickerStopped) return@Runnable
            count--
            if (onCountDownListener != null) {
                onCountDownListener!!.onTick(count)
            }
            if (count <= 0) {
                onCountDownListener?.onFinish()
                text = updateTextListener?.onEndText()
                isEnabled = true
                return@Runnable
            } else {
                text = updateTextListener!!.onCountingText(count)
            }
            val now = SystemClock.uptimeMillis()
            val next = now + (1000 - now % 1000)
            mHandler?.postAtTime(mTicker!!, next)
        }
        mTicker?.run()
    }

    /**
     * 倒计时监听
     */
    interface OnCountDownListener {
        fun onFinish()
        fun onTick(time: Int)
    }

    fun setOnCountDownListener(onCountDownListener: OnCountDownListener?) {
        this.onCountDownListener = onCountDownListener
    }

    /**
     * 倒计时文字更新
     */
    interface OnUpdateTextListener {
        fun onPreText(): String?
        fun onCountingText(count: Int): String?
        fun onEndText(): String?
    }

    fun setOnUpdateTextListener(updateTextListener: OnUpdateTextListener?) {
        this.updateTextListener = updateTextListener
        if (updateTextListener != null) {
            text = updateTextListener.onPreText()
        }
    }

    fun getCount(): Int {
        return count
    }

    private fun setCount(count: Int) {
        if (count < 0) {
            this.count = 0
            return
        }
        this.count = count
    }

    fun start() {
        setCount(COUNT)
        isEnabled = false
        runCountButton()
    }

    fun reset() {
        if (!mTickerStopped && count > 0) {
            count = 0
        }
    }

    fun start(count: Int) {
        setCount(count)
        isEnabled = false
        runCountButton()
    }

    companion object {
        private const val COUNT = 60 //60 second default
    }

    init {
        init()
    }
}