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

    public AppConfig retrieveAppConfig(String packageName) {
        //TODO implement

        AppConfig appConfig = new AppConfig();
        appConfig.setSplashImageUrl("https://s-media-cache-ak0.pinimg.com/236x/ab/19/55/ab195529d317cd8d107ba3b87a4297eb.jpg");

        return appConfig;
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
}
