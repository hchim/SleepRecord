package im.hch.sleeprecord.utils;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * Created by huiche on 2/24/17.
 */
public abstract class CountUpTimer {

    private final long interval;
    private long base;

    public CountUpTimer(long intervalInMillis) {
        this.interval = intervalInMillis;
    }

    public void start() {
        base = SystemClock.elapsedRealtime();
        handler.sendMessage(handler.obtainMessage(MSG));
    }

    public void cancel() {
        handler.removeMessages(MSG);
    }

    public void reset() {
        synchronized (this) {
            base = SystemClock.elapsedRealtime();
        }
    }

    abstract public void onTick(long elapsedTime);

    private static final int MSG = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (CountUpTimer.this) {
                long elapsedTime = SystemClock.elapsedRealtime() - base;
                onTick(elapsedTime);
                sendMessageDelayed(obtainMessage(MSG), interval);
            }
        }
    };
}
