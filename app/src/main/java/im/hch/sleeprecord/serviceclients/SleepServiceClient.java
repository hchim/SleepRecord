package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.serviceclients.exceptions.BabyNotExistsException;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.serviceclients.exceptions.TimeOverlapException;
import im.hch.sleeprecord.utils.DateUtils;

public class SleepServiceClient extends BaseServiceClient {
    public static final String TAG = "SleepServiceClient";

    public static final String BABYINFO_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "babyinfos/";
    public static final String SLEEP_RECORDS_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "sleeprecs/";

    public static final String ERROR_CODE_BABY_NOT_EXISTS = "BABY_NOT_EXISTS";
    public static final String ERROR_CODE_TIME_OVERLAP = "TIME_OVERLAP";

    /**
     * Save or update the baby information.
     * @param babyInfo
     * @param userId
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public void saveBabyInfo(BabyInfo babyInfo, String userId)
            throws InternalServerException, ConnectionFailureException {
        String url = BABYINFO_URL + userId;
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

    /**
     * Get baby information.
     * @param userId
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     * @throws BabyNotExistsException
     */
    public BabyInfo getBabyInfo(String userId)
            throws InternalServerException, ConnectionFailureException, BabyNotExistsException {
        String url = BABYINFO_URL + userId;

        try {
            JSONObject result = get(url);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_BABY_NOT_EXISTS)) {
                    throw new BabyNotExistsException();
                }
                throw new InternalServerException();
            }

            BabyInfo babyInfo = new BabyInfo();
            babyInfo.setBabyName(result.getString("name"));
            babyInfo.setBabyBirthday(DateUtils.strToDate(result.getString("birthday"), DATE_FORMAT));
            babyInfo.setBabyGender(BabyInfo.Gender.create(result.getInt("gender")));
            return babyInfo;
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Add sleep record.
     * @param from
     * @param to
     * @param userId
     * @throws ConnectionFailureException
     * @throws InternalServerException
     * @throws TimeOverlapException
     */
    public void addSleepRecord(Date from, Date to, String userId)
            throws ConnectionFailureException, InternalServerException, TimeOverlapException {
        String url = SLEEP_RECORDS_URL + userId;
        try {
            JSONObject object = new JSONObject();
            object.put("userId", userId);
            object.put("fallAsleepTime", from);
            object.put("wakeupTime", to);

            JSONObject result = post(url, object);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_TIME_OVERLAP)) {
                    throw new TimeOverlapException();
                }
                throw new InternalServerException();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }
}
