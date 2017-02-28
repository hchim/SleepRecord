package im.hch.sleeprecord.activities.home;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.Metrics;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.BaseFragment;
import im.hch.sleeprecord.activities.main.AddRecordDialogFragment;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.activities.records.SleepRecordsAdapter;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.SleepQuality;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.models.SleepTrainingPlan;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.exceptions.AuthFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.ImageUtils;
import im.hch.sleeprecord.utils.SleepRecordUtils;
import im.hch.sleeprecord.utils.ViewUtils;

public class HomeFragment extends BaseFragment implements AddRecordDialogFragment.AddRecordDialogListener,
        VerifyEmailDialogFragment.OnEmailVerifiedListener{

    @BindView(R.id.list_view) ListView sleepRecordListView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.verify_email_layout) View verifyEmailView;
    @BindView(R.id.verifyEmailTextView) TextView verifyEmailTextView;
    @BindView(R.id.sleepQualityTrend) SleepQualityTrendView sleepQualityTrendView;
    @BindView(R.id.adWidget) LinearLayout adWidgetView;
    NativeExpressAdView adView;

    @BindString(R.string.app_name) String title;
    @BindString(R.string.admob_id_main_activity) String adId;

    private SleepRecordsAdapter sleepRecordsAdapter;

    public HomeFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    /*
     ***********************************************************
     * Override methods
     ***********************************************************
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        mainActivity.setTitle(title);

        verifyEmailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showVerifyEmailDialog(getActivity().getFragmentManager());
            }
        });

        // Disallow the touch request for parent scroll on touch of child view
        sleepRecordListView.setClickable(false);

        //setup adview
        adView = new NativeExpressAdView(mainActivity);
        adView.setAdUnitId(adId);
        adWidgetView.addView(adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                metricHelper.increaseCounter(Metrics.HOME_FRAGMENT_AD_LOAD_FAILURE);
            }

            @Override
            public void onAdLoaded() {
                adWidgetView.setVisibility(View.VISIBLE);
                metricHelper.increaseCounter(Metrics.HOME_FRAGMENT_AD_LOADED);
            }
        });

        loadCachedSleepRecords();
        loadCachedSleepQualityTrend();
        if (!mainActivity.isRemoteDataLoaded()) {
            loadRemoteData();
        }

        view.post(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                float density = ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
                int adMarginDP = (int) (getResources().getDimension(R.dimen.activity_horizontal_margin) * 2 / density);
                int adWidthDP = (int) (view.getWidth() / density) - adMarginDP;
                int adHeightDP = (int) (adWidthDP / 4.0); // the width and height of the native ad defines 4.0
                adView.setAdSize(new AdSize(adWidthDP, adHeightDP));
                loadAd();
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_sleep_record) {
            DialogUtils.showAddRecordDialog(getFragmentManager(), this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     ***********************************************************
     * Helper methods
     ***********************************************************
     */

    /**
     * The number of sleep records to show in the sleep records widget.
     */
    public static final int SHOW_SLEEP_RECORDS_NUM = 5;

    /**
     * Load sleep records from shared preference.
     */
    private void loadCachedSleepRecords() {
        List<SleepRecord> records = sharedPreferenceUtil.retrieveSleepRecords();
        if (records == null) {
            records = new ArrayList<>();
        }

        Calendar to = Calendar.getInstance();
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1);

        sleepRecordsAdapter = new SleepRecordsAdapter(mainActivity,
                SleepRecordUtils.fillSleepRecords(records, from.getTime(), to.getTime()));
        sleepRecordListView.setAdapter(sleepRecordsAdapter);
        ViewUtils.setListViewHeightBasedOnChildren(sleepRecordListView);
    }

    /**
     * Init sleep quality trend view.
     */
    private void loadCachedSleepQualityTrend() {
        //TODO init sleep quality trend view
        ArrayList<SleepQuality> sleepQualities = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -30);
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 30; i++) {
            sleepQualities.add(new SleepQuality(calendar.getTime(), 10 - random.nextInt(4)));
            calendar.add(Calendar.DATE, 1);
        }
        sleepQualityTrendView.setSleepQualities(sleepQualities);
    }

    /**
     * Load AD.
     */
    private void loadAd() {
        AdRequest.Builder builder = new AdRequest.Builder();
        adView.loadAd(builder.build());
    }

    /**
     * Load the remote data.
     * 1. Baby Info.
     * 2. Sleep Record.
     */
    private void loadRemoteData() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        new LoadRemoteDataTask().execute();
    }

    /*
    ***********************************************************
    * Listener callback methods
    ***********************************************************
    */

    @Override
    public void onSleepRecordSaved(Date from, Date to) {
        new LoadSleepRecordTask().execute();
    }

    @Override
    public void onEmailVerified() {
        verifyEmailView.setVisibility(View.GONE);
    }

    /*
     ***********************************************************
     * Async tasks
     ***********************************************************
     */

    /**
     * Update baby info task.
     */
    private class LoadRemoteDataTask extends AsyncTask<Void, Integer, Boolean> {

        BabyInfo babyInfo;
        UserProfile userProfile;
        List<SleepRecord> sleepRecords;
        SleepTrainingPlan sleepTrainingPlan;
        boolean reloadHeaderImage = false;
        boolean authFailure = false;

        public static final int BABY_INFO_UPDATED = 30;
        public static final int USER_INFO_UPDATED = 60;
        public static final int TRAINING_PLAN_UPDATED = 80;
        public static final int SLEEP_RECORDS_UPDATED = 100;

        @Override
        protected Boolean doInBackground(Void... params) {
            String userId = sessionManager.getUserId();
            if (userId == null) {
                Log.wtf(MainActivity.TAG, "User id is null.");
                return false;
            }

            //update baby info
            try {
                babyInfo = mainActivity.sleepServiceClient.getBabyInfo();
                sharedPreferenceUtil.storeBabyInfo(babyInfo);
            } catch (AuthFailureException e) {
                authFailure = true;
                return false;
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
                if (e instanceof InternalServerException) {
                    metricHelper.errorMetric(Metrics.GET_BABY_INFO_ERROR_METRIC, e);
                }
            }
            publishProgress(BABY_INFO_UPDATED);

            try {
                userProfile = mainActivity.identityServiceClient.getUser();

                String currentHeaderUrl = sharedPreferenceUtil.retrieveHeaderImageUrl();
                String headerImagePath = sharedPreferenceUtil.retrieveHeaderImage();
                sharedPreferenceUtil.storeUserProfile(userProfile);
                //download header image the url of the header
                if (userProfile.getHeaderIconUrl() != null) {
                    if (headerImagePath == null
                            || !userProfile.getHeaderIconUrl().equals(currentHeaderUrl)) {
                        String imagePath = ImageUtils.downloadImage(
                                HomeFragment.this.getActivity(), userProfile.getHeaderIconUrl());
                        if (imagePath != null) {
                            userProfile.setHeaderIconPath(imagePath);
                            sharedPreferenceUtil.storeHeaderImage(imagePath);
                            reloadHeaderImage = true;
                        }
                    }
                }
            } catch (AuthFailureException e) {
                authFailure = true;
                return false;
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
                if (e instanceof InternalServerException) {
                    metricHelper.errorMetric(Metrics.GET_USER_ERROR_METRIC, e);
                }
            }
            publishProgress(USER_INFO_UPDATED);
            //update sleep training plan
            try {
                sleepTrainingPlan = mainActivity.sleepServiceClient.getSleepTrainingPlan();
                if (sleepTrainingPlan != null) {
                    sharedPreferenceUtil.storeSleepTrainingPlan(sleepTrainingPlan);
                }
            } catch (AuthFailureException e) {
                authFailure = true;
                return false;
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
                if (e instanceof InternalServerException) {
                    metricHelper.errorMetric(Metrics.GET_TRAINING_PLAN_ERROR_METRIC, e);
                }
            }
            publishProgress(TRAINING_PLAN_UPDATED);
            //update sleep records
            Calendar to = Calendar.getInstance();
            Calendar from = Calendar.getInstance();
            from.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1);

            try {
                sleepRecords = mainActivity.sleepServiceClient.getSleepRecords(from.getTime(), to.getTime());
                sharedPreferenceUtil.storeSleepRecords(sleepRecords, userId);
            }  catch (AuthFailureException e) {
                authFailure = true;
                return false;
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
                if (e instanceof InternalServerException) {
                    metricHelper.errorMetric(Metrics.GET_SLEEP_RECORDS_ERROR_METRIC, e);
                }
            }
            publishProgress(SLEEP_RECORDS_UPDATED);

            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0]) {
                case BABY_INFO_UPDATED:
                    mainActivity.updateBabyInfo(babyInfo);
                    if (babyInfo == null) {
                        DialogUtils.showEditBabyInfoDialog(getActivity().getFragmentManager(), babyInfo);
                    }
                    break;
                case USER_INFO_UPDATED:
                    mainActivity.updateUserInfo(userProfile);
                    //show email verify widget
                    if (!userProfile.isEmailVerified()) {
                        verifyEmailView.setVisibility(View.VISIBLE);
                    }

                    if (reloadHeaderImage && mainActivity != null) {
                        Picasso.with(HomeFragment.this.getActivity())
                                .load(new File(userProfile.getHeaderIconPath()))
                                .into(mainActivity.getHeaderViewHolder().headerImage);
                    }
                    break;
                case TRAINING_PLAN_UPDATED:
                    //nothing to do
                    break;
                case SLEEP_RECORDS_UPDATED:
                    sleepRecordsAdapter.updateSleepRecords(sleepRecords);
                    break;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                progressBar.setProgress(values[0]);
            } else {
                progressBar.setProgress(values[0], true);
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (authFailure) {
                ActivityUtils.navigateToLoginActivity(mainActivity);
                return;
            }
            mainActivity.setRemoteDataLoaded(true);
            progressBar.setVisibility(View.GONE);
            metricHelper.stopTimeMetric(Metrics.MAIN_ACTIVITY_LOADING_TIME_METRIC);
        }

        @Override
        protected void onPreExecute() {
            metricHelper.startTimeMetric(Metrics.MAIN_ACTIVITY_LOADING_TIME_METRIC);
        }
    }

    private class LoadSleepRecordTask extends AsyncTask<Void, Integer, Boolean> {
        List<SleepRecord> sleepRecords;
        boolean authFailure = false;

        @Override
        protected Boolean doInBackground(Void... params) {
            String userId = sessionManager.getUserId();
            if (userId == null) {
                Log.wtf(MainActivity.TAG, "User id is null.");
                return false;
            }

            //update sleep records
            Calendar to = Calendar.getInstance();
            Calendar from = Calendar.getInstance();
            from.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1);

            try {
                sleepRecords = mainActivity.sleepServiceClient.getSleepRecords(from.getTime(), to.getTime());
                sharedPreferenceUtil.storeSleepRecords(sleepRecords, userId);
            } catch (AuthFailureException e) {
                authFailure = true;
                return false;
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
                if (e instanceof InternalServerException) {
                    metricHelper.errorMetric(Metrics.GET_SLEEP_RECORDS_ERROR_METRIC, e);
                }
            }

            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                sleepRecordsAdapter.updateSleepRecords(sleepRecords);
            } else if (authFailure) {
                ActivityUtils.navigateToLoginActivity(mainActivity);
            }
        }
    }
}
