package im.hch.sleeprecord.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import im.hch.sleeprecord.serviceclients.AppInfoServiceClient;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

/**
 * SplashActivity. Currently not used.
 */
public class SplashActivity extends AppCompatActivity {
    public static final String TAG = "SplashActivity";

    private static final int DELAY_MILLIS = 1000;

    private SessionManager sessionManager;
    private AppInfoServiceClient appConfigServiceClient;
    private SharedPreferenceUtil sharedPreferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(this);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        appConfigServiceClient = new AppInfoServiceClient();

//        SharedPreferenceUtil sharedPreferenceUtil = new SharedPreferenceUtil(this);
//        String splashImage = sharedPreferenceUtil.getString(AppConfigUpdateService.SPLASH_IMAGE_Location, null);
//
//        if (splashImage != null) {
//            //show image view
//            Uri uri = Uri.fromFile(new File(getFilesDir(), splashImage));
////            Picasso.with(this).load(uri).into(mImageView);
//        } else {
//        }
//
//        //update splash image
//        AppConfigUpdateService.startActionUpdateImage(this);

        startSplashTimer();
    }

    private void startSplashTimer() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
//                    try {
//                        AppConfig appConfig = appConfigServiceClient.retrieveAppConfig();
//                        sharedPreferenceUtil.storeAppConfig(appConfig);
//                    } catch (Exception e) {
//                        Log.e(TAG, e.getMessage(), e);
//                    }

                    if (!sessionManager.isLoggedIn()) {
                        ActivityUtils.navigateToLoginActivity(SplashActivity.this);
                    } else {
                        ActivityUtils.navigateToMainActivity(SplashActivity.this);
                    }
                }
            }, DELAY_MILLIS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}