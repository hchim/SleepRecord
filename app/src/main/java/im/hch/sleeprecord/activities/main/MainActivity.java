package im.hch.sleeprecord.activities.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
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
import im.hch.sleeprecord.utils.ImageUtils;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;
import im.hch.sleeprecord.utils.SleepRecordUtils;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        BabyInfoDialogFragment.BabyInfoDialogFragmentListener,
        AddRecordDialogFragment.AddRecordDialogListener {
    public static final String TAG = "MainActivity";
    /**
     * The number of sleep records to show in the sleep records widget.
     */
    public static final int SHOW_SLEEP_RECORDS_NUM = 5;
    public static final int CROP_WIN_SIZE = 1024;

    private SleepRecordsAdapter sleepRecordsAdapter;
    private SessionManager sessionManager;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private SleepServiceClient sleepServiceClient;
    private IdentityServiceClient identityServiceClient;
    private HeaderViewHolder headerViewHolder;
    private Uri mCropImageUri;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.list_view) ListView sleepRecordListView;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    @BindString(R.string.age_years_singlular) String AGE_YEARS_S;
    @BindString(R.string.age_months_singlular) String AGE_MONTHS_S;
    @BindString(R.string.age_days_singlular) String AGE_DAYS_S;
    @BindString(R.string.age_years_plural) String AGE_YEARS_P;
    @BindString(R.string.age_months_plural) String AGE_MONTHS_P;
    @BindString(R.string.age_days_plural) String AGE_DAYS_P;
    @BindString(R.string.permission_not_granted) String permissionNotGranted;
    @BindString(R.string.crop_image_menu_crop) String cropMenu;

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
        if (babyInfo != null) {
            updateBabyInfo(babyInfo);
        }

        initSleepRecords();

        headerViewHolder.babyNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showEditBabyInfoDialog(
                        MainActivity.this.getFragmentManager(),
                        sharedPreferenceUtil.retrieveBabyInfo());
            }
        });

        headerViewHolder.headerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.startPickImageActivity(MainActivity.this);
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
                sharedPreferenceUtil.removeAllData();
                ActivityUtils.navigateToLoginActivity(this);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE:
                Uri imageUri = CropImage.getPickImageResultUri(this, data);

                // For API >= 23 we need to check specifically that we have permissions to read external storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    mCropImageUri = imageUri;
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                } else {
                    // no permissions required or already grunted, can start crop image activity
                    startHeaderCropActivity(imageUri);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK && result != null && result.getUri() != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                        headerViewHolder.headerImage.setImageBitmap(bitmap);
                        //start a async task to save image to local cache, add image path to shared preference and upload the image
                        new SaveImageFileTask(bitmap).execute();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE:
                Log.e(TAG, "Failed to crop image.");
                break;
        }
    }

    @Override
    public void onBabyInfoUpdated(BabyInfo babyInfo) {
        updateBabyInfo(babyInfo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startHeaderCropActivity(mCropImageUri);
            } else {
                Snackbar.make(headerViewHolder.headerImage, permissionNotGranted, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void startHeaderCropActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setMinCropResultSize(CROP_WIN_SIZE, CROP_WIN_SIZE)
                .setAllowRotation(false)
                .setFixAspectRatio(true)
                .start(this);
    }

    private void updateUserInfo(UserProfile userProfile) {
        if (userProfile != null) {
            headerViewHolder.nameTextView.setText(userProfile.getUsername());
            if (userProfile.getHeaderIconPath() != null) {
                Picasso.with(this)
                        .load(new File(userProfile.getHeaderIconPath()))
                        .into(headerViewHolder.headerImage);
            }
        }
    }

    private void updateBabyInfo(BabyInfo babyInfo) {
        if (babyInfo != null) {
            headerViewHolder.babyNameTextView.setText(getBabyInfoDisplayString(babyInfo));
        }
    }

    /**
     * Load sleep records from shared preference.
     */
    private void initSleepRecords() {
        List<SleepRecord> records = sharedPreferenceUtil.retrieveSleepRecords();
        if (records == null) {
            records = new ArrayList<>();
        }

        Calendar to = Calendar.getInstance();
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1);

        sleepRecordsAdapter = new SleepRecordsAdapter(this,
                SleepRecordUtils.fillSleepRecords(records, from.getTime(), to.getTime()));
        sleepRecordListView.setAdapter(sleepRecordsAdapter);
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
        @BindView(R.id.headerImageView) CircleImageView headerImage;
        @BindView(R.id.usernameTextView) TextView nameTextView;
        @BindView(R.id.babyNameTextView) TextView babyNameTextView;

        public HeaderViewHolder(View root) {
            ButterKnife.bind(this, root);
        }
    }

    @Override
    public void onSleepRecordSaved(Date from, Date to) {
        new LoadSleepRecordTask().execute();
    }

    /**
     * Update baby info task.
     */
    private class LoadRemoteDataTask extends AsyncTask<Void, Integer, Boolean> {

        BabyInfo babyInfo;
        UserProfile userProfile;
        List<SleepRecord> sleepRecords;
        boolean reloadHeaderImage = false;

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
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
            }
            publishProgress(BABY_INFO_UPDATED);

            try {
                userProfile = identityServiceClient.getUser(userId);

                String currentHeaderUrl = sharedPreferenceUtil.retrieveHeaderImageUrl();
                String headerImagePath = sharedPreferenceUtil.retrieveHeaderImage();
                sharedPreferenceUtil.storeUserProfile(userProfile);
                //download header image the url of the header
                if (userProfile.getHeaderIconUrl() != null) {
                    if (headerImagePath == null
                            || !userProfile.getHeaderIconUrl().equals(currentHeaderUrl)) {
                        String imagePath = ImageUtils.downloadImage(MainActivity.this, userProfile.getHeaderIconUrl());
                        if (imagePath != null) {
                            userProfile.setHeaderIconPath(imagePath);
                            sharedPreferenceUtil.storeHeaderImage(imagePath);
                            reloadHeaderImage = true;
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
            }
            publishProgress(USER_INFO_UPDATED);

            //update sleep records
            Calendar to = Calendar.getInstance();
            Calendar from = Calendar.getInstance();
            from.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1);

            try {
                sleepRecords = sleepServiceClient.getSleepRecords(userId, from.getTime(), to.getTime());
                sharedPreferenceUtil.storeSleepRecords(sleepRecords, userId);
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
            }
            publishProgress(SLEEP_RECORDS_UPDATED);

            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0]) {
                case BABY_INFO_UPDATED:
                    updateBabyInfo(babyInfo);
                    if (babyInfo == null) {
                        DialogUtils.showEditBabyInfoDialog(getFragmentManager(), babyInfo);
                    }
                    break;
                case USER_INFO_UPDATED:
                    updateUserInfo(userProfile);
                    if (reloadHeaderImage) {
                        Picasso.with(MainActivity.this)
                                .load(new File(userProfile.getHeaderIconPath()))
                                .into(headerViewHolder.headerImage);
                    }
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
            progressBar.setVisibility(View.GONE);
        }
    }

    private class LoadSleepRecordTask extends AsyncTask<Void, Integer, Boolean> {
        List<SleepRecord> sleepRecords;

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
                sleepRecords = sleepServiceClient.getSleepRecords(userId, from.getTime(), to.getTime());
                sharedPreferenceUtil.storeSleepRecords(sleepRecords, userId);
            } catch (Exception e) {
                Log.w(MainActivity.TAG, e);
            }

            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                sleepRecordsAdapter.updateSleepRecords(sleepRecords);
            }
        }
    }

    private class SaveImageFileTask extends AsyncTask<Void, Void, Boolean> {
        private Bitmap bitmap;

        public SaveImageFileTask(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (bitmap == null) {
                return Boolean.FALSE;
            }
            //save local image
            String imagePath = ImageUtils.saveImageFile(MainActivity.this, bitmap);
            //store header path to shared preference
            sharedPreferenceUtil.storeHeaderImage(imagePath);
            //upload header image
            String userId = sessionManager.getUserId();
            try {
                String url = identityServiceClient.uploadHeaderIcon(imagePath, userId);
                sharedPreferenceUtil.storeHeaderImageUrl(url);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return Boolean.TRUE;
        }
    }
}
