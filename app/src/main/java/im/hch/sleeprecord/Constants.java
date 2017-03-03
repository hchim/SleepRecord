package im.hch.sleeprecord;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

public class Constants {

    public static final String VERSION = "v1.0.0";
    public static final String APP_NAME = "SleepAiden";
    public static final String OS_NAME = "Android";

    public static JSONObject getAppJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("appName", Constants.APP_NAME);
            object.put("appVersion", Constants.VERSION);
            object.put("device", getDeviceJSON());
            object.put("os", getOSJSON());
        } catch (JSONException e) {}

        return object;
    }

    /**
     * Get the device information and wrap in a json.
     * @return
     */
    public static JSONObject getDeviceJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("model", Build.MODEL); // like "Nexus 4"
            object.put("brand", Build.BOARD); // like "google"
            object.put("serial", Build.SERIAL);
        } catch (JSONException e) {}

        return object;
    }

    public static JSONObject getOSJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("os_name", OS_NAME);
            object.put("sdk_int", Build.VERSION.SDK_INT); // BUILD.VERSION_CODES
            object.put("type", Build.TYPE); // like "user", "userdebug"
            object.put("fingerprint", Build.FINGERPRINT);
        } catch (JSONException e) {}
        return object;
    }
}
