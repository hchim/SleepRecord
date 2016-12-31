package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
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

    /**
     * Send the get, post, put, delete request.
     * @param request
     * @return
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    private JSONObject sendRequest(Request request)
            throws ConnectionFailureException, InternalServerException {
        try {
            Response response = httpClient.newCall(request).execute();
            String resBody = response.body().string();
            JSONObject jsonObj = new JSONObject(resBody);
            return jsonObj;
        } catch (IOException e) {
            Log.e(TAG, "Failed to submit the post request.", e);
            throw new ConnectionFailureException();
        } catch (JSONException ex) {
            Log.e(TAG, "Illegal response.", ex);
            throw new InternalServerException();
        }
    }

    /**
     * Submit a delete request.
     * @param url
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public JSONObject delete(String url)
            throws InternalServerException, ConnectionFailureException {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        return sendRequest(request);
    }

    /**
     * Submit a get request.
     * @param url
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public JSONObject get(String url)
            throws InternalServerException, ConnectionFailureException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return sendRequest(request);
    }

    /**
     * Submit a post request.
     * @param url
     * @param object
     * @return
     * @throws InternalServerException response has a wrong format.
     * @throws ConnectionFailureException when failed to connect to the server.
     */
    public JSONObject post(String url, JSONObject object)
            throws InternalServerException, ConnectionFailureException {
        RequestBody body = RequestBody.create(JSON, object.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        return sendRequest(request);
    }

    /**
     * Submit a put request.
     * @param url
     * @param object
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public JSONObject put(String url, JSONObject object)
            throws InternalServerException, ConnectionFailureException {
        RequestBody body = RequestBody.create(JSON, object.toString());
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        return sendRequest(request);
    }
}
