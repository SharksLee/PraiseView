package com.example.administrator.praiseview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 点赞控件
 * Created by lishaojie on 2017/10/26.
 */

public class PraiseView extends LinearLayout implements View.OnClickListener {
    private static final int DIP_8 = DisplayUtil.dip2px(8);
    /**
     * 默认的padding为缩放动画留出空间
     */
    private final static int PADDING = DIP_8;

    private ImageView mImageView;
    private ScrollTextView mScrollTextView;
    private Drawable mPraiseDrawable;
    private Drawable mUnPraiseDrawable;
    private int mTextSize;
    private int mTextColor;
    public boolean mCanClick = true;
    private AnimatorSet mAnimatorSet;
    private int mLikeCount;
    private boolean mIsLiked;
    //圆的半径
    private int mCircleMaxRadius;
    //园的颜色
    private int mCircleColor = Color.parseColor("#E73256");
    private Paint mCirclePaint = new Paint();
    private int mCurrentRadius = 0;
    private IPraiseListener mIPraiseListener;
    private ValueAnimator valueAnimator;


    public PraiseView(Context context) {
        this(context, null);
    }

    public PraiseView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PraiseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        View.inflate(context, R.layout.layout_praise_view, this);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(PADDING, PADDING, PADDING, PADDING);
        setOnClickListener(this);
        mImageView = findViewById(R.id.iv_praise);
        mScrollTextView = findViewById(R.id.scroll_text_praise);

        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.PraiseView);
        mTextSize = attrArray.getDimensionPixelSize(R.styleable.PraiseView_pv_textSize, DisplayUtil.sp2px(12));
        mTextColor = attrArray.getColor(R.styleable.PraiseView_pv_textColor, Color.parseColor("#757575"));
        mPraiseDrawable = attrArray.getDrawable(R.styleable.PraiseView_pv_praise_imageSrc);
        mUnPraiseDrawable = attrArray.getDrawable(R.styleable.PraiseView_pv_unPraise_imageSrc);
        attrArray.recycle();

        initView();
    }

    private void initView() {
        if (mPraiseDrawable == null) {
            mPraiseDrawable = getResources().getDrawable(R.mipmap.icon_praise_orange);
        }
        if (mUnPraiseDrawable == null) {
            mUnPraiseDrawable = getResources().getDrawable(R.mipmap.icon_un_praise_gray);
        }
        mImageView.setImageDrawable(mIsLiked ? mPraiseDrawable : mUnPraiseDrawable);
        mScrollTextView.setTextColorAndSize(mTextColor, mTextSize);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(DisplayUtil.dip2px(2));
    }


    public void bindData(IPraiseListener praiseListener, boolean isLike, int likeCount) {
        mLikeCount = likeCount;
        mIPraiseListener = praiseListener;
        setLiked(isLike);
        refreshText(likeCount);
    }

    void refreshText(int likeCount) {
        mScrollTextView.bindData(likeCount > 0 ? likeCount : 0);

    }

    public void setLiked(boolean isLike) {
        mIsLiked = isLike;
        mImageView.setImageDrawable(isLike ? mPraiseDrawable : mUnPraiseDrawable);
    }


    public void clickLike() {
        setLiked(!mIsLiked);

        if (mAnimatorSet == null) {
            mAnimatorSet = generateScaleAnim(mImageView, 1f, 1.3f, 0.9f, 1f);
        } else {
            mAnimatorSet.cancel();
        }
        mAnimatorSet.start();
        if (mIsLiked) {
            mLikeCount++;
        } else if (mLikeCount > 0) {
            mLikeCount--;
        }
        mIPraiseListener.like(mIsLiked, mLikeCount);
        mScrollTextView.bindDataWithAnim(mLikeCount);


    }


    @Override
    public void onClick(View v) {
        if (!mCanClick) return;
        clickLike();
        generateCircleAnim();

    }

    /**
     * 生成一个缩放动画 X轴和Y轴
     *
     * @param view       需要播放动画的View
     * @param scaleValue 缩放轨迹
     * @return AnimatorSet 动画对象
     */
    public static AnimatorSet generateScaleAnim(View view, float... scaleValue) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.SCALE_X, scaleValue);
        animatorX.setDuration(600);

        ObjectAnimator animatorY = ObjectAnimator.ofFloat(view, View.SCALE_Y, scaleValue);
        animatorY.setDuration(600);

        List<Animator> animatorList = new ArrayList<>(2);
        animatorList.add(animatorX);
        animatorList.add(animatorY);
        animatorSet.playTogether(animatorList);
        return animatorSet;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mCurrentRadius, mCirclePaint);
    }

    /**
     * 计算波纹动画的最大半径
     */
    private void calculateRadius() {
        mCircleMaxRadius = Math.min(getWidth(), getHeight()) / 2 - DIP_8;
    }

    public interface IPraiseListener {
        void like(boolean isPraise, int praiseCount);
    }

    /***
     * 波纹动画
     */
    private void generateCircleAnim() {
        calculateRadius();
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        valueAnimator = ValueAnimator.ofInt(0, mCircleMaxRadius);
        valueAnimator.setDuration(600);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentRadius = (int) animation.getAnimatedValue();
                if (mCurrentRadius >= mCircleMaxRadius) {
                    mCurrentRadius = 0;
                }
                mCirclePaint.setColor(ColorUtils.setAlphaComponent(mCircleColor, (int) ((mCircleMaxRadius - mCurrentRadius) * 1.0f / mCircleMaxRadius * 255)));
                invalidate();
            }
        });
        valueAnimator.start();
    }
}
