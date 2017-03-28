package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import im.hch.sleeprecord.Constants;
import im.hch.sleeprecord.models.AppConfig;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;

public class AppInfoServiceClient extends BaseServiceClient {

    public static final String ADD_SUGGESTION_URL = EndPoints.APP_INFO_SERVICE_ENDPOINT + "/suggestions";
    public static final String APP_CONFIG_URL = EndPoints.APP_INFO_SERVICE_ENDPOINT + "/confs";

    @Override
    protected String getPath(String url) {
        return url.replace(EndPoints.APP_INFO_SERVICE_ENDPOINT, "");
    }

    public AppConfig retrieveAppConfig() throws ConnectionFailureException, InternalServerException {
        JSONObject object = Constants.getAppJSON();

        try {
            JSONObject result = post(APP_CONFIG_URL, object);
            handleGeneralErrors(result, true);
            return new AppConfig(result);
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error", e);
            throw new InternalServerException();
        }
    }

    /**
     * Add a suggestion.
     * @param userId
     * @param suggestion
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public void addSuggestion(String userId, String suggestion)
            throws ConnectionFailureException, InternalServerException {
        JSONObject object = Constants.getAppJSON();
        try {
            object.put("userId", userId);
            object.put("message", suggestion);

            JSONObject result = post(ADD_SUGGESTION_URL, object);
            handleGeneralErrors(result, true);
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error", ex);
            throw new InternalServerException();
        }
    }
}
