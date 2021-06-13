package com.common.stdlib.util

import android.graphics.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin


class ImageCode {
    private var mPaddingLeft = 0
    private var mPaddingTop = 0
    private val mBuilder = StringBuilder()
    private val mRandom: Random = Random()
    private var width: Int = DEFAULT_WIDTH
    private var height: Int = DEFAULT_HEIGHT

    /**
     * 得到图片中的验证码字符串
     *
     * @return
     */
    var code: String? = null
        private set

    fun width(width: Int): ImageCode {
        this.width = width
        return this
    }

    fun height(height: Int): ImageCode {
        this.height = height
        return this
    }

    //生成验证码图片
    fun createBitmap(): Bitmap {
        mPaddingLeft = 0 //每次生成验证码图片时初始化
        mPaddingTop = 0
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        code = createCode()
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawColor(DEFAULT_COLOR)
        val paint = Paint()
        paint.textSize = DEFAULT_FONT_SIZE.toFloat()
        val randomStyle = mRandom.nextBoolean()
        for (element in code!!) {
            randomTextStyle(paint, randomStyle)
            randomPadding()
            canvas.drawText(element.toString() + "", mPaddingLeft.toFloat(), mPaddingTop.toFloat(), paint)
        }
        //干扰线
        for (i in 0 until DEFAULT_LINE_NUMBER) {
            drawLine(canvas, paint)
        }
        drawPath(canvas, paint)
        canvas.save() //保存
        canvas.restore()
        return bitmap
    }

    //生成验证码
    fun createCode(): String {
        mBuilder.delete(0, mBuilder.length) //使用之前首先清空内容
        for (i in 0 until DEFAULT_CODE_LENGTH) {
            mBuilder.append(CHARS[mRandom.nextInt(CHARS.size)])
        }
        return mBuilder.toString()
    }

    private fun drawPath(canvas: Canvas, mPaint: Paint) {
        var mTheta = 0
        val offset = 20f
        // 振幅
        val amplitude = 10f
        val height = this.height
        // 波长
        val width = this.width - offset
        var index = 0f
        val mPath = Path()
        mPath.reset()
        var first = true
        var rb = mRandom.nextBoolean()
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 4f
        while (index <= width - offset) {
            val v = if (rb) sin(index / (width - offset) * 2f * Math.PI + mTheta)
            else cos(index / (width - offset) * 2f * Math.PI + mTheta)
            val endY = (v * amplitude + height / 2).toFloat()
            if (first) {
                first = false
                mPath.moveTo(offset, endY)
            }
            mPath.lineTo(index + offset, endY)
            index++
        }
        canvas.drawPath(mPath, mPaint)
    }

    //生成干扰线
    private fun drawLine(canvas: Canvas, paint: Paint) {
        val color = randomColor()
//        val startX: Int = mRandom.nextInt(width)
//        val startY: Int = mRandom.nextInt(height)
//        val stopX: Int = mRandom.nextInt(width)
//        val stopY: Int = mRandom.nextInt(height)
        val offset = 20
        val startX: Int = offset
        val startY: Int = height / 2
        val stopX: Int = width - offset
        val stopY: Int = height / 2

        paint.strokeWidth = 3f
        paint.color = color
        canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), paint)
    }

    //随机颜色
    private fun randomColor(): Int {
        mBuilder.delete(0, mBuilder.length) //使用之前首先清空内容
        var haxString: String
        for (i in 0..2) {
            haxString = Integer.toHexString(mRandom.nextInt(0xEE))
            if (haxString.length == 1) {
                haxString = "0$haxString"
            }
            mBuilder.append(haxString)
        }
        return Color.parseColor("#$mBuilder")
    }

    //随机文本样式
    private fun randomTextStyle(paint: Paint, randomStyle: Boolean) {
        val color = randomColor()
        paint.color = color
        paint.isFakeBoldText = mRandom.nextBoolean() //true为粗体，false为非粗体
        var skewX: Float = mRandom.nextInt(11) / 10f
        skewX = if (randomStyle) skewX else -skewX
        paint.textSkewX = skewX //float类型参数，负数表示右斜，整数左斜
//        paint.isUnderlineText = mRandom.nextBoolean() //true为下划线，false为非下划线
//        paint.isStrikeThruText = mRandom.nextBoolean() //true为删除线，false为非删除线
//        paint.isStrikeThruText = true //true为删除线，false为非删除线
    }

    //随机间距
    private fun randomPadding() {
        mPaddingLeft += BASE_PADDING_LEFT + mRandom.nextInt(RANGE_PADDING_LEFT)
        mPaddingTop = BASE_PADDING_TOP + mRandom.nextInt(RANGE_PADDING_TOP)
    }

    companion object {
        //随机码集
        private val CHARS = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
        )

        //Default Settings
        private const val DEFAULT_CODE_LENGTH = 4 //验证码的长度  这里是4位
        private const val DEFAULT_FONT_SIZE = 60 //字体大小
        private const val DEFAULT_LINE_NUMBER = 0 //多少条干扰线
        private const val BASE_PADDING_LEFT = 35 //左边距
        private const val RANGE_PADDING_LEFT = 10 //左边距范围值
        private const val BASE_PADDING_TOP = 60 //上边距
        private const val RANGE_PADDING_TOP = 5 //上边距范围值
        private const val DEFAULT_WIDTH = 200 //默认宽度.图片的总宽
        private const val DEFAULT_HEIGHT = 100 //默认高度.图片的总高
        private val DEFAULT_COLOR = Color.rgb(0xee, 0xee, 0xee) //默认背景颜色值
        val INSTANCE: ImageCode by lazy { ImageCode() }

    }
}