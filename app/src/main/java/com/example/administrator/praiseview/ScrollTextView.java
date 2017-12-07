package com.example.administrator.praiseview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.View;


/**
 * 数字垂直滚动的TextView
 * 如果不需要动画 可以通过{@link #bindData(int)}
 * 如果需要动画 可以通过{@link #bindDataWithAnim(int)}}
 * Created by lishaojie on 2017/10/26.
 */

public class ScrollTextView extends View {
    public static final int ANIM_DURATION = 300;
    /**
     * //num[0]是不变的部分，mChangeNumbers[1]原来的部分，mChangeNumbers[2]变化后的部分;
     * 比如从110，到111，num[0] = 11，mChangeNumbers[1] = 0，mChangeNumbers[2]= 1;
     */
    private String[] mChangeNumbers;
    /**
     * 原始数据
     */
    private int mOriginValue;
    /**
     * 是否是增大
     */
    private boolean toBigger;
    private int mTextSize;
    //文本颜色
    private
    @ColorInt
    int mTextColor;
    // 文本可点击颜色
    private
    @ColorInt
    int mClickTextColor;
    private
    @ColorInt
    int mTextEndColor;
    @ColorInt
    int mTextStartColor;
    private Paint mTextPaint;
    private float mOldOffsetY;
    private float mNewOffsetY;
    private int mChange;
    private int mStartX;
    /**
     * 单个字符的宽度
     */
    private float mSingleTextWidth;

    public ScrollTextView(Context context) {
        this(context, null);
    }

