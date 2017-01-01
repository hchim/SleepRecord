package im.hch.sleeprecord.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import im.hch.sleeprecord.activities.LoginActivity;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.activities.RegisterActivity;
import im.hch.sleeprecord.activities.records.SleepRecordsActivity;
import im.hch.sleeprecord.activities.settings.SettingsActivity;

public class ActivityUtils {

    public static void navigateToMainActivity(Activity currentActivity) {
        navigateTo(currentActivity, MainActivity.class, true, null);
    }

    public static void navigateToRegisterActivity(Activity currentActivity) {
        navigateTo(currentActivity, RegisterActivity.class, true, null);
    }

    public static void navigateToLoginActivity(Activity currentActivity) {
        navigateTo(currentActivity, LoginActivity.class, true, null);
    }

    public static void navigateToSettingsActivity(Activity currentActivity) {
        navigateTo(currentActivity, SettingsActivity.class, false, null);
    }

    public static void navigateToSleepRecordsActivity(Activity currentActivity) {
        navigateTo(currentActivity, SleepRecordsActivity.class, false, null);
    }

    /**
     * Navigate the the new activity from the current activity.
     * @param current
     * @param newActivityClass
     * @param clearTop
     */
    private static void navigateTo(Activity current, Class newActivityClass, boolean clearTop, Bundle bundle) {
        Intent intent = new Intent(current, newActivityClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }

        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        current.startActivity(intent);

        if (clearTop) {
            current.finish();
        }
    }

    /**
     * Hide keyboard.
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        View v = activity.getWindow().getCurrentFocus();
        if (v != null) {
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
