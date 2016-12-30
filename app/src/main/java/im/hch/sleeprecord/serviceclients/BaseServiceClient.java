package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by huiche on 11/11/16.
 */

public class BaseServiceClient {
    public static final String TAG = "BaseServiceClient";

    public static final String ERROR_MESSAGE_KEY = "message";
    public static final String ERROR_CODE_KEY = "errorCode";
    public static final String ERROR_CODE_INTERNAL_FAILURE = "INTERNAL_FAILURE";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    protected OkHttpClient httpClient;

    public BaseServiceClient() {
        httpClient = new OkHttpClient();
    }

    public JSONObject post(String url, JSONObject object) {
        RequestBody body = RequestBody.create(JSON, object.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            String resBody = response.body().string();
            JSONObject jsonObj = new JSONObject(resBody);
            return jsonObj;
        } catch (IOException e) {
            Log.e(TAG, "Failed to submit the post request.");
            return null;
        } catch (JSONException ex) {
            Log.e(TAG, "Illegal response.");
            return null;
        }
    }

}
