package com.dyhdyh.view.rangetimelineview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 范围选择器
 * author  dengyuhan
 * created 2017/8/10 15:28
 */
public class RangeTimelineView extends View {
    private boolean mDebug = false;

    private float mTouchOffsetScale;//触摸容错比例

    private int mTimelineColor;
    private int mBorderColor;
    private int mIndicatorColor;

    private int mBorderWidth;//选择框上下边框高度
    private int mIndicatorWidth;//选择框左右边框宽度

    private int mRangeWidth;//选择框宽度
    private int mMinRangeWidth;//选择框最小宽度
    private int mMaxRangeWidth;//选择框最大宽度

    private boolean mDragEnabled;//拖动开关

    private Paint mPaint;

    private int mLeftTouchStartX;
    private int mLeftTouchEndX;
    private int mRightTouchStartX;
    private int mRightTouchEndX;

    private int mRangeStartX;//开始位置
    private int mRangeEndX;//结束位置

    private int mMaxRangeEndX;

    private boolean mLeftTouch;
    private boolean mRightTouch;

    private int mTargetStartX;//目标开始X
    private int mTargetEndX;//目标结束X

    private OnRangeChangeListener mOnRangeChangeListener;
    private OnRangeChangeStateListener mOnRangeChangeStateListener;

    public RangeTimelineView(Context context) {
        this(context, null);
    }

