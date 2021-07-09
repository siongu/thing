package com.common.stdlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;


import com.v2x.thing.R;

import org.jetbrains.annotations.NotNull;

public class TitleView extends ConstraintLayout {
    private String TAG = this.getClass().getSimpleName();
    private OnClickListener onLeftClickListener;
    private OnClickListener onRightClickListener;
    private ImageView ivLeft;
    private TextView tvTitle;
    private TextView tvRight;
    private final String titleText;
    private final int titleColor;
    private final float titleSize;
    private final int titleTextStyle;
    private final String rightText;
    private final int rightColor;
    private final float rightSize;
    private final Drawable leftDrawable;
    private final int index;
    private final int dividerColor;
    private final int dividerHeight;
    private final int dividerMarginLeft;
    private final int dividerMarginRight;
    private Paint dividerPaint = new Paint();

    public TitleView(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public TitleView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TitleView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.title_view_layout, this, true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleView, defStyleAttr, 0);
        titleText = a.getString(R.styleable.TitleView_tiv_titleText);
        titleColor = a.getColor(R.styleable.TitleView_tiv_titleColor, Color.BLACK);
        titleSize = a.getDimension(R.styleable.TitleView_tiv_titleSize, 14f);
        titleTextStyle = a.getInt(R.styleable.TitleView_tiv_titleTextStyle, 0);
        rightText = a.getString(R.styleable.TitleView_tiv_rightText);
        rightColor = a.getColor(R.styleable.TitleView_tiv_rightColor, Color.BLACK);
        rightSize = a.getDimension(R.styleable.TitleView_tiv_rightSize, 14f);
        leftDrawable = a.getDrawable(R.styleable.TitleView_tiv_leftSrc);
        index = a.getInt(R.styleable.TitleView_tiv_leftScaleType, ImageView.ScaleType.FIT_CENTER.ordinal());
        Log.d(TAG, "index:" + index);
        dividerColor = a.getColor(R.styleable.TitleView_tiv_dividerColor, Color.TRANSPARENT);
        dividerHeight = a.getDimensionPixelOffset(R.styleable.TitleView_tiv_dividerHeight, 0);
        dividerMarginLeft = a.getDimensionPixelOffset(R.styleable.TitleView_tiv_dividerMarginLeft, 0);
        dividerMarginRight = a.getDimensionPixelOffset(R.styleable.TitleView_tiv_dividerMarginRight, 0);
        a.recycle();
        setWillNotDraw(dividerHeight <= 0);
        initView();

    }

    private void initView() {
        ivLeft = findViewById(R.id.iv_left);
        tvTitle = findViewById(R.id.tv_title);
        tvRight = findViewById(R.id.tv_right);
        ivLeft.setOnClickListener(v -> {
            if (onLeftClickListener != null) {
                onLeftClickListener.onClick(v);
            }
        });
        tvRight.setOnClickListener(v -> {
            if (onRightClickListener != null) {
                onRightClickListener.onClick(v);
            }
        });
        ImageView.ScaleType scaleType = ImageView.ScaleType.values()[index];
        ivLeft.setImageDrawable(leftDrawable);
        ivLeft.setScaleType(scaleType);

        tvTitle.setText(titleText);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
        tvTitle.setTextColor(titleColor);
        tvTitle.setTypeface(Typeface.create((String) null, titleTextStyle));

        tvRight.setText(rightText);
        tvRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, rightSize);
        tvRight.setTextColor(rightColor);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDivider(canvas);
    }

    private void drawDivider(Canvas canvas) {
        dividerPaint.setColor(dividerColor);
        dividerPaint.setStrokeWidth(dividerHeight);
        dividerPaint.setStyle(Paint.Style.FILL);
        float startX = dividerMarginLeft;
        float startY = getHeight() - dividerHeight;
        float stopX = getWidth() - dividerMarginRight;
        float stopY = startY;
        canvas.drawLine(startX, startY, stopX, stopY, dividerPaint);
    }

    public void setTitleText(String titleText) {
        tvTitle.setText(titleText);
    }

    public void setTitleColor(int titleColor) {
        tvTitle.setTextColor(titleColor);
    }

    public void setTitleSize(int titleSize) {
        tvTitle.setTextSize(titleSize);
    }

    public void setRightText(String rightText) {
        tvRight.setText(rightText);
    }

    public void setRightColor(int rightColor) {
        tvRight.setTextColor(rightColor);
    }

    public void setRightSize(int rightSize) {
        tvRight.setTextSize(rightSize);
    }

    public void setLeftImageResource(@DrawableRes int resId) {
        ivLeft.setImageResource(resId);
    }

    public void setLeftImageScaleType(ImageView.ScaleType scaleType) {
        ivLeft.setScaleType(scaleType);
    }

    public void setOnLeftClickListener(OnClickListener onLeftClickListener) {
        this.onLeftClickListener = onLeftClickListener;
    }

    public void setOnRightClickListener(OnClickListener onRightClickListener) {
        this.onRightClickListener = onRightClickListener;
    }

}
