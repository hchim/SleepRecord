package im.hch.sleeprecord.utils;

import android.content.Context;
import im.hch.sleeprecord.models.UserProfile;

public class SessionManager {

    private static final String USER_ID = "UserId";
    private static final String ACCESS_TOKEN = "AccessToken";
    private static final String SESSION_CREATE_TIME = "SessionCreateTime";

    private SharedPreferenceUtil mSharedPreferenceUtil;

    public SessionManager(Context context) {
        this.mSharedPreferenceUtil = new SharedPreferenceUtil(context);
    }

    public void createSession(UserProfile userProfile) {
        mSharedPreferenceUtil.setValue(USER_ID, userProfile.getId());
        mSharedPreferenceUtil.setValue(ACCESS_TOKEN, userProfile.getAccessToken());
        mSharedPreferenceUtil.setValue(SESSION_CREATE_TIME, System.currentTimeMillis());
        mSharedPreferenceUtil.storeUserProfile(userProfile);
    }

    public void clearSession() {
        mSharedPreferenceUtil.removeValue(USER_ID);
        mSharedPreferenceUtil.removeValue(ACCESS_TOKEN);
        mSharedPreferenceUtil.removeValue(SESSION_CREATE_TIME);
    }

    public boolean isLoggedIn() {
        return mSharedPreferenceUtil.getString(USER_ID, null) != null;
    }

    public String getUserId() {
        return mSharedPreferenceUtil.getString(USER_ID, null);
    }
}
