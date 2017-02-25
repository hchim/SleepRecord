package im.hch.sleeprecord.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

import im.hch.sleeprecord.utils.CountUpTimer;

public class CountUpTextView extends TextView {

    public static final String FORMAT = "%02d:%02d";

    private CountUpTimer mCountUpTimer = null;
    private long mCountedTime = 0;

    public CountUpTextView(Context context) {
        super(context);
    }

    public CountUpTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CountUpTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CountUpTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Start to count down.
     * @param timeInMinutes
     */
    public void start(int timeInMinutes) {
        if (mCountUpTimer != null) {
            mCountUpTimer.cancel();
        }

        mCountUpTimer = new CountUpTimer(1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCountedTime = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                CountUpTextView.this.setText(String.format(FORMAT, seconds / 60, seconds % 60));
            }
        };
        mCountUpTimer.start();
    }

    /**
     * Stop to count.
     */
    public void stop() {
        if (mCountUpTimer == null) {
            return;
        }

        mCountUpTimer.cancel();
    }
}
