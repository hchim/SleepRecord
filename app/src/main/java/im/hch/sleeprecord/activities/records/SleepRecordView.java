package im.hch.sleeprecord.activities.records;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import im.hch.sleeprecord.R;

public class SleepRecordView extends View {

    private static final int MINUTES_A_DAY = 24 * 60;

    // the color of the scale line
    private int mScaleLineColor = Color.BLACK;
    // the height percentage of time scale part
    private float mScaleHeightPercentage = 0.4f;
    // the time label color
    private int mTimeLabelColor = Color.BLACK;
    // hour type: 12 or 24
    private int mHourType = 12;
    // font size of time label
    private float mTimeLabelFontSize = 5;
    // the height of time scale
    private float mTimeScaleHeight = 5;
    private int mDefaultSleepTimeColor = Color.BLACK;
    private int mBestSleepTimeColor = Color.GREEN;
    private int mWorstSleepTimeColor = Color.RED;

    private String[] hourLabels;

    private List<Pair<Date, Date>> mSleepTimePairs;
    private double mSleepQuality = 0;

    public SleepRecordView(Context context) {
        super(context);
        init(null, 0);
    }

    public SleepRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SleepRecordView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SleepRecordView, defStyle, 0);

        mScaleLineColor = a.getColor(R.styleable.SleepRecordView_scaleLineColor, mScaleLineColor);
        mScaleHeightPercentage = a.getFloat(R.styleable.SleepRecordView_scaleHeightPercentage, mScaleHeightPercentage);
        mHourType = a.getInt(R.styleable.SleepRecordView_hourType, mHourType);
        mTimeLabelFontSize = a.getDimension(R.styleable.SleepRecordView_timeLabelFontSize, mTimeLabelFontSize);
        mTimeScaleHeight = a.getDimension(R.styleable.SleepRecordView_timeScaleHeight, mTimeScaleHeight);
        mDefaultSleepTimeColor = a.getColor(R.styleable.SleepRecordView_defaultSleepTimeColor, mDefaultSleepTimeColor);
        mBestSleepTimeColor = a.getColor(R.styleable.SleepRecordView_bestSleepTimeColor, mBestSleepTimeColor);
        mWorstSleepTimeColor = a.getColor(R.styleable.SleepRecordView_worstSleepTimeColor, mWorstSleepTimeColor);
        mTimeLabelColor = a.getColor(R.styleable.SleepRecordView_timeLabelColor, mTimeLabelColor);

        a.recycle();

        hourLabels = new String[24];
        for (int i = 0; i < 24; i++) {
            if (mHourType == 12) {
                hourLabels[i] = i + (i < 12 ? "AM" : "PM");
            } else {
                hourLabels[i] = String.valueOf(i);
            }
        }

        initPaintResources();
    }

    public void setSleepTimePairs(List<Pair<Date, Date>> mSleepTimePairs) {
        this.mSleepTimePairs = mSleepTimePairs;
    }

    public void setSleepQuality(double mSleepQuality) {
        this.mSleepQuality = mSleepQuality;
    }

    private int paddingLeft = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;
    private int paddingBottom = 0;
    private int contentWidth = 0;
    private int contentHeight = 0;
    private int scaleLinePosition = 0;

    private Paint scaleLinePaint;
    private Paint mTextPaint;
    private Paint sleepTimePaint;

    private void initPaintResources() {
        //scale line paint
        scaleLinePaint = new Paint();
        scaleLinePaint.setColor(mScaleLineColor);

        //scale label paint
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize(mTimeLabelFontSize);
        mTextPaint.setColor(mTimeLabelColor);

        //sleep time paint
        sleepTimePaint = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();
        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;
        scaleLinePosition = paddingTop + (int) (contentHeight * (1 - mScaleHeightPercentage));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScalesAndLabels(canvas);
        drawSleepTimes(canvas);
    }

    /**
     * Draw the time scales and time labels.
     * @param canvas
     */
    private void drawScalesAndLabels(Canvas canvas) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float mTextWidth;
        float mTextHeight = fontMetrics.bottom;

        canvas.drawLine(paddingLeft, scaleLinePosition,
                        paddingLeft + contentWidth, scaleLinePosition, scaleLinePaint);

        float scaleMarginWidth = contentWidth * 1.0f / 24;
        float x = 0;
        for (int i = 0; i <= 24; i++) {
            x = paddingLeft + scaleMarginWidth * i;

            if ((i % 2) == 1) {
                //draw scale line
                canvas.drawLine(x, scaleLinePosition, x, scaleLinePosition + mTimeScaleHeight, scaleLinePaint);
                //draw time label
                mTextWidth = mTextPaint.measureText(hourLabels[i]);
                canvas.drawText(hourLabels[i], x - mTextWidth / 2, paddingTop + contentHeight - mTextHeight, mTextPaint);
            } else {
                //draw scale line
                canvas.drawLine(x, scaleLinePosition, x, scaleLinePosition + mTimeScaleHeight * 2, scaleLinePaint);
            }
        }
    }

    /**
     * Draw sleep time pairs.
     * @param canvas
     */
    private void drawSleepTimes(Canvas canvas) {
        if (mSleepTimePairs == null) {
            return;
        }

        updateSleepTimePaintColor(sleepTimePaint);

        for (Pair<Date, Date> pair : mSleepTimePairs) {
            float beginPos = calculateTimePosition(pair.first);
            float endPos = calculateTimePosition(pair.second);
            canvas.drawRect(beginPos, 0, endPos, scaleLinePosition, sleepTimePaint);
        }
    }

    private void updateSleepTimePaintColor(Paint paint) {
        if (mSleepQuality == 0) {
            paint.setColor(mDefaultSleepTimeColor);
        }

        //TODO update color based on sleep quality
    }

    private float calculateTimePosition(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int timeInMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        float pos = paddingLeft + contentWidth * (timeInMinutes * 1.0f / MINUTES_A_DAY);
        return pos;
    }
}
