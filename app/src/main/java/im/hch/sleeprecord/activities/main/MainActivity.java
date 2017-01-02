package im.hch.sleeprecord.activities.main;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.records.SleepRecordsAdapter;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.IdentityServiceClient;
import im.hch.sleeprecord.serviceclients.SleepServiceClient;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DateUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        BabyInfoDialogFragment.BabyInfoDialogFragmentListener,
        AddRecordDialogFragment.AddRecordDialogListener {
    public static final String TAG = "MainActivity";

    private SleepRecordsAdapter sleepRecordsAdapter;
    private SessionManager sessionManager;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private SleepServiceClient sleepServiceClient;
    private IdentityServiceClient identityServiceClient;
    private HeaderViewHolder headerViewHolder;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.list_view) ListView listView;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    @BindString(R.string.age_years_singlular) String AGE_YEARS_S;
    @BindString(R.string.age_months_singlular) String AGE_MONTHS_S;
    @BindString(R.string.age_days_singlular) String AGE_DAYS_S;
    @BindString(R.string.age_years_plural) String AGE_YEARS_P;
    @BindString(R.string.age_months_plural) String AGE_MONTHS_P;
    @BindString(R.string.age_days_plural) String AGE_DAYS_P;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        
        headerViewHolder = new HeaderViewHolder(navigationView.getHeaderView(0));

        sessionManager = new SessionManager(this);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        sleepServiceClient = new SleepServiceClient();
        identityServiceClient = new IdentityServiceClient();

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        if (!sessionManager.isLoggedIn()) {
            ActivityUtils.navigateToLoginActivity(this);
        } else {
            updateUserInfo(sharedPreferenceUtil.retrieveUserProfile());
        }

        BabyInfo babyInfo = sharedPreferenceUtil.retrieveBabyInfo();
        if (babyInfo == null) {
            DialogUtils.showEditBabyInfoDialog(getFragmentManager(), babyInfo);
        } else {
            updateBabyInfo(babyInfo);
        }

        //TODO replace these default data with real data
        List<Pair<Date, Date>> sleepTimes = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date begin, end;

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 15);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 20);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 33);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 20);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 30);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        //test data
        ArrayList<SleepRecord> records = new ArrayList<>();
        for (int i = 27; i >= 25; i--) {
            SleepRecord sleepRecord = new SleepRecord(11, i);
            sleepRecord.setSleepTimePairs(sleepTimes);
            records.add(sleepRecord);
        }

        sleepRecordsAdapter = new SleepRecordsAdapter(this, records);
        listView.setAdapter(sleepRecordsAdapter);

        headerViewHolder.babyNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showEditBabyInfoDialog(
                        MainActivity.this.getFragmentManager(),
                        sharedPreferenceUtil.retrieveBabyInfo());
            }
        });

        loadRemoteData();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_sleep_record) {
            DialogUtils.showAddRecordDialog(getFragmentManager());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_records:
                ActivityUtils.navigateToSleepRecordsActivity(this);
                break;
            case R.id.nav_sleep_training:
                //TODO add sleep training activity
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_settings:
                ActivityUtils.navigateToSettingsActivity(this);
                break;
            case R.id.nav_logout:
                sessionManager.clearSession();
                ActivityUtils.navigateToLoginActivity(this);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBabyInfoUpdated(BabyInfo babyInfo) {
        updateBabyInfo(babyInfo);
    }

    private void updateUserInfo(UserProfile userProfile) {
        if (userProfile != null) {
            headerViewHolder.nameTextView.setText(userProfile.getUsername());
        }
    }

    private void updateBabyInfo(BabyInfo babyInfo) {
        if (babyInfo != null) {
            headerViewHolder.babyNameTextView.setText(getBabyInfoDisplayString(babyInfo));
        }
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

    private String getBabyInfoDisplayString(BabyInfo babyInfo) {

        String str = "";
        if (babyInfo.getBabyName() != null) {
            str = babyInfo.getBabyName();
        }

        if (babyInfo.getBabyBirthday() != null) {
            Calendar birthday = Calendar.getInstance();
            birthday.setTime(babyInfo.getBabyBirthday());
            Calendar today = Calendar.getInstance();

            int years = DateUtils.yearsBetween(birthday, today);
            int months = DateUtils.monthsBetween(birthday, today);
            birthday.add(Calendar.MONTH, months);
            int days = DateUtils.daysBetween(birthday, today);

            if (years > 0) {
                if (months == 0) {
                    str += String.format(" %d%s", years, years == 1 ? AGE_YEARS_S : AGE_YEARS_P);
                } else {
                    str += String.format(" %d%s %d%s",
                            years, years == 1 ? AGE_YEARS_S : AGE_YEARS_P,
                            (months - years * 12), months == 1 ? AGE_MONTHS_S : AGE_MONTHS_P);
                }
            } else if (months > 0) {
                if (days == 0) {
                    str += String.format(" %d%s", months, months == 1 ? AGE_MONTHS_S : AGE_MONTHS_P);
                } else {
                    str += String.format(" %d%s %d%s",
                            months, months == 1 ? AGE_MONTHS_S : AGE_MONTHS_P,
                            days, days == 1 ? AGE_DAYS_S : AGE_DAYS_P);
                }
            } else {
                str += String.format(" %d%s", days, days == 1 ? AGE_DAYS_S : AGE_DAYS_P);
            }
        }

        return str;
    }

    static class HeaderViewHolder {
        @BindView(R.id.headerImageView) ImageView headerImage;
        @BindView(R.id.usernameTextView) TextView nameTextView;
        @BindView(R.id.babyNameTextView) TextView babyNameTextView;

        public HeaderViewHolder(View root) {
            ButterKnife.bind(this, root);
        }
    }

    @Override
    public void onSleepRecordSaved(Date from, Date to) {
        //TODO reload sleep records
    }

    /**
     * Update baby info task.
     */
    private class LoadRemoteDataTask extends AsyncTask<Void, Integer, Boolean> {

        BabyInfo babyInfo;
        UserProfile userProfile;

        public static final int BABY_INFO_UPDATED = 30;
        public static final int USER_INFO_UPDATED = 60;
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
                babyInfo = sleepServiceClient.getBabyInfo(userId);
                sharedPreferenceUtil.storeBabyInfo(babyInfo);
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
            }
            publishProgress(BABY_INFO_UPDATED);

            try {
                userProfile = identityServiceClient.getUser(userId);
                //TODO store download image if required
                sharedPreferenceUtil.storeUserProfile(userProfile);
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
            }
            publishProgress(USER_INFO_UPDATED);

            //TODO update sleep records
            publishProgress(SLEEP_RECORDS_UPDATED);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0]) {
                case BABY_INFO_UPDATED:
                    updateBabyInfo(babyInfo);
                    break;
                case USER_INFO_UPDATED:
                    updateUserInfo(userProfile);
                    break;
                case SLEEP_RECORDS_UPDATED:
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
            progressBar.setVisibility(View.GONE);
        }
    }
}
