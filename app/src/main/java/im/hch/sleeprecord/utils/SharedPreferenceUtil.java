package im.hch.sleeprecord.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import im.hch.sleeprecord.models.AppConfig;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.SleepRecordsPerDay;
import im.hch.sleeprecord.models.SleepTrainingPlan;
import im.hch.sleeprecord.models.UserProfile;

public class SharedPreferenceUtil {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private SharedPreferences mSharedPreference;

    public SharedPreferenceUtil(Context context) {
        this.mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setValue(String key, Object value) {
        SharedPreferences.Editor editor = mSharedPreference.edit();

        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Double) {
            editor.putFloat(key, ((Double) value).floatValue());
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        } else if (value instanceof Date) {
            String strDate = null;
            if (value != null) {
                strDate = DateUtils.dateToStr((Date) value, DATE_FORMAT);
            }
            editor.putString(key, strDate);
        } else if (value instanceof JSONObject) {
            String jsonString = null;
            if (value != null) {
                jsonString = ((JSONObject) value).toString();
            }
            editor.putString(key, jsonString);
        }

        editor.commit();
    }

    public String getString(String key, String defaultValue) {
        return mSharedPreference.getString(key, defaultValue);
    }

    public Date getDate(String key, Date defaultValue) {
        String str = mSharedPreference.getString(key, null);
        if (str == null) {
            return defaultValue;
        } else {
            return DateUtils.strToDate(str, DATE_FORMAT);
        }
    }

    public Float getFloat(String key, float defaultValue) {
        return mSharedPreference.getFloat(key, defaultValue);
    }

