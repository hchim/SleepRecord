package im.hch.sleeprecord.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SharedPreferenceUtil {

    private static final String SHARED_PREFERENCE_NAME = "SleepRecord.pref";

    private Context mContext;
    private SharedPreferences mSharedPreference;

    public SharedPreferenceUtil(Context context) {
        this.mContext = context;
        this.mSharedPreference = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
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

    public Object getValue(String key, Object defValue) {
        if (defValue instanceof String) {
            return mSharedPreference.getString(key, (String) defValue);
        } else if (defValue instanceof Boolean) {
            return mSharedPreference.getBoolean(key, (Boolean) defValue);
        } else if (defValue instanceof Integer) {
            return mSharedPreference.getInt(key, (Integer) defValue);
        } else if (defValue instanceof Float) {
            return mSharedPreference.getFloat(key, (Float) defValue);
        } else if (defValue instanceof Double) {
            return mSharedPreference.getFloat(key, ((Double) defValue).floatValue());
        } else if (defValue instanceof Long) {
            return mSharedPreference.getLong(key, (Long) defValue);
        } else if (defValue instanceof Set) {
            return mSharedPreference.getStringSet(key, (Set<String>) defValue);
        }

        return null;
    }

    public void removeValue(String key) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.remove(key);
        editor.commit();
    }
}
