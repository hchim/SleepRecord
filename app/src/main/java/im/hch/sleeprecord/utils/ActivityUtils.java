package im.hch.sleeprecord.utils;

import android.app.Activity;
import android.content.Intent;

import im.hch.sleeprecord.activities.AddRecordActivity;
import im.hch.sleeprecord.activities.LoginActivity;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.activities.RegisterActivity;
import im.hch.sleeprecord.activities.records.SleepRecordsActivity;
import im.hch.sleeprecord.activities.settings.SettingsActivity;

public class ActivityUtils {

    public static void navigateToMainActivity(Activity currentActivity) {
        navigateTo(currentActivity, MainActivity.class, true);
    }

    public static void navigateToRegisterActivity(Activity currentActivity) {
        navigateTo(currentActivity, RegisterActivity.class, true);
    }

    public static void navigateToLoginActivity(Activity currentActivity) {
        navigateTo(currentActivity, LoginActivity.class, true);
    }

    public static void navigateToSettingsActivity(Activity currentActivity) {
        navigateTo(currentActivity, SettingsActivity.class, false);
    }

    public static void navigateToAddRecordActivity(Activity currentActivity) {
        navigateTo(currentActivity, AddRecordActivity.class, false);
    }

    public static void navigateToSleepRecordsActivity(Activity currentActivity) {
        navigateTo(currentActivity, SleepRecordsActivity.class, false);
    }

    /**
     * Navigate the the new activity from the current activity.
     * @param current
     * @param newActivityClass
     * @param clearTop
     */
    private static void navigateTo(Activity current, Class newActivityClass, boolean clearTop) {
        Intent intent = new Intent(current, newActivityClass);

        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        current.startActivity(intent);

        if (clearTop) {
            current.finish();
        }
    }
}
