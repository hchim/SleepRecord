package im.hch.sleeprecord.views;

import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

public class CountDownTextView extends TextView {

    public static final String FORMAT = "%02d:%02d";

    private CountDownTimer mCountDownTimer = null;
    private long mTotalTime = 0;
    private long mTimeLeft = 0;

    public CountDownTextView(Context context) {
        super(context);
    }

    public CountDownTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CountDownTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CountDownTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Start to count down.
     * @param timeInMinutes
     */
    public void start(int timeInMinutes, final OnFinishCallback callback) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mTotalTime = timeInMinutes * 60 * 1000;
        mCountDownTimer = new CountDownTimer(mTotalTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeft = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                CountDownTextView.this.setText(String.format(FORMAT, seconds / 60, seconds % 60));
            }

            @Override
            public void onFinish() {
                if (callback != null) {
                    callback.onFinish();
                }
            }
        };
        mCountDownTimer.start();
    }

    /**
     * Stop count down.
     */
    public void stop() {
        if (mCountDownTimer == null) {
            return;
        }

        mCountDownTimer.cancel();
    }

    /**
     * Callback interface when count down finishes.
     */
    public interface OnFinishCallback {
        public void onFinish();
    }
}
