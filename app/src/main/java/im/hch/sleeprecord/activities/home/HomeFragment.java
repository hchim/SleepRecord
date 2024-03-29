package im.hch.sleeprecord.activities.home;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sleepaiden.androidcommonutils.exceptions.AuthFailureException;
import com.sleepaiden.androidcommonutils.exceptions.InternalServerException;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.Metrics;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.BaseFragment;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.activities.records.SleepRecordsAdapter;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.SleepQuality;
import im.hch.sleeprecord.models.SleepRecordsPerDay;
import im.hch.sleeprecord.models.SleepTrainingPlan;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.ImageUtils;
import im.hch.sleeprecord.utils.SleepRecordUtils;
import im.hch.sleeprecord.utils.ViewUtils;

public class HomeFragment extends BaseFragment
        implements VerifyEmailDialogFragment.OnEmailVerifiedListener {

    @BindView(R.id.list_view) ListView sleepRecordListView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.verify_email_layout) View verifyEmailView;
    @BindView(R.id.verifyEmailTextView) TextView verifyEmailTextView;
    @BindView(R.id.sleepQualityTrend) SleepQualityTrendView sleepQualityTrendView;
    @BindView(R.id.adWidget) LinearLayout adWidgetView;

    @BindString(R.string.app_name) String title;

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
        setupAdView(view, adWidgetView);

        loadCachedSleepRecords();
        loadCachedSleepQualityTrend();
        if (!mainActivity.isRemoteDataLoaded()) {
            loadRemoteData();
        }

        return view;
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
    public static final int TREND_SHOW_SLEEP_RECORDS_NUM = 30;

    /**
     * Load sleep records from shared preference.
     */
    private void loadCachedSleepRecords() {
        List<SleepRecordsPerDay> records = sharedPreferenceUtil.retrieveSleepRecords();
        List<SleepRecordsPerDay> newRecords = new ArrayList<>();
        if (records != null && records.size() > SHOW_SLEEP_RECORDS_NUM) {
            newRecords = records.subList(0, SHOW_SLEEP_RECORDS_NUM);
        }

        Calendar to = Calendar.getInstance();
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1);

        sleepRecordsAdapter = new SleepRecordsAdapter(mainActivity,
                SleepRecordUtils.fillSleepRecords(newRecords, from.getTime(), to.getTime()));
        sleepRecordListView.setAdapter(sleepRecordsAdapter);
        ViewUtils.setListViewHeightBasedOnChildren(sleepRecordListView);
    }

    /**
     * Init sleep quality trend view.
     */
    private void loadCachedSleepQualityTrend() {
        List<SleepRecordsPerDay> records = sharedPreferenceUtil.retrieveSleepRecords();
        if (records == null) {
            records = new ArrayList<>();
        }

        ArrayList<SleepQuality> sleepQualities = new ArrayList<>();
        Calendar to = Calendar.getInstance();
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, TREND_SHOW_SLEEP_RECORDS_NUM * -1);
        records = SleepRecordUtils.fillSleepRecords(records, from.getTime(), to.getTime());

        for (int i = records.size() - 1; i >= 0; i--) {
            SleepRecordsPerDay record = records.get(i);
            sleepQualities.add(
                    new SleepQuality(record.getDateTime().getTime(), record.getSleepQuality()));
        }

        sleepQualityTrendView.setSleepQualities(sleepQualities);
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
        List<SleepRecordsPerDay> sleepRecords;
        SleepTrainingPlan sleepTrainingPlan;
        boolean reloadHeaderImage = false;
        boolean authFailure = false;
        boolean loadBabyInfoHasException = false;
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
                loadBabyInfoHasException = false;
            } catch (AuthFailureException e) {
                authFailure = true;
                loadBabyInfoHasException = true;
                return false;
            } catch (Exception e) {
                loadBabyInfoHasException = true;
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
            from.add(Calendar.DATE, TREND_SHOW_SLEEP_RECORDS_NUM * -1);

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
                    if (babyInfo == null && !loadBabyInfoHasException) {
                        DialogUtils.showEditBabyInfoDialog(getActivity().getFragmentManager(), babyInfo);
                    }
                    break;
                case USER_INFO_UPDATED:
                    mainActivity.updateUserInfo(userProfile);
                    //show email verify widget
                    if (userProfile != null && !userProfile.isEmailVerified()) {
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
                    HomeFragment.this.loadCachedSleepRecords();
                    HomeFragment.this.loadCachedSleepQualityTrend();
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
}
