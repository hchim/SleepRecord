package im.hch.sleeprecord.activities.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindArray;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.models.SleepQuality;
import im.hch.sleeprecord.utils.DateUtils;
import lombok.Setter;

public class SleepQualityTrendView extends View {
    @Setter
    private int mScaleLineColor = Color.BLACK;
    @Setter
    private int mGridLineColor = Color.GRAY;
    @Setter
    private int mLabelColor = Color.BLUE;
    @Setter
    private float mLabelTextSize = 10;
    @Setter
    private int mTrendLineColor = Color.BLUE;
    @Setter
    private float mMarginBottomPercent = 0.05f;
    @Setter
    private float mHeightPercent = 0.75f;
    @Setter
    private int mGridLineNumber = 5;
    @Setter
    private float mPointWidth = 5.0f;
    @Setter
    private List<SleepQuality> sleepQualities = new ArrayList<>();

    @BindArray(R.array.month_short) String[] monthsShort;

    private Paint scaleLinePaint;
    private Paint gridLinePaint;
    private Paint trendLinePaint;
    private Paint pointPaint;
    private Paint labelPaint;

    public SleepQualityTrendView(Context context) {
        super(context);
        init(null, 0);
    }

    public SleepQualityTrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SleepQualityTrendView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        ButterKnife.bind(this);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SleepQualityTrendView, defStyle, 0);

        mScaleLineColor = a.getColor(R.styleable.SleepQualityTrendView_ScaleLineColor, mScaleLineColor);
        mGridLineColor = a.getColor(R.styleable.SleepQualityTrendView_GridLineColor, mGridLineColor);
        mLabelColor = a.getColor(R.styleable.SleepQualityTrendView_LabelColor, mLabelColor);
        mLabelTextSize = a.getDimension(R.styleable.SleepQualityTrendView_LabelTextSize, mLabelTextSize);
        mTrendLineColor = a.getColor(R.styleable.SleepQualityTrendView_TrendLineColor, mTrendLineColor);
        mMarginBottomPercent = a.getFloat(R.styleable.SleepQualityTrendView_MarginBottomPercent, mMarginBottomPercent);
        mHeightPercent = a.getFloat(R.styleable.SleepQualityTrendView_HeightPercent, mHeightPercent);
        mGridLineNumber = a.getInteger(R.styleable.SleepQualityTrendView_GridLineNumber, mGridLineNumber);
        mPointWidth = a.getDimension(R.styleable.SleepQualityTrendView_PointWidth, mPointWidth);

        a.recycle();

        //init paint Resources
        scaleLinePaint = new Paint();
        scaleLinePaint.setStyle(Paint.Style.STROKE);
        scaleLinePaint.setColor(mScaleLineColor);

        labelPaint = new Paint();
        labelPaint.setColor(mLabelColor);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setTextSize(mLabelTextSize);

        gridLinePaint = new Paint();
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 5, 5, 5}, 1));
        gridLinePaint.setColor(mGridLineColor);

        trendLinePaint = new Paint();
        trendLinePaint.setStyle(Paint.Style.STROKE);
        trendLinePaint.setColor(mScaleLineColor);

        pointPaint = new Paint();
        pointPaint.setStrokeWidth(mPointWidth);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setColor(mScaleLineColor);
    }

    private int paddingLeft = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;
    private int paddingBottom = 0;
    private int bottomLineHeight = 0;
    private int contentWidth = 0;
    private int contentHeight = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();
        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = (int) (contentWidth * mHeightPercent);
        setMinimumHeight(contentHeight + paddingTop + paddingBottom);
        bottomLineHeight = (int) (contentHeight * (1 - mMarginBottomPercent));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawScalesAndLabels(canvas);
        drawGridLines(canvas);
        drawSleepQuality(canvas);
    }

    /**
     * Draw the time scales and time labels.
     * @param canvas
     */
    private void drawScalesAndLabels(Canvas canvas) {
        Rect paintArea = new Rect(0, 0, contentWidth, bottomLineHeight);
        canvas.drawRect(paintArea, scaleLinePaint);

        float margin = contentWidth * 1.0f / (sleepQualities.size() + 1);
        for (int i = 0; i < sleepQualities.size(); i++) {
            SleepQuality sq = sleepQualities.get(i);
            if (shouldDrawLabel(sq.getDate())) {
                canvas.drawLine((i + 1) * margin, bottomLineHeight,
                        (i + 1) * margin, bottomLineHeight - 10, scaleLinePaint);

                String text = getDateLabel(sq.getDate());
                float textWidth = labelPaint.measureText(text);
                float x = (i + 1) * margin - textWidth / 2;
                canvas.drawText(text, x, bottomLineHeight + mLabelTextSize, labelPaint);
            } else {
                canvas.drawLine((i + 1) * margin, bottomLineHeight,
                        (i + 1) * margin, bottomLineHeight - 5, scaleLinePaint);
            }
        }
    }

    private boolean shouldDrawLabel(Date date) {
        if (sleepQualities.size() < 100) { // every week
            return DateUtils.isMonday(date);
        } else { // every month
            return DateUtils.isFirstDayOfMonth(date);
        }
    }

    private String getDateLabel(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (sleepQualities.size() < 100) { // every week
            return String.format("%d/%d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            return String.format("%s", monthsShort[calendar.get(Calendar.MONTH)]);
        }
    }

    /**
     * Draw the grid lines.
     * @param canvas
     */
    private void drawGridLines(Canvas canvas) {
        float margin = bottomLineHeight * 1.0f / (mGridLineNumber + 1);
        for (int i = 0; i < mGridLineNumber; i++) {
            Path path = new Path();
            path.moveTo(0, (i + 1) * margin);
            path.lineTo(contentWidth, (i + 1) * margin);
            canvas.drawPath(path, gridLinePaint);
        }
    }

    /**
     * Draw sleep quality dot and lines.
     * @param canvas
     */
    private void drawSleepQuality(Canvas canvas) {
        float margin = contentWidth * 1.0f / (sleepQualities.size() + 1);
        float lastx = -1;
        float lasty = -1;
        for (int i = 0; i < sleepQualities.size(); i++) {
            SleepQuality sq = sleepQualities.get(i);
            float x = (i + 1) * margin;
            float y = (10.0f - sq.getSleepQuality()) / 10.0f * contentHeight;
            if (lastx != -1) {
                Path path = new Path();
                path.moveTo(lastx, lasty);
                path.lineTo(x, y);
                canvas.drawPath(path, trendLinePaint);
            }

            canvas.drawPoint(x, y, pointPaint);
            lastx = x;
            lasty = y;
        }
    }
}
