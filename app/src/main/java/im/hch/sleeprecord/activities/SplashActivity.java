package im.hch.sleeprecord.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

public class SplashActivity extends AppCompatActivity {
    private static final int DELAY_MILLIS = 3000;
    public static final String SPLASH_IMAGE = "SplashImage";

    @BindView(R.id.fullscreen_imageview) ImageView mImageView;
    @BindView(R.id.fullscreen_textview) TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        SharedPreferenceUtil sharedPreferenceUtil = new SharedPreferenceUtil(this);
        String splashImage = (String) sharedPreferenceUtil.getValue(SPLASH_IMAGE, null);
        if (splashImage != null) {
            //hide text view
            mTextView.setVisibility(View.GONE);
            //show image view
            //TODO set image
        }

        startSplashTimer();
    }

    private void startSplashTimer() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, DELAY_MILLIS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}