package im.hch.sleeprecord.utils;

import android.content.Context;
import android.content.Intent;

import im.hch.sleeprecord.activities.LoginActivity;

public class SessionManager {

    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String LOGIN_ID = "LoginID";
    private static final String USERNAME = "UserName";
    private static final String ACCESS_TOKEN = "AccessToken";
    private static final String SESSION_CREATE_TIME = "SessionCreateTime";

    private Context mContext;
    private SharedPreferenceUtil mSharedPreferenceUtil;

    public SessionManager(Context context) {
        this.mContext = context;
        this.mSharedPreferenceUtil = new SharedPreferenceUtil(context);
    }

    public void createSession(String loginId, String username, String accessToken) {
        mSharedPreferenceUtil.setValue(IS_LOGIN, true);
        mSharedPreferenceUtil.setValue(LOGIN_ID, loginId);
        mSharedPreferenceUtil.setValue(USERNAME, username);
        mSharedPreferenceUtil.setValue(ACCESS_TOKEN, accessToken);
        mSharedPreferenceUtil.setValue(SESSION_CREATE_TIME, System.currentTimeMillis());
    }

    public void logoutUser() {
        mSharedPreferenceUtil.removeValue(IS_LOGIN);
        mSharedPreferenceUtil.removeValue(USERNAME);
        mSharedPreferenceUtil.removeValue(ACCESS_TOKEN);
        mSharedPreferenceUtil.removeValue(SESSION_CREATE_TIME);

        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        // Closing all the Activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        mContext.startActivity(intent);
    }

    public void checkLogin() {
        if (!isLoggedIn()) {
            startLoginActivity();
        }
    }

    public boolean isLoggedIn() {
        return mSharedPreferenceUtil.getBoolean(IS_LOGIN, false);
    }

    public String getUsername() {
        return mSharedPreferenceUtil.getString(USERNAME, null);
    }

    public String getAccessToken() {
        return mSharedPreferenceUtil.getString(ACCESS_TOKEN, null);
    }

    public String getLoginId() {
        return mSharedPreferenceUtil.getString(LOGIN_ID, null);
    }
}
