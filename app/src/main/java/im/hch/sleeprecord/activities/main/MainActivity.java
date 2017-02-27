package im.hch.sleeprecord.activities.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import im.hch.sleeprecord.Metrics;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.home.HomeFragment;
import im.hch.sleeprecord.activities.records.SleepRecordsFragment;
import im.hch.sleeprecord.activities.settings.SettingsFragment;
import im.hch.sleeprecord.activities.training.ChecklistFragment;
import im.hch.sleeprecord.activities.training.SleepTrainingFragment;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.SleepTrainingPlan;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.IdentityServiceClient;
import im.hch.sleeprecord.serviceclients.SleepServiceClient;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DateUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.ImageUtils;
import im.hch.sleeprecord.utils.MetricHelper;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;
import lombok.Getter;
import lombok.Setter;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        BabyInfoDialogFragment.BabyInfoDialogFragmentListener {
    public static final String TAG = "MainActivity";

    public static final int CROP_WIN_SIZE = 1024;

    public SessionManager sessionManager;
    public SharedPreferenceUtil sharedPreferenceUtil;
    public SleepServiceClient sleepServiceClient;
    public IdentityServiceClient identityServiceClient;
    @Getter
    private HeaderViewHolder headerViewHolder;
    private Uri mCropImageUri;
    public MetricHelper metricHelper;
    @Getter@Setter
    private boolean remoteDataLoaded = false;

    @Getter
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

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
        metricHelper = new MetricHelper(this);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        updateUserInfo(sharedPreferenceUtil.retrieveUserProfile());
        BabyInfo babyInfo = sharedPreferenceUtil.retrieveBabyInfo();
        if (babyInfo != null) {
            updateBabyInfo(babyInfo);
        }

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

        loadFragment(HomeFragment.newInstance(), null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        metricHelper.startTimeMetric(Metrics.MAIN_ACTIVITY_USAGE_TIME_METRIC);
    }

    @Override
    protected void onPause() {
        metricHelper.stopTimeMetric(Metrics.MAIN_ACTIVITY_USAGE_TIME_METRIC);
        super.onPause();
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                loadFragment(HomeFragment.newInstance(), null);
                break;
            case R.id.nav_records:
                loadFragment(SleepRecordsFragment.newInstance(), null);
                break;
            case R.id.nav_sleep_training:
                SleepTrainingPlan plan = sharedPreferenceUtil.retrieveSleepTrainingPlan();
                if (plan == null) {
                    loadFragment(ChecklistFragment.newInstance(), null);
                } else {
                    loadFragment(SleepTrainingFragment.newInstance(), null);
                }
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_settings:
                loadFragment(SettingsFragment.newInstance(), null);
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

    public void updateUserInfo(UserProfile userProfile) {
        if (userProfile != null) {
            headerViewHolder.nameTextView.setText(userProfile.getUsername());
            if (userProfile.getHeaderIconPath() != null) {
                Picasso.with(this)
                        .load(new File(userProfile.getHeaderIconPath()))
                        .into(headerViewHolder.headerImage);
            }
        }
    }

    public void updateBabyInfo(BabyInfo babyInfo) {
        if (babyInfo != null) {
            headerViewHolder.babyNameTextView.setText(getBabyInfoDisplayString(babyInfo));
        }
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

    public void loadFragment(Fragment fragment, Bundle args) {
        if (args != null) {
            fragment.setArguments(args);
        }
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        drawer.closeDrawer(GravityCompat.START);
    }

    public static class HeaderViewHolder {
        public @BindView(R.id.headerImageView) CircleImageView headerImage;
        @BindView(R.id.usernameTextView) TextView nameTextView;
        @BindView(R.id.babyNameTextView) TextView babyNameTextView;

        public HeaderViewHolder(View root) {
            ButterKnife.bind(this, root);
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
