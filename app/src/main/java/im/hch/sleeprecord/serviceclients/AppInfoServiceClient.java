package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import com.sleepaiden.androidcommonutils.config.AppConfig;
import com.sleepaiden.androidcommonutils.exceptions.ConnectionFailureException;
import com.sleepaiden.androidcommonutils.exceptions.InternalServerException;
import com.sleepaiden.androidcommonutils.service.BaseServiceClient;

import org.json.JSONException;
import org.json.JSONObject;

import im.hch.sleeprecord.models.ApplicationConfig;

public class AppInfoServiceClient extends BaseServiceClient {

    public static final String ADD_SUGGESTION_URL = EndPoints.APP_INFO_SERVICE_ENDPOINT + "/suggestions";
    public static final String APP_CONFIG_URL = EndPoints.APP_INFO_SERVICE_ENDPOINT + "/confs";

    public AppInfoServiceClient(AppConfig appConfig) {
        super(appConfig);
    }

    @Override
    protected String getPath(String url) {
        return url.replace(EndPoints.APP_INFO_SERVICE_ENDPOINT, "");
    }

    public ApplicationConfig retrieveAppConfig() throws ConnectionFailureException, InternalServerException {
        JSONObject object = appConfig.getAppJSON();

        try {
            JSONObject result = post(APP_CONFIG_URL, object);
            handleGeneralErrors(result, true);
            return new ApplicationConfig(result);
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
        JSONObject object = appConfig.getAppJSON();
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
