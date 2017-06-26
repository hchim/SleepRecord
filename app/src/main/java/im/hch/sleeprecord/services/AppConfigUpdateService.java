package im.hch.sleeprecord.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileOutputStream;

import im.hch.sleeprecord.MyAppConfig;
import im.hch.sleeprecord.models.ApplicationConfig;
import im.hch.sleeprecord.serviceclients.AppInfoServiceClient;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class AppConfigUpdateService extends IntentService {

    private static final String TAG = "AppConfigUpdateService";
    public static final String SPLASH_IMAGE_URL = "SplashImageURL";
    public static final String SPLASH_IMAGE_Location = "SplashImageLocation";
    public static final String SPLASH_IMAGE_NAME = "splash.png";

    // Retrieve splash image.
    private static final String ACTION_UPDATE_SPLASH_IMAGE = "im.hch.sleeprecord.services.action.UPDATE_SPLASH_IMAGE";

//    private static final String EXTRA_PARAM1 = "im.hch.sleeprecord.services.extra.PARAM1";

    private AppInfoServiceClient appConfigServiceClient;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private ApplicationConfig appConfig;
    final OkHttpClient okHttpClient;

    public AppConfigUpdateService() {
        super("AppConfigUpdateService");
        this.appConfigServiceClient = new AppInfoServiceClient(MyAppConfig.getAppConfig());
        this.okHttpClient = new OkHttpClient();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateImage(Context context) {
        Intent intent = new Intent(context, AppConfigUpdateService.class);
        intent.setAction(ACTION_UPDATE_SPLASH_IMAGE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.sharedPreferenceUtil = new SharedPreferenceUtil(this);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_SPLASH_IMAGE.equals(action)) {
                handleActionUpdateImage();
            }
        }
    }

    /**
     * Handle action UPDATE_IMAGE in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateImage() {
        try {
            appConfig = appConfigServiceClient.retrieveAppConfig();
            if (appConfig != null && appConfig.getSplashImageUrl() != null) {
                String localSplashImageUrl = sharedPreferenceUtil.getString(SPLASH_IMAGE_URL, null);

                if (localSplashImageUrl == null || !localSplashImageUrl.equals(appConfig.getSplashImageUrl())) {
                    Request request = new Request.Builder().url(appConfig.getSplashImageUrl()).build();
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        if (response.isSuccessful()) {
                            Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                            saveSplashImage(bitmap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to download splash image.", e);
                    }
                }
            } else {
                Log.w(TAG, "Failed to update app splash image.");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void saveSplashImage(Bitmap bitmap) {
        try {
            FileOutputStream fileOutputStream = this.openFileOutput(SPLASH_IMAGE_NAME, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
            // update shared preference
            sharedPreferenceUtil.setValue(SPLASH_IMAGE_URL, appConfig.getSplashImageUrl());
            sharedPreferenceUtil.setValue(SPLASH_IMAGE_Location, SPLASH_IMAGE_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save splash image.", e);
        }
    }
}
