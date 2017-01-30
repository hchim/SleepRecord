package im.hch.sleeprecord.utils;

import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

/**
 * Created by huiche on 1/29/17.
 */

public class AdUtils {

    /**
     * Load an ad to the container.
     * @param container
     * @param adId
     * @param adHeight
     */
    public static void loadAd(ViewGroup container, String adId, int adHeight) {
        NativeExpressAdView adView = new NativeExpressAdView(container.getContext());
        container.addView(adView);
        adView.setAdSize(new AdSize(AdSize.FULL_WIDTH, adHeight));
        adView.setAdUnitId(adId);
        AdRequest.Builder builder = new AdRequest.Builder();
        adView.loadAd(builder.build());
    }
}
