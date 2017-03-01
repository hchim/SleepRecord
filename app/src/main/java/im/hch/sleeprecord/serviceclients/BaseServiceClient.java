package im.hch.sleeprecord.serviceclients;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import im.hch.mapikey.messagesigner.MessageSigner;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.utils.DateUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BaseServiceClient {
    public static final String TAG = "BaseServiceClient";

    public static final String ERROR_MESSAGE_KEY = "message";
    public static final String ERROR_CODE_KEY = "errorCode";
    public static final String ERROR_CODE_INTERNAL_FAILURE = "INTERNAL_FAILURE";
    public static final String ERROR_AUTH_FAILURE = "AUTH_FAILURE";
    public static final String ERROR_UNKNOWN_USER = "UNKNOWN_USER";

    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String ACCESS_TOKEN = "x-auth-token";
    public static final String TIME_LABEL = "x-auth-time";
    public static final String REQUEST_DIGEST = "x-auth-digest";

    protected OkHttpClient httpClient;
    private MessageSigner messageSigner;

    public BaseServiceClient() {
        httpClient = new OkHttpClient();
        messageSigner = MessageSigner.getInstance();
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
        return delete(url, null);
    }

    public JSONObject delete(String url, Map<String, String> headers)
            throws InternalServerException, ConnectionFailureException {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .delete();
        headers = addHeaders(builder, headers);
        //sign request
        String signature = messageSigner.generateSignature("post", url, null, headers);
        if (signature == null) {
            Log.wtf(TAG, "Failed to sign message");
        }
        builder.header(REQUEST_DIGEST, signature);
        return sendRequest(builder.build());
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
        return get(url, null);
    }

    /**
     * Submit a get request
     * @param url
     * @param headers
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public JSONObject get(String url, Map<String, String> headers)
            throws InternalServerException, ConnectionFailureException {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .get();
        headers = addHeaders(builder, headers);
        //sign request
        String signature = messageSigner.generateSignature("get", url, null, headers);
        if (signature == null) {
            Log.wtf(TAG, "Failed to sign message");
        }
        builder.header(REQUEST_DIGEST, signature);
        return sendRequest(builder.build());
    }

    /**
     * Submit a post request.
     * @param url
     * @param object
     * @param headers
     * @return
     * @throws InternalServerException response has a wrong format.
     * @throws ConnectionFailureException when failed to connect to the server.
     */
    public JSONObject post(String url, JSONObject object, Map<String, String> headers)
            throws InternalServerException, ConnectionFailureException {
        String bodyStr = encodingBody(object);
        RequestBody body = RequestBody.create(JSON, bodyStr);
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);
        headers = addHeaders(builder, headers);
        //sign request
        String signature = messageSigner.generateSignature("post", url, bodyStr, headers);
        if (signature == null) {
            Log.wtf(TAG, "Failed to sign message");
        }
        builder.header(REQUEST_DIGEST, signature);
        return sendRequest(builder.build());
    }

    /**
     * Submit a post request.
     * @param url
     * @param object
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public JSONObject post(String url, JSONObject object)
            throws InternalServerException, ConnectionFailureException {
        return post(url, object, null);
    }

    /**
     * Submit a put request.
     * @param url
     * @param object
     * @param headers
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public JSONObject put(String url, JSONObject object, Map<String, String> headers)
            throws InternalServerException, ConnectionFailureException {
        String objectStr = encodingBody(object);
        RequestBody body = RequestBody.create(JSON, objectStr);
        Request.Builder builder = new Request.Builder()
                .url(url)
                .put(body);
        headers = addHeaders(builder, headers);
        //sign request
        String signature = messageSigner.generateSignature("put", url, objectStr, headers);
        if (signature == null) {
            Log.wtf(TAG, "Failed to sign message");
        }
        builder.header(REQUEST_DIGEST, signature);
        return sendRequest(builder.build());
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
        return put(url, object, null);
    }

    /**
     * Upload an image.
     * @param url
     * @param imagePath
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public JSONObject uploadImage(String url, String imagePath, Map<String, String> headers)
            throws InternalServerException, ConnectionFailureException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "header_icon.jpg",
                        RequestBody.create(MEDIA_TYPE_PNG, new File(imagePath)))
                .build();

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(requestBody);
        headers = addHeaders(builder, headers);
        //sign request
        String signature = messageSigner.generateSignature("post", url, null, headers);
        if (signature == null) {
            Log.wtf(TAG, "Failed to sign message");
        }
        builder.header(REQUEST_DIGEST, signature);

        return sendRequest(builder.build());
    }

    /**
     * add headers to the request builder
     * @param builder
     * @param headers
     */
    private Map<String, String> addHeaders(Request.Builder builder, Map<String, String> headers) {
        if (headers == null)  {
            headers = new HashMap<>();
        }
        headers.put(TIME_LABEL, DateUtils.dateToStr(new Date(System.currentTimeMillis()), DATE_FORMAT));

        for (String key: headers.keySet()) {
            builder.header(key, headers.get(key));
        }

        return headers;
    }

    /**
     * Encoding the request body.
     * @param object
     * @return
     */
    private String encodingBody(JSONObject object) {
        if (object == null) {
            return null;
        }

        JSONObject encodedBody = new JSONObject();
        String payload = Base64.encodeToString(object.toString().getBytes(), Base64.DEFAULT);
        //TODO encrypt payload
        try {
            encodedBody.put("payload", payload);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return encodedBody.toString();
    }
}
