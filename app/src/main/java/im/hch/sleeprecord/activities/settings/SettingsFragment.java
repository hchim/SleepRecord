package im.hch.sleeprecord.activities.settings;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;

import im.hch.sleeprecord.R;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.IdentityServiceClient;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "SettingsFragment";
    public static final String PREFERENCE_KEY_NICKNAME = "nickname_text";
    public static final String PREFERENCE_KEY_PASSWORD = "pref_password_settings";
    public static final String PREFERENCE_KEY_baby_info = "pref_baby_info";

    private SharedPreferenceUtil sharedPreferenceUtil;
    private SessionManager sessionManager;
    private IdentityServiceClient identityServiceClient;
    private UserProfile userProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        setHasOptionsMenu(true);
        sharedPreferenceUtil = new SharedPreferenceUtil(this.getActivity());
        identityServiceClient = new IdentityServiceClient();
        sessionManager = new SessionManager(getActivity());
        userProfile = sharedPreferenceUtil.retrieveUserProfile();

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

        findPreference(PREFERENCE_KEY_baby_info).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BabyInfo babyInfo = sharedPreferenceUtil.retrieveBabyInfo();
                DialogUtils.showEditBabyInfoDialog(SettingsFragment.this.getFragmentManager(), babyInfo);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                identityServiceClient.updateUserName(userName, sessionManager.getUserId());
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
