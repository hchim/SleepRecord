package im.hch.sleeprecord.activities;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.sleepaiden.androidcommonutils.ResourceUtils;
import com.sleepaiden.androidcommonutils.media.AudioService;

import im.hch.sleeprecord.Metrics;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.utils.MetricHelper;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {
    protected SessionManager sessionManager;
    protected SharedPreferenceUtil sharedPreferenceUtil;
    protected MetricHelper metricHelper;
    protected MainActivity mainActivity;
    protected NativeExpressAdView adView;
    protected LayoutInflater mInflater;

    private String adId;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        adId = getString(R.string.admob_id_main_activity);
        mInflater = inflater;

        sessionManager = new SessionManager(getActivity());
        sharedPreferenceUtil = new SharedPreferenceUtil(getActivity());
        metricHelper = new MetricHelper(getActivity());
        mainActivity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
        return super.onCreateView(mInflater, container, savedInstanceState);
    }

    protected void setupAdView(final View root, final LinearLayout adViewWidget) {
        adView = new NativeExpressAdView(mainActivity);
        adView.setAdUnitId(adId);
        adViewWidget.addView(adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                metricHelper.increaseCounter(Metrics.HOME_FRAGMENT_AD_LOAD_FAILURE);
            }

            @Override
            public void onAdLoaded() {
                adViewWidget.setVisibility(View.VISIBLE);
                metricHelper.increaseCounter(Metrics.HOME_FRAGMENT_AD_LOADED);
            }
        });

        root.post(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                float density = ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
                int adWidthDP = (int) (root.getWidth() / density);
                int adHeightDP = (int) (adWidthDP / 4.0); // the width and height of the native ad defines 4.0
                adView.setAdSize(new AdSize(adWidthDP, adHeightDP));
                loadAd();
            }
        });
    }

    /**
     * Load AD.
     */
    private void loadAd() {
        AdRequest.Builder builder = new AdRequest.Builder();
        adView.loadAd(builder.build());
    }

    protected void playAudio(int resId, AudioService.PlayMode playMode, int playTime) {
        if (mainActivity.audioService == null) {
            return;
        }
        mainActivity.audioService.playAudio(ResourceUtils.getResourceUri(mainActivity, resId), playMode, playTime);
    }

    protected void pauseAudio() {
        if (mainActivity.audioService == null) {
            return;
        }
        mainActivity.audioService.pauseAudio();
    }

    protected void resumeAudio() {
        if (mainActivity.audioService == null) {
            return;
        }
        mainActivity.audioService.resumeAudio();
    }

    protected void stopAudio() {
        if (mainActivity.audioService == null) {
            return;
        }
        mainActivity.audioService.stopAudio();
    }

    protected void resetPlayTime(int playTime) {
        if (mainActivity.audioService == null) {
            return;
        }

        mainActivity.audioService.resetTimer(playTime);
    }

    protected AudioService.PlayStatus getPlayStatus() {
        if (mainActivity.audioService == null) {
            return null;
        }

        return mainActivity.audioService.getStatus();
    }
}