    public RangeTimelineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        Resources res = getResources();
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RangeTimelineView);
        int defHorizontalIndicator = res.getDimensionPixelSize(R.dimen.range_default_border_width);
        mBorderWidth = a.getDimensionPixelSize(R.styleable.RangeTimelineView_borderWidth, defHorizontalIndicator);
        int defVerticalIndicator = res.getDimensionPixelSize(R.dimen.range_default_indicator_width);
        mIndicatorWidth = a.getDimensionPixelSize(R.styleable.RangeTimelineView_indicatorWidth, defVerticalIndicator);
        mMinRangeWidth = a.getDimensionPixelSize(R.styleable.RangeTimelineView_minRangeWidth, 0);
        mMaxRangeWidth = a.getDimensionPixelSize(R.styleable.RangeTimelineView_maxRangeWidth, 0);
        mRangeStartX = a.getDimensionPixelSize(R.styleable.RangeTimelineView_rangeStartX, 0);
        mRangeEndX = a.getDimensionPixelSize(R.styleable.RangeTimelineView_rangeEndX, 0);
        mIndicatorColor = a.getColor(R.styleable.RangeTimelineView_indicatorColor, Color.BLACK);
        mTimelineColor = a.getColor(R.styleable.RangeTimelineView_timelineColor, 0x99000000);
        mBorderColor = a.getColor(R.styleable.RangeTimelineView_borderColor, mIndicatorColor);
        mDragEnabled = a.getBoolean(R.styleable.RangeTimelineView_dragEnabled, true);
        mDebug = a.getBoolean(R.styleable.RangeTimelineView_debug, false);
        mTouchOffsetScale = a.getFloat(R.styleable.RangeTimelineView_touchOffsetScale, 3.0f);
        a.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = measureView(widthMeasureSpec);
        int measureHeight = measureView(heightMeasureSpec);
        if (measureWidth <= 0 || measureHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(measureWidth, measureHeight);
        }
        //Log.d("onMeasure------>", measureWidth + "," + getWidth() + "," + getMeasuredWidth());
        //regulateAttribute();
    }

    /**
     * 校正
     */
    private void regulateAttribute() {
        //Log.d("1----------->", startX + "," + endX);

        int startX = mTargetStartX;
        int endX = mTargetEndX;

        int measuredWidth = getMeasuredWidth();

        //Log.d("1--->", startX + "," + endX + "," + measuredWidth + "," + mMaxRangeWidth);

        if (mMaxRangeWidth <= 0) {
            mMaxRangeWidth = measuredWidth;
        }

        if (startX < 0) {
            //指示器到达最小边界
            startX = 0;
            endX = mRangeEndX;
        }

        int maxRangeEndX = startX + mMaxRangeWidth;

        //Log.d("2--->", startX + "," + endX + "," + measuredWidth + "," + mMaxRangeWidth);

        if (maxRangeEndX >= measuredWidth) {
            //外框到达最大边界
            maxRangeEndX = measuredWidth;
        }

        int minRangeEndX = startX + mMinRangeWidth;

        //Log.d("3--->", startX + "," + endX + "," + measuredWidth + "," + mMaxRangeWidth + "," + minRangeEndX);

        if (endX > maxRangeEndX) {
            //指示器到达最大边界
            endX = maxRangeEndX;
            startX = mRangeStartX;
        } else if (endX <= minRangeEndX) {
            endX = minRangeEndX;
        }

        int minRangeStartX = endX - mMinRangeWidth;

        //Log.d("4--->", startX + "," + endX + "," + measuredWidth + "," + mMaxRangeWidth + "," + minRangeEndX + "," + minRangeStartX + "," + mMinRangeWidth);

        if (startX >= minRangeStartX) {
            //指示器到达最小边界
            startX = minRangeStartX;
        }
        //Log.d("5--->", startX + "," + endX + "," + measuredWidth + "," + mMaxRangeWidth + "," + minRangeEndX + "," + minRangeStartX + "," + mMinRangeWidth);

        this.mRangeStartX = startX;
        this.mRangeEndX = endX;
        this.mRangeWidth = mRangeEndX - mRangeStartX;
        this.mMaxRangeEndX = maxRangeEndX;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        mPaint.setColor(mTimelineColor);
        //左右遮罩
        canvas.drawRect(0, 0, mRangeStartX, height, mPaint);
        canvas.drawRect(mRangeEndX, 0, width, height, mPaint);

        //最大值外框
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderWidth);
        int borderWidthHalf = mBorderWidth / 2;
        canvas.drawRect(mRangeStartX + borderWidthHalf, 0, mMaxRangeEndX - mBorderWidth, height, mPaint);
        mPaint.reset();

        mPaint.setColor(mIndicatorColor);
        //左右指示器矩形
        canvas.drawRect(mRangeStartX, 0, mRangeStartX + mIndicatorWidth, height, mPaint);
        //int rightIndicatorX = rightTimelineX - mIndicatorWidth;
        canvas.drawRect(mRangeEndX - mIndicatorWidth, 0, mRangeEndX, height, mPaint);

        //显示可触摸区域
        if (mDebug) {
            mPaint.setColor(Color.GREEN);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(mLeftTouchStartX, 0, mLeftTouchEndX, height, mPaint);
            canvas.drawRect(mRightTouchStartX, 0, mRightTouchEndX, height, mPaint);
            mPaint.reset();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mDragEnabled) {
            return super.onTouchEvent(event);
        }
        //LogUtil.d("---------->" + event.getAction());
        if (event.getAction() == MotionEvent.ACTION_MOVE
                || event.getAction() == MotionEvent.ACTION_DOWN) {
            float eventX = event.getX();
            int offsetX = (int) (mIndicatorWidth * mTouchOffsetScale);//触摸容错值
            mLeftTouchStartX = mRangeStartX - offsetX;
            mLeftTouchEndX = mRangeStartX + mIndicatorWidth + offsetX;
            mRightTouchEndX = mRangeEndX + offsetX;
            mRightTouchStartX = mRangeEndX - mIndicatorWidth - offsetX;
            //Log.d("--------->", event.getAction() + "," + event.getX() + "," + mIndicatorWidth + "," + mLeftTimelineWidth + "," + leftStartX + " " + leftEndX + "," + (eventX >= leftStartX && eventX <= leftEndX));
            //Log.d("--------->", event.getAction() + "," + event.getX() + "," + mIndicatorWidth + "," + mRightTimelineWidth + "," + mRightTouchStartX + " " + mRightTouchEndX + "," + (eventX >= mRightTouchStartX && eventX <= mRightTouchEndX));
            if (eventX >= mLeftTouchStartX && eventX <= mLeftTouchEndX && !mRightTouch) {
                //如果触摸在左边矩形 范围
                //LogUtil.d("---------->左边");
                mLeftTouch = true;
                setTouchRange((int) eventX, mRangeEndX, false);
                return true;
            } else if (eventX >= mRightTouchStartX && eventX <= mRightTouchEndX && !mLeftTouch) {
                //如果触摸在右边矩形 范围
                //LogUtil.d("---------->右边");
                mRightTouch = true;
                setTouchRange(mRangeStartX, (int) eventX, false);
                return true;
            }
        } else {
            callComplete();
        }
        return super.onTouchEvent(event);
    }

    private void callComplete() {
        mLeftTouch = false;
        mRightTouch = false;
        if (mOnRangeChangeStateListener != null) {
            mOnRangeChangeStateListener.onRangeChangeState(OnRangeChangeStateListener.STATE_COMPLETE);
        }
    }

    public void setDragEnabled(boolean dragEnabled) {
        this.mDragEnabled = dragEnabled;
    }

    public void scrollRange(int startX, int endX) {
        setTouchRange(startX, endX, true);
    }

    private void setTouchRange(int startX, int endX, boolean callComplete) {
        int newRangeWidth = endX - startX;
        int diffStartX = startX - mRangeStartX;
        if (newRangeWidth <= mMinRangeWidth) {
            //最小边界拖动
            if (diffStartX > 0) {
                //endX = Math.max(startX + mMinRangeWidth, mRangeStartX + mMinRangeWidth);
                endX = startX + mMinRangeWidth;
            } else {
                //startX = Math.max(endX - mMinRangeWidth, 0);
                startX = endX - mMinRangeWidth;
                if (startX <= 0) {
                    startX = 0;
                    endX = startX + mMinRangeWidth;
                }
            }
        } else if (newRangeWidth >= mMaxRangeWidth) {
            //最大边界拖动
            if (diffStartX >= 0) {
                startX = endX - mMaxRangeWidth;
            } else {
                endX = startX + mMaxRangeWidth;
            }
        }
        if (mOnRangeChangeStateListener != null) {
            mOnRangeChangeStateListener.onRangeChangeState(OnRangeChangeStateListener.STATE_CHANGE);
        }
        callRange(startX, endX, callComplete);
    }

    private void callRange(int startX, int endX, final boolean callComplete) {
        mTargetStartX = startX;
        mTargetEndX = endX;
        post(new Runnable() {
            @Override
            public void run() {
                regulateAttribute();
                invalidate();
                if (mOnRangeChangeListener != null) {
                    mOnRangeChangeListener.onRangeChange(mRangeWidth, mRangeStartX, mRangeEndX);
                }
                if (callComplete) {
                    callComplete();
                }
            }
        });
    }

    public void setMinRangeWidth(int minRangeWidth) {
        this.mMinRangeWidth = minRangeWidth;
    }

    public void setMaxRangeWidth(int maxRangeWidth) {
        this.mMaxRangeWidth = maxRangeWidth;
    }

    public int getRangeWidth() {
        return mRangeWidth;
    }

    public void setOnRangeChangeListener(OnRangeChangeListener onRangeChangeListener) {
        this.mOnRangeChangeListener = onRangeChangeListener;
    }

    public void setOnRangeChangeStateListener(OnRangeChangeStateListener onRangeChangeStateListener) {
        this.mOnRangeChangeStateListener = onRangeChangeStateListener;
    }

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }

    private int measureView(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY || specMode == MeasureSpec.AT_MOST) {
            return specSize;
        }
        return 0;
    }


    public int getRangeStartX() {
        return mRangeStartX;
    }

    public int getRangeEndX() {
        return mRangeEndX;
    }

    public int getTimelineColor() {
        return mTimelineColor;
    }

    public void setTimelineColor(int timelineColor) {
        this.mTimelineColor = timelineColor;
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        this.mBorderColor = borderColor;
    }

    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.mBorderWidth = borderWidth;
    }

    public int getIndicatorWidth() {
        return mIndicatorWidth;
    }

    public void setIndicatorWidth(int indicatorWidth) {
        this.mIndicatorWidth = indicatorWidth;
    }

    /**
     * 范围改变监听(具体数值)
     */
    public interface OnRangeChangeListener {
        void onRangeChange(int rangeWidth, int rangeStartX, int rangeEndX);
    }

    public interface OnRangeChangeStateListener {
        int STATE_CHANGE = 0;
        int STATE_COMPLETE = 1;

        void onRangeChangeState(int state);
    }

}