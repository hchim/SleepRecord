package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.serviceclients.exceptions.BabyNotExistsException;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.serviceclients.exceptions.TimeOverlapException;
import im.hch.sleeprecord.utils.DateUtils;
import im.hch.sleeprecord.utils.SleepRecordUtils;

public class SleepServiceClient extends BaseServiceClient {
    public static final String TAG = "SleepServiceClient";

    public static final String BABYINFO_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "babyinfos/";
    public static final String SLEEP_RECORDS_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "sleeprecs/";

    public static final String ERROR_CODE_BABY_NOT_EXISTS = "BABY_NOT_EXISTS";
    public static final String ERROR_CODE_TIME_OVERLAP = "TIME_OVERLAP";
    public static final String QUERY_DATE_FORMAT = "yyyy-MM-dd";

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
            object.put("birthday", DateUtils.dateToStr(babyInfo.getBabyBirthday(), QUERY_DATE_FORMAT));
            object.put("gender", babyInfo.getBabyGender().getValue());

            JSONObject result = post(url, object);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }
                throw new InternalServerException();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error", e);
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
            babyInfo.setBabyBirthday(DateUtils.strToDate(result.getString("birthday"), QUERY_DATE_FORMAT));
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
        String url = SLEEP_RECORDS_URL;
        try {
            JSONObject object = new JSONObject();
            object.put("userId", userId);
            object.put("fallAsleepTime", from);
            object.put("wakeupTime", to);
            object.put("timezone", DateUtils.getLocalTimezone(false));

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

    /**
     * Get the sleep records of userId in the specified time period.
     * @param userId
     * @param from
     * @param to
     * @return
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public List<SleepRecord> getSleepRecords(String userId, Date from, Date to)
            throws ConnectionFailureException, InternalServerException {
        String url = String.format(SLEEP_RECORDS_URL + "%s/%s/%s/%s", userId,
                DateUtils.dateToStr(from, QUERY_DATE_FORMAT),
                DateUtils.dateToStr(to, QUERY_DATE_FORMAT),
                DateUtils.getLocalTimezone(true));

        try {
            JSONObject result = get(url);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }
                throw new InternalServerException();
            }

            JSONArray array = result.getJSONArray("records");
            List<SleepRecord> list = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                SleepRecord rec = SleepRecord.create(array.getJSONObject(i));
                if (rec != null) {
                    list.add(rec);
                }
            }

            Calendar fromCal = Calendar.getInstance();
            fromCal.setTime(from);
            Calendar toCal = Calendar.getInstance();
            toCal.setTime(to);
            list = SleepRecordUtils.fillSleepRecords(list, from, to);

            return list;
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }
}
