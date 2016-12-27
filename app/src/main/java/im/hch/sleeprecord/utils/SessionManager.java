package im.hch.sleeprecord.utils;

import android.content.Context;
import im.hch.sleeprecord.models.UserProfile;

public class SessionManager {

    private static final String USER_ID = "UserId";
    private static final String EMAIL = "Email";
    private static final String USER_NAME = "UserName";
    private static final String HEADER_ICON_URL = "HeaderIconURL";
    private static final String HEADER_ICON = "HeaderIcon";
    private static final String ACCESS_TOKEN = "AccessToken";
    private static final String SESSION_CREATE_TIME = "SessionCreateTime";

    private SharedPreferenceUtil mSharedPreferenceUtil;

    public SessionManager(Context context) {
        this.mSharedPreferenceUtil = new SharedPreferenceUtil(context);
    }

    public void createSession(UserProfile userProfile) {
        mSharedPreferenceUtil.setValue(USER_ID, userProfile.getId());
        mSharedPreferenceUtil.setValue(EMAIL, userProfile.getEmail());
        mSharedPreferenceUtil.setValue(USER_NAME, userProfile.getUsername());
        mSharedPreferenceUtil.setValue(ACCESS_TOKEN, userProfile.getAccessToken());
        mSharedPreferenceUtil.setValue(HEADER_ICON_URL, userProfile.getHeaderIconUrl());
        mSharedPreferenceUtil.setValue(SESSION_CREATE_TIME, System.currentTimeMillis());
    }

    public void clearSession() {
        mSharedPreferenceUtil.removeValue(USER_ID);
        mSharedPreferenceUtil.removeValue(EMAIL);
        mSharedPreferenceUtil.removeValue(USER_NAME);
        mSharedPreferenceUtil.removeValue(ACCESS_TOKEN);
        mSharedPreferenceUtil.removeValue(HEADER_ICON_URL);
        mSharedPreferenceUtil.removeValue(HEADER_ICON);
        mSharedPreferenceUtil.removeValue(SESSION_CREATE_TIME);
    }

    public boolean isLoggedIn() {
        return mSharedPreferenceUtil.getString(USER_ID, null) != null;
    }

    public String getUsername() {
        return mSharedPreferenceUtil.getString(USER_NAME, null);
    }

    public String getHeaderIcon() {
        return mSharedPreferenceUtil.getString(HEADER_ICON, null);
    }

    public String getHeaderIconUrl() {
        return mSharedPreferenceUtil.getString(HEADER_ICON_URL, null);
    }

    public String getAccessToken() {
        return mSharedPreferenceUtil.getString(ACCESS_TOKEN, null);
    }

    public String getUserId() {
        return mSharedPreferenceUtil.getString(USER_ID, null);
    }

    public String getEmail() {
        return mSharedPreferenceUtil.getString(EMAIL, null);
    }
}
