package im.hch.sleeprecord.activities.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "SettingsFragment";
    public static final String PREFERENCE_KEY_NICKNAME = "nickname_text";
    public static final String PREFERENCE_KEY_PASSWORD = "pref_password_settings";
    public static final String PREFERENCE_KEY_BABY_INFO = "pref_baby_info";
    public static final String PREFERENCE_KEY_VERSION = "pref_version";

    private SharedPreferenceUtil sharedPreferenceUtil;
    private UserProfile userProfile;
    private MainActivity mainActivity;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        setHasOptionsMenu(true);

        sharedPreferenceUtil = new SharedPreferenceUtil(this.getActivity());
        userProfile = sharedPreferenceUtil.retrieveUserProfile();
        mainActivity = (MainActivity) getActivity();
        String title = getResources().getString(R.string.title_activity_settings);
        mainActivity.setTitle(title);

        EditTextPreference nicknamePref = (EditTextPreference) findPreference(PREFERENCE_KEY_NICKNAME);
        nicknamePref.setOnPreferenceChangeListener(this);
        nicknamePref.setSummary(userProfile.getUsername());
        nicknamePref.setText(userProfile.getUsername());

        findPreference(PREFERENCE_KEY_PASSWORD).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogUtils.showUpdatePasswordDialog(SettingsFragment.this.getFragmentManager());
                return true;
            }
        });

        findPreference(PREFERENCE_KEY_BABY_INFO).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BabyInfo babyInfo = sharedPreferenceUtil.retrieveBabyInfo();
                DialogUtils.showEditBabyInfoDialog(SettingsFragment.this.getFragmentManager(), babyInfo);
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference.getKey().equals(PREFERENCE_KEY_NICKNAME)) {
            String newName = (String) newValue;
            preference.setSummary(newName);
            new SaveUserNameAsyncTask(newName).execute();
            return true;
        }

        return false;
    }

    private class SaveUserNameAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private String userName;

        public SaveUserNameAsyncTask(String userName) {
            this.userName = userName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mainActivity.identityServiceClient.updateUserName(userName);
                return Boolean.TRUE;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                sharedPreferenceUtil.storeUserName(userName);
            }
        }
    }
}
