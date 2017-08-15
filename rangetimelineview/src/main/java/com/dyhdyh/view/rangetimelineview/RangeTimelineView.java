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
    private boolean mDebug = true;

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

    private OnRangeChangeListener mOnRangeChangeListener;

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

        int measuredWidth = getMeasuredWidth();
        if (mMaxRangeWidth <= 0) {
            mMaxRangeWidth = measuredWidth;
        }
        mMaxRangeEndX = mRangeStartX + mMaxRangeWidth;
        if (mMaxRangeEndX >= measuredWidth) {
            //外框到达最大边界
            mMaxRangeEndX = measuredWidth;
        }

        int minRangeEndX = mRangeStartX + mMinRangeWidth;
        if (mRangeEndX >= mMaxRangeEndX) {
            //指示器到达最大边界
            mRangeEndX = mMaxRangeEndX;
        } else if (mRangeEndX <= minRangeEndX) {
            mRangeEndX = minRangeEndX;
        }

        int minRangeStartX = mRangeEndX - mMinRangeWidth;
        if (mRangeStartX >= minRangeStartX) {
            //指示器到达最小边界
            mRangeStartX = minRangeStartX;
        }

        if (mRangeStartX <= 0) {
            //指示器到达最小边界
            mRangeStartX = 0;
        }

        this.mRangeWidth = mRangeEndX - mRangeStartX;
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
            int offsetX = mIndicatorWidth * 3;//触摸容错值
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
                setTouchRange((int) eventX, mRangeEndX);
                return true;
            } else if (eventX >= mRightTouchStartX && eventX <= mRightTouchEndX && !mLeftTouch) {
                //如果触摸在右边矩形 范围
                //LogUtil.d("---------->右边");
                mRightTouch = true;
                setTouchRange(mRangeStartX, (int) eventX);
                return true;
            }
        } else {
            mLeftTouch = false;
            mRightTouch = false;
        }
        return super.onTouchEvent(event);
    }

    public void setDragEnabled(boolean dragEnabled) {
        this.mDragEnabled = dragEnabled;
    }

    private void setTouchRange(int startX, int endX) {
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
        setRange(startX, endX);
    }

    public void setRange(int startX, int endX) {
        this.mRangeStartX = startX;
        this.mRangeEndX = endX;
        this.mRangeWidth = mRangeEndX - mRangeStartX;
        requestLayout();
        invalidate();
        if (mOnRangeChangeListener != null) {
            mOnRangeChangeListener.onRangeChange(mRangeWidth, mRangeStartX, mRangeEndX);
        }
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

    /**
     * 范围改变监听(具体数值)
     */
    public interface OnRangeChangeListener {
        void onRangeChange(int rangeWidth, int rangeStartX, int rangeEndX);
    }

}
