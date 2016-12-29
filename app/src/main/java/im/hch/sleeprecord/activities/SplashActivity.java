package im.hch.sleeprecord.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.services.AppConfigUpdateService;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

/**
 * SplashActivity. Currently not used.
 */
public class SplashActivity extends AppCompatActivity {
    private static final int DELAY_MILLIS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ButterKnife.bind(this);
//
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
                    ActivityUtils.navigateToMainActivity(SplashActivity.this);
                }
            }, DELAY_MILLIS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}