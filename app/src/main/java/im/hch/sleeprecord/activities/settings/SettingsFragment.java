package im.hch.sleeprecord.activities.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.util.Log;

import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.AppInfoServiceClient;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "SettingsFragment";
    public static final String PREFERENCE_KEY_NICKNAME = "nickname_text";
    public static final String PREFERENCE_KEY_PASSWORD = "pref_password_settings";
    public static final String PREFERENCE_KEY_BABY_INFO = "pref_baby_info";
    public static final String PREFERENCE_KEY_SUGGESTION = "suggestion_text";

    private SharedPreferenceUtil sharedPreferenceUtil;
    private SessionManager sessionManager;
    private UserProfile userProfile;
    private AppInfoServiceClient appInfoServiceClient;
    private MainActivity mainActivity;

    private EditTextPreference suggestionPreference;

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
        sessionManager = new SessionManager(getActivity());
        userProfile = sharedPreferenceUtil.retrieveUserProfile();
        appInfoServiceClient = new AppInfoServiceClient();
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

        suggestionPreference = (EditTextPreference)findPreference(PREFERENCE_KEY_SUGGESTION);
        suggestionPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference.getKey().equals(PREFERENCE_KEY_NICKNAME)) {
            String newName = (String) newValue;
            preference.setSummary(newName);
            new SaveUserNameAsyncTask(newName).execute();
            return true;
        } else if (preference.getKey().equals(PREFERENCE_KEY_SUGGESTION)) {
            String suggestion = (String) newValue;
            new AddSuggestionAsyncTask(suggestion).execute();
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
                mainActivity.identityServiceClient.updateUserName(userName, sessionManager.getUserId());
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

    private class AddSuggestionAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private String suggestion;

        public AddSuggestionAsyncTask(String suggestion) {
            this.suggestion = suggestion;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                appInfoServiceClient.addSuggestion(sessionManager.getUserId(), suggestion);
                return Boolean.TRUE;
             } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                suggestionPreference.setText("");
                String thanksMessage = SettingsFragment.this.getResources().getString(R.string.suggestion_thanks_message);
                Snackbar.make(SettingsFragment.this.getView(), thanksMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }
}