    public Boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPreference.getBoolean(key, defaultValue);
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return mSharedPreference.getStringSet(key, defaultValue);
    }

    public Integer getInt(String key, int defaultValue) {
        return mSharedPreference.getInt(key, defaultValue);
    }

    public Long getLong(String key, long defaultValue) {
        return mSharedPreference.getLong(key, defaultValue);
    }

    public JSONObject getJSONObject(String key, JSONObject defaultValue) {
        String str = mSharedPreference.getString(key, null);
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return new JSONObject(str);
            } catch (JSONException e) {
                return null;
            }
        }
    }

    public void removeValue(String key) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.remove(key);
        editor.commit();
    }

    private static final String BABY_NAME = "BabyName";
    private static final String BABY_BIRTHDAY = "BabyBirthday";
    private static final String BABY_GENDER = "BabyGender";

    public void storeBabyInfo(BabyInfo babyInfo) {
        if (babyInfo == null) {
            return;
        }

        setValue(BABY_NAME, babyInfo.getBabyName());
        setValue(BABY_BIRTHDAY, DateUtils.dateToStr(babyInfo.getBabyBirthday()));
        setValue(BABY_GENDER, babyInfo.getBabyGender().getValue());
    }

    public void removeBabyInfo() {
        removeValue(BABY_NAME);
        removeValue(BABY_BIRTHDAY);
        removeValue(BABY_GENDER);
    }

    public BabyInfo retrieveBabyInfo() {
        String name = getString(BABY_NAME, null);
        if (name == null) {
            return null;
        }

        String birthday = getString(BABY_BIRTHDAY, null);
        int gender = getInt(BABY_GENDER, BabyInfo.Gender.Unknown.getValue());

        BabyInfo babyInfo = new BabyInfo();
        babyInfo.setBabyName(name);
        if (birthday != null) {
            babyInfo.setBabyBirthday(DateUtils.strToDate(birthday));
        }
        babyInfo.setBabyGender(BabyInfo.Gender.create(gender));

        return babyInfo;
    }

    public static final String USER_ID = "UserId";
    public static final String EMAIL = "Email";
    public static final String USER_NAME = "UserName";
    public static final String HEADER_ICON_URL = "HeaderIconURL";
    public static final String HEADER_ICON = "HeaderIcon";
    public static final String ACCESS_TOKEN = "AccessToken";
    public static final String SESSION_CREATE_TIME = "SessionCreateTime";
    public static final String EMAIL_VERIFIED = "EmailVerified";
    public static final String USER_CREATE_TIME = "UserCreateTime";

    public void storeUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            return;
        }

        setValue(USER_NAME, userProfile.getUsername());
        setValue(HEADER_ICON_URL, userProfile.getHeaderIconUrl());
        setValue(EMAIL_VERIFIED, userProfile.isEmailVerified());
        setValue(USER_CREATE_TIME, userProfile.getCreateTime());
    }

    public void removeUserProfile() {
        removeValue(USER_NAME);
        removeValue(HEADER_ICON_URL);
        removeValue(EMAIL_VERIFIED);
        removeValue(HEADER_ICON);
        removeValue(USER_CREATE_TIME);
    }

    public UserProfile retrieveUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(getString(USER_ID, null));
        userProfile.setUsername(getString(USER_NAME, null));
        userProfile.setHeaderIconUrl(getString(HEADER_ICON_URL, null));
        userProfile.setHeaderIconPath(getString(HEADER_ICON, null));
        userProfile.setEmailVerified(getBoolean(EMAIL_VERIFIED, false));
        userProfile.setCreateTime(getDate(USER_CREATE_TIME, null));
        return userProfile;
    }

    public void storeUserName(String userName) {
        if (userName == null) {
            return;
        }
        setValue(USER_NAME, userName);
    }

    public String retrieveUserName() {
        return getString(USER_NAME, null);
    }

    public void storeHeaderImage(String headerImagePath) {
        if (headerImagePath == null) {
            return;
        }
        setValue(HEADER_ICON, headerImagePath);
    }

    public String retrieveHeaderImage() {
        return getString(HEADER_ICON, null);
    }

    public void storeHeaderImageUrl(String headerImageUrl) {
        if (headerImageUrl == null) {
            return;
        }
        setValue(HEADER_ICON_URL, headerImageUrl);
    }

    public String retrieveHeaderImageUrl() {
        return getString(HEADER_ICON_URL, null);
    }

    public static final String SLEEP_RECORDS = "SleepRecords";

    public void storeSleepRecords(List<SleepRecordsPerDay> records, String userId) {
        if (records == null || userId == null) {
            return;
        }

        JSONArray jsonArray = new JSONArray();
        for (SleepRecordsPerDay record : records) {
            jsonArray.put(record.toJson());
        }

        JSONObject object = new JSONObject();
        try {
            object.put("records", jsonArray);
            setValue(SLEEP_RECORDS, object.toString());
        } catch (JSONException e) {}
    }

    public void removeSleepRecords() {
        removeValue(SLEEP_RECORDS);
    }

    public List<SleepRecordsPerDay> retrieveSleepRecords() {
        String json = getString(SLEEP_RECORDS, null);
        List<SleepRecordsPerDay> records = new ArrayList<>();

        if (json == null) {
            return records;
        }

        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("records");
            for (int i = 0; i < array.length(); i++) {
                SleepRecordsPerDay record = SleepRecordsPerDay.create(array.getJSONObject(i));
                records.add(record);
            }
        } catch (JSONException e) {}

        return records;
    }

    /* Sleep Training */

    public static final String SLEEP_TRAINING_PLAN = "TrainingPlan";

    public void storeSleepTrainingPlan(SleepTrainingPlan plan) {
        setValue(SLEEP_TRAINING_PLAN, plan.toJson());
    }

    public SleepTrainingPlan retrieveSleepTrainingPlan() {
        JSONObject jsonObject = getJSONObject(SLEEP_TRAINING_PLAN, null);
        if (jsonObject == null) {
            return null;
        }

        return new SleepTrainingPlan(jsonObject);
    }

    public void removeSleepTrainingPlan() {
        removeValue(SLEEP_TRAINING_PLAN);
    }

    /**
     * Remove user related preference data.
     */
    public void removeAllData() {
        removeBabyInfo();
        removeUserProfile();
        removeSleepRecords();
        removeSleepTrainingPlan();
    }

    public void storeAppConfig(AppConfig appConfig) {

    }

    public AppConfig retrieveAppConfig() {
        AppConfig appConfig = new AppConfig();
        return appConfig;
    }
}
