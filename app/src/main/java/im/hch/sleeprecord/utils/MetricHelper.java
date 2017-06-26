package im.hch.sleeprecord.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import im.hch.sleeprecord.MyAppConfig;
import im.hch.sleeprecord.serviceclients.MetricServiceClient;

public class MetricHelper {
    public static final String TAG = "MetricHelper";
    public static final int ERROR_SIZE = 4096;
    public enum MetricType {
        COUNT("count"), TIME("time"), ERROR("error"), MESSAGE("msg");

        private String type;

        MetricType(String type) {
            this.type = type;
        };

        @Override
        public String toString() {
            return type;
        }
    }

    private MetricServiceClient metricServiceClient;
    private HashMap<String, Long> metricMap;
    private Context context;

    public MetricHelper(Context context) {
        this.context = context;
        metricServiceClient = new MetricServiceClient(MyAppConfig.getAppConfig());
        metricMap = new HashMap<>();
    }

    /**
     * Add a counter metric.
     * @param tag
     */
    public void increaseCounter(String tag) {
        new SendMetricAsyncTask(tag, MetricType.COUNT, 1).execute();
    }

    /**
     * Add an error metric.
     * @param tag
     * @param error
     */
    public void errorMetric(String tag, String error) {
        new SendMetricAsyncTask(tag, MetricType.ERROR, error).execute();
    }

    /**
     * Add an error metric.
     * @param tag
     * @param e
     */
    public void errorMetric(String tag, Exception e) {
        StringWriter writer = new StringWriter(ERROR_SIZE);
        e.printStackTrace(new PrintWriter(writer));
        new SendMetricAsyncTask(tag, MetricType.ERROR, writer.toString()).execute();
    }

    /**
     * Add a message metric.
     * @param tag
     * @param message
     */
    public void messageMetric(String tag, String message) {
        new SendMetricAsyncTask(tag, MetricType.MESSAGE, message).execute();
    }

    /**
     * Start a time metric.
     * startTimeMetric and stopTimeMetric should be used in pairs.
     * @param tag
     */
    public void startTimeMetric(String tag) {
        metricMap.put(tag, System.currentTimeMillis());
    }

    /**
     * Stop a time metric and submit it.
     * @param tag
     */
    public void stopTimeMetric(String tag) {
        if (!metricMap.containsKey(tag)) {
            return;
        }

        long time = System.currentTimeMillis() - metricMap.get(tag);
        new SendMetricAsyncTask(tag, MetricType.TIME, (int) time).execute();
    }

    private class SendMetricAsyncTask extends AsyncTask<Void, Void, Void> {

        private String tag;
        private MetricType type;
        private int val;
        private String message;

        public SendMetricAsyncTask(String tag, MetricType type, int val) {
            this.tag = tag;
            this.type = type;
            this.val = val;
        }

        public SendMetricAsyncTask(String tag, MetricType type, String message) {
            this.tag = tag;
            this.type = type;
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String ipAddress = NetworkUtils.getIPAddress(context);
                switch (type) {
                    case COUNT:
                        metricServiceClient.addCountMetric(tag, val, ipAddress);
                        break;
                    case TIME:
                        metricServiceClient.addTimeMetric(tag, val, ipAddress);
                        break;
                    case ERROR:
                        metricServiceClient.addErrorMetric(tag, message, ipAddress);
                        break;
                    case MESSAGE:
                        metricServiceClient.addMessageMetric(tag, message, ipAddress);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

            return null;
        }
    }
}
