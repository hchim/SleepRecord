package im.hch.sleeprecord.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

import im.hch.sleeprecord.models.BabyInfo;

public class SharedPreferenceUtil {

    private Context mContext;
    private SharedPreferences mSharedPreference;

    public SharedPreferenceUtil(Context context) {
        this.mContext = context;
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
        }

        editor.commit();
    }

    public String getString(String key, String defaultValue) {
        return mSharedPreference.getString(key, defaultValue);
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
        setValue(BABY_GENDER, babyInfo.getBabyGender());
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

        return null;
    }
}
