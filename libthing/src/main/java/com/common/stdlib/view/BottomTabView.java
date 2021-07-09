package com.common.stdlib.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.v2x.thing.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xzw
 */
public class BottomTabView extends LinearLayout {
    private int[] mDrawableIds;
    private List<CheckBox> mCheckedList = new ArrayList<CheckBox>();
    private List<TabItemHolder> mViewList = new ArrayList<TabItemHolder>();
    private List<ImageView> mIndicators = new ArrayList<ImageView>();
    private CharSequence[] mLabels;
    private Drawable mSelectBackgroud;
    private Drawable mUnSelectBackgroud;
    private Drawable mIndicatorDrawble;
    private ColorStateList mTabColorStateList;
    private Drawable mHorizontalDivider;
    private float textSize;
    private int mDividerWidth;
    private int mDrawablePadding;
    private int mIndicatorMarginTop;
    private int mIndicatorMarginBottom;
    private int mIndicatorMarginLeft;
    private int mIndicatorMarginRight;
    private int mCount;

    public BottomTabView(Context context) {
        super(context);
        init(context);
    }

    public BottomTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomTabView, defStyle, 0);
        mLabels = a.getTextArray(R.styleable.BottomTabView_bottom_labels);
        mSelectBackgroud = a.getDrawable(R.styleable.BottomTabView_tab_select_background);
        mUnSelectBackgroud = a.getDrawable(R.styleable.BottomTabView_tab_unSelect_background);
        mIndicatorDrawble = a.getDrawable(R.styleable.BottomTabView_tab_indicator_drawable);
        mTabColorStateList = a.getColorStateList(R.styleable.BottomTabView_tab_textColor_selector);
        mHorizontalDivider = a.getDrawable(R.styleable.BottomTabView_tab_horizontal_divider);
        mDividerWidth = a.getDimensionPixelSize(R.styleable.BottomTabView_tab_divider_width, 1);
        mDrawablePadding = a.getDimensionPixelSize(R.styleable.BottomTabView_tab_drawable_padding, 1);
        mIndicatorMarginTop = a.getDimensionPixelSize(R.styleable.BottomTabView_tab_indicator_margin_top, 0);
        mIndicatorMarginBottom = a.getDimensionPixelSize(R.styleable.BottomTabView_tab_indicator_margin_bottom, 0);
        mIndicatorMarginLeft = a.getDimensionPixelSize(R.styleable.BottomTabView_tab_indicator_margin_left, 0);
        mIndicatorMarginRight = a.getDimensionPixelSize(R.styleable.BottomTabView_tab_indicator_margin_rigth, 0);
        mCount = a.getInt(R.styleable.BottomTabView_tab_count, 0);
        textSize = a.getDimensionPixelSize(R.styleable.BottomTabView_tab_text_size, 11);
        a.recycle();
        init(context);
    }

    private void init(final Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;
        params.gravity = Gravity.CENTER;
        mCheckedList.clear();
        mIndicators.clear();
        mViewList.clear();
        for (int i = 0; i < mCount; i++) {
            final int index = i;
            TabItemHolder holder = createTabItem();
            mCheckedList.add(holder.tab);
            mIndicators.add(holder.indicator);
            mViewList.add(holder);
            setTabCompoundDrawables(i);
            setTabText(i);
            holder.tab.setTag(index);
            if (mTabColorStateList != null) {
                holder.tab.setTextColor(mTabColorStateList);
            }
            this.addView(holder.root, params);
            if (i >= 0 && i < mCount - 1) {// set horizontal divider
                View divider = new View(context);
                divider.setBackgroundDrawable(mHorizontalDivider);
                this.addView(divider, new LayoutParams(mDividerWidth,
                        LayoutParams.WRAP_CONTENT));
            }
            holder.root.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTabDisplay(index);
                    if (null != mTabListener) {
                        mTabListener.onTabSelected(index);
                    }
                }
            });

            if (i == 0) {
                holder.tab.setChecked(true);
                holder.root.setBackgroundDrawable(mSelectBackgroud);
            } else {
                holder.tab.setChecked(false);
                holder.root.setBackgroundDrawable(mUnSelectBackgroud);
            }

        }
    }

    private TabItemHolder createTabItem() {
        RelativeLayout root = new RelativeLayout(getContext());
        CheckBox tab = new CheckBox(getContext());
        tab.setGravity(Gravity.CENTER);
        tab.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        tab.setBackgroundColor(Color.TRANSPARENT);
        tab.setClickable(false);
        ImageView indicator = new ImageView(getContext());
        TabItemHolder holder = new TabItemHolder(root, tab, indicator);
        RelativeLayout.LayoutParams alpTab = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        alpTab.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout.LayoutParams alpIndicator = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams
                .WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        tab.setId(R.id.tab);
        indicator.setId(R.id.indicator);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        alpIndicator.addRule(RelativeLayout.ALIGN_TOP, tab.getId());
        alpIndicator.addRule(RelativeLayout.ALIGN_RIGHT, tab.getId());
        alpIndicator.topMargin = mIndicatorMarginTop;
        alpIndicator.bottomMargin = mIndicatorMarginBottom;
        alpIndicator.leftMargin = mIndicatorMarginLeft;
        alpIndicator.rightMargin = mIndicatorMarginRight;
        indicator.setImageDrawable(mIndicatorDrawble);
        indicator.setVisibility(GONE);
        root.addView(tab, alpTab);
        root.addView(indicator, alpIndicator);
        return holder;
    }

    public void setTabLabels(String[] lables) {
        if (lables != null) {
            this.mLabels = lables;
            for (int i = 0; i < lables.length; i++) {
                setTabText(i);
            }
        }
    }

    public void setTabResIds(int[] resIds) {
        if (resIds != null) {
            this.mDrawableIds = resIds;
            for (int i = 0; i < resIds.length; i++) {
                setTabCompoundDrawables(i);
            }
        }
    }

    private void setTabText(int index) {
        TabItemHolder holder = mViewList.get(index);
        if (mLabels != null && mLabels.length > index) {
            holder.tab.setText(mLabels[index]);
        }
    }

    private void setTabCompoundDrawables(int index) {
        TabItemHolder holder = mViewList.get(index);
        if (mDrawableIds != null && mDrawableIds.length > index) {
            holder.tab.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable
                    (mDrawableIds[index]), null, null);
            holder.tab.setCompoundDrawablePadding(mDrawablePadding);
        }
    }

    public void setIndicatorDisplay(int position, boolean visible) {
        int size = mIndicators.size();
        if (size <= position) {
            return;
        }
        ImageView indicator = mIndicators.get(position);
        indicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setTabDisplay(int index) {
        int size = mCheckedList.size();
        for (int i = 0; i < size; i++) {
            CheckBox checkedTextView = mCheckedList.get(i);
            if ((Integer) (checkedTextView.getTag()) == index) {
                checkedTextView.setChecked(true);
                mViewList.get(i).root.setBackgroundDrawable(mSelectBackgroud);
            } else {
                checkedTextView.setChecked(false);
                mViewList.get(i).root.setBackgroundDrawable(mUnSelectBackgroud);
            }
        }
    }

    private OnTabSelectedListener mTabListener;

    public interface OnTabSelectedListener {
        void onTabSelected(int index);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.mTabListener = listener;
    }

    static class TabItemHolder {
        RelativeLayout root;
        CheckBox tab;
        ImageView indicator;

        TabItemHolder(RelativeLayout root, CheckBox tab, ImageView indicator) {
            this.root = root;
            this.tab = tab;
            this.indicator = indicator;
        }

    }
}
