package im.hch.sleeprecord;

import android.app.Application;
import android.util.Log;

import im.hch.sleeprecord.utils.MetricHelper;

public class SleepRecordApplication extends Application {
    private MetricHelper metricHelper;
    private static final String APP_START = Constants.APP_NAME + ":Start";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Constants.APP_NAME, "Start SleepRecord app.");
        metricHelper = new MetricHelper(getBaseContext());
        metricHelper.increaseCounter(APP_START);
    }
}
