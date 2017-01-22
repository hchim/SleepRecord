package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import im.hch.sleeprecord.Constants;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.utils.MetricHelper.MetricType;

public class MetricServiceClient extends BaseServiceClient {

    public static final String ADD_METRIC_URL = EndPoints.METRIC_SERVICE_ENDPOINT + "metrics";

    private void addMetric(String tag, MetricType type, int val, String message, String ip)
            throws ConnectionFailureException, InternalServerException {
        JSONObject object = Constants.getAppJSON();
        try {
            object.put("tag", tag);
            object.put("hostname", ip);
            switch (type) {
                case MESSAGE:
                case ERROR:
                    object.put("message", message);
                    break;
                case COUNT:
                    object.put("count", val);
                    break;
                case TIME:
                    object.put("time", val);
                    break;
            }

            JSONObject result = post(ADD_METRIC_URL, object);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }
                throw new InternalServerException();
            }
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Add count metric.
     * @param tag
     * @param count
     * @param ip
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public void addCountMetric(String tag, int count, String ip)
            throws ConnectionFailureException, InternalServerException {
        addMetric(tag, MetricType.COUNT, count, null, ip);
    }

    /**
     * Add time metric.
     * @param tag
     * @param time
     * @param ip
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public void addTimeMetric(String tag, int time, String ip)
            throws ConnectionFailureException, InternalServerException {
        addMetric(tag, MetricType.TIME, time, null, ip);
    }

    /**
     * Add message metric.
     * @param tag
     * @param message
     * @param ip
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public void addMessageMetric(String tag, String message, String ip)
            throws ConnectionFailureException, InternalServerException {
        addMetric(tag, MetricType.MESSAGE, 0, message, ip);
    }

    /**
     * Add error metric.
     * @param tag
     * @param error
     * @param ip
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public void addErrorMetric(String tag, String error, String ip)
            throws ConnectionFailureException, InternalServerException {
        addMetric(tag, MetricType.ERROR, 0, error, ip);
    }
}
