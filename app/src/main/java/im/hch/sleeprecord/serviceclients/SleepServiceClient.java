package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.serviceclients.exceptions.BabyNotExistsException;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;

public class SleepServiceClient extends BaseServiceClient {
    public static final String TAG = "SleepServiceClient";

    public static final String SAVE_BABYINFO_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "babyinfos/";

    public static final String ERROR_CODE_BABY_NOT_EXISTS = "BABY_NOT_EXISTS";

    /**
     * Save or update the baby information.
     * @param babyInfo
     * @param userId
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public void saveBabyInfo(BabyInfo babyInfo, String userId)
            throws InternalServerException, ConnectionFailureException {
        String url = SAVE_BABYINFO_URL + userId;
        JSONObject object = new JSONObject();

        try {
            object.put("name", babyInfo.getBabyName());
            object.put("birthday", babyInfo.getBabyBirthday());
            object.put("gender", babyInfo.getBabyGender().getValue());

            JSONObject result = post(url, object);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }
                throw new InternalServerException();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    public void addSleepRecord(Date from, Date to, String userid) {
        //
    }
}