    public ScrollTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScrollTextView);
        mTextSize = attrArray.getDimensionPixelSize(R.styleable.ScrollTextView_sv_textColor, DisplayUtil.sp2px(12));
        mTextColor = attrArray.getColor(R.styleable.ScrollTextView_sv_textSize, Color.parseColor("#757575"));
        attrArray.recycle();

        mChangeNumbers = new String[]{String.valueOf(mOriginValue), "", ""};
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        initPaint();
    }

    private void initPaint() {
        mTextPaint.setTextSize(mTextSize);
        mSingleTextWidth = mTextPaint.measureText("0");
        mTextStartColor = mTextColor;
        mTextEndColor = ColorUtils.setAlphaComponent(mTextStartColor, 0);
        mStartX = getPaddingLeft();
    }

    public void setClickTextColor(@ColorInt int textColor) {
        mClickTextColor = textColor;
    }

    public void setTextColorAndSize(@ColorInt int textColor, @IntRange(from = 0) int textSize) {
        mTextColor = textColor;
        mTextSize = textSize;
        initPaint();
        postInvalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getWidth(widthMeasureSpec), getHeight(heightMeasureSpec));
    }

    private int getWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                result = getContentWidth();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                result = Math.max(getContentWidth(), result);
                break;
        }
        return result;
    }

    private int getHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                result = getContentHeight();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                result = Math.max(getContentHeight(), result);
                break;
        }
        return result;
    }

    private int getContentWidth() {
        /**
         * 加1为了防止进位时宽度不够显示不下
         */
        return (int) (getPaddingRight() + getPaddingLeft() + mSingleTextWidth * (String.valueOf(mOriginValue).length() + 1));
    }

    private int getContentHeight() {
        return getPaddingTop() + getPaddingBottom() + DisplayUtil.sp2px(mTextSize);
    }

    public void setTextOffsetY(float offsetY) {
        this.mOldOffsetY = offsetY;//变大是从[0,1]，变小是[0,-1]
        if (toBigger) {//从下到上[-1,0]
            this.mNewOffsetY = mTextSize + offsetY;
        } else {//从上到下[1,0]
            this.mNewOffsetY = offsetY - mTextSize;
        }
        postInvalidate();
    }

    /**
     * 不带动画适合第一次绑定数据
     *
     * @param value
     */
    public void bindData(int value) {
        mOriginValue = value;
        calculateChangeNum(0);
        refreshColor();
        postInvalidate();
    }

    /**
     * 上下滚动动画
     *
     * @param value
     */
    public void bindDataWithAnim(int value) {
        if (value > mOriginValue) {
            praise();
        } else if (value < mOriginValue) {
            unPraise();
        } else {
            bindData(value);
        }
    }

    private void praise() {
        calculateChangeNum(1);
        refreshColor();
        ObjectAnimator textOffsetY = ObjectAnimator.ofFloat(this, "textOffsetY", 0, -mTextSize);
        textOffsetY.setDuration(ANIM_DURATION);
        textOffsetY.start();

    }

    private void unPraise() {
        calculateChangeNum(-1);
        refreshColor();
        ObjectAnimator textOffsetY = ObjectAnimator.ofFloat(this, "textOffsetY", 0, mTextSize);
        textOffsetY.setDuration(ANIM_DURATION);
        textOffsetY.start();

    }

    private boolean canClick() {
        return mClickTextColor != 0 && mOriginValue > 0;
    }

    /**
     * 计算不变，原来，和改变后各部分的数字
     * 这里是只针对加一和减一去计算的算法，因为直接设置的时候没有动画
     */
    private void calculateChangeNum(int change) {
        mChange = change;
        if (change == 0) {
            mChangeNumbers[0] = String.valueOf(mOriginValue);
            mChangeNumbers[1] = "";
            mChangeNumbers[2] = "";
            return;
        }
        toBigger = change > 0;
        String oldNum = String.valueOf(mOriginValue);
        String newNum = String.valueOf(mOriginValue + change);

        int oldNumLen = oldNum.length();

        if (isLengthDifferent(mOriginValue, mOriginValue + change)) {
            mChangeNumbers[0] = "";
            mChangeNumbers[1] = oldNum;
            mChangeNumbers[2] = newNum;
        } else {
            for (int i = 0; i < oldNumLen; i++) {
                char oldC1 = oldNum.charAt(i);
                char newC1 = newNum.charAt(i);
                if (oldC1 != newC1) {
                    if (i == 0) {
                        mChangeNumbers[0] = "";
                    } else {
                        mChangeNumbers[0] = newNum.substring(0, i);
                    }
                    mChangeNumbers[1] = oldNum.substring(i);
                    mChangeNumbers[2] = newNum.substring(i);
                    break;
                }
            }
        }
        mOriginValue = mOriginValue + change;


    }

    private void refreshColor() {
        boolean canClick = canClick();
        setEnabled(canClick);
        if (canClick) {
            mTextPaint.setColor(mClickTextColor);
            mTextStartColor = mClickTextColor;
        } else {
            mTextPaint.setColor(mTextColor);
            mTextStartColor = mTextColor;
        }
        mTextEndColor = ColorUtils.setAlphaComponent(mTextStartColor, 0);
    }


    /**
     * 是否有进位或者退位
     */
    private boolean isLengthDifferent(int oldValue, int newValue) {
        return String.valueOf(oldValue).length() != String.valueOf(newValue).length();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        float y = (getHeight() - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(String.valueOf(mChangeNumbers[0]), mStartX, y, mTextPaint);
        if (mChange != 0) {
            //字体滚动
            float fraction = (mTextSize - Math.abs(mOldOffsetY)) / mTextSize;
            mTextPaint.setColor(evaluate(fraction, mTextEndColor, mTextStartColor));
            canvas.drawText(String.valueOf(mChangeNumbers[1]), mSingleTextWidth * mChangeNumbers[0].length() + mStartX, y + mOldOffsetY, mTextPaint);
            mTextPaint.setColor(evaluate(fraction, mTextStartColor, mTextEndColor));
            canvas.drawText(String.valueOf(mChangeNumbers[2]), mSingleTextWidth * mChangeNumbers[0].length() + mStartX, y + mNewOffsetY, mTextPaint);
        }

    }


    /**
     * 颜色的估值
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public
    @ColorInt
    int evaluate(float fraction, @ColorInt int startValue, @ColorInt int endValue) {
        int startInt = startValue;
        float startA = ((startInt >> 24) & 0xff) / 255.0f;
        float startR = ((startInt >> 16) & 0xff) / 255.0f;
        float startG = ((startInt >> 8) & 0xff) / 255.0f;
        float startB = (startInt & 0xff) / 255.0f;

        int endInt = endValue;
        float endA = ((endInt >> 24) & 0xff) / 255.0f;
        float endR = ((endInt >> 16) & 0xff) / 255.0f;
        float endG = ((endInt >> 8) & 0xff) / 255.0f;
        float endB = (endInt & 0xff) / 255.0f;

        // convert from sRGB to linear
        startR = (float) Math.pow(startR, 2.2);
        startG = (float) Math.pow(startG, 2.2);
        startB = (float) Math.pow(startB, 2.2);

        endR = (float) Math.pow(endR, 2.2);
        endG = (float) Math.pow(endG, 2.2);
        endB = (float) Math.pow(endB, 2.2);

        // compute the interpolated color in linear space
        float a = startA + fraction * (endA - startA);
        float r = startR + fraction * (endR - startR);
        float g = startG + fraction * (endG - startG);
        float b = startB + fraction * (endB - startB);

        // convert back to sRGB in the [0..255] range
        a = a * 255.0f;
        r = (float) Math.pow(r, 1.0 / 2.2) * 255.0f;
        g = (float) Math.pow(g, 1.0 / 2.2) * 255.0f;
        b = (float) Math.pow(b, 1.0 / 2.2) * 255.0f;

        return Math.round(a) << 24 | Math.round(r) << 16 | Math.round(g) << 8 | Math.round(b);
    }

}
