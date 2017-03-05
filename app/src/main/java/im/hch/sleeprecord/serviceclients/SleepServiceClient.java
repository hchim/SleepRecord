package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.models.SleepTrainingPlan;
import im.hch.sleeprecord.serviceclients.exceptions.AccountNotExistException;
import im.hch.sleeprecord.serviceclients.exceptions.AuthFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.BabyNotExistsException;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.serviceclients.exceptions.InvalidRequestException;
import im.hch.sleeprecord.serviceclients.exceptions.TimeOverlapException;
import im.hch.sleeprecord.serviceclients.exceptions.TrainingPlanNotExistException;
import im.hch.sleeprecord.utils.DateUtils;
import im.hch.sleeprecord.utils.SleepRecordUtils;

public class SleepServiceClient extends BaseServiceClient {
    public static final String TAG = "SleepServiceClient";

    public static final String BABYINFO_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "babyinfos/";
    public static final String SLEEP_RECORDS_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "sleeprecs/";
    public static final String SLEEP_TRAINING_PLAN_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "plan/";
    public static final String TRAINING_RECORDS_URL = EndPoints.SLEEP_RECORD_SERVICE_ENDPOINT + "trainrecs/";

    public static final String RESET_SLEEP_TRAINING_PLAN_URL = SLEEP_TRAINING_PLAN_URL + "reset";

    public static final String ERROR_CODE_BABY_NOT_EXISTS = "BABY_NOT_EXISTS";
    public static final String ERROR_CODE_TIME_OVERLAP = "TIME_OVERLAP";
    public static final String ERROR_PLAN_NOT_EXISTS = "SLEEP_TRAINING_PLAN_NOT_EXISTS";
    public static final String QUERY_DATE_FORMAT = "yyyy-MM-dd";


    private Map<String, String> aaaHeaders;

    public SleepServiceClient() {
        aaaHeaders = new HashMap<>();
    }

    public void setAccessToken(String accessToken) {
        aaaHeaders.put(BaseServiceClient.REQUEST_HEADER_ACCESS_TOKEN, accessToken);

    }

    /**
     * Save or update the baby information.
     * @param babyInfo
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public void saveBabyInfo(BabyInfo babyInfo)
            throws InternalServerException, ConnectionFailureException,
            AuthFailureException, InvalidRequestException {
        String url = BABYINFO_URL;
        JSONObject object = new JSONObject();

        try {
            object.put("name", babyInfo.getBabyName());
            object.put("birthday", DateUtils.dateToStr(babyInfo.getBabyBirthday(), QUERY_DATE_FORMAT));
            object.put("gender", babyInfo.getBabyGender().getValue());

            JSONObject result = post(url, object, aaaHeaders);

            handleGeneralErrors(result, false);
            handleGeneralErrors(result, true);
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error", e);
            throw new InternalServerException();
        }
    }

    /**
     * Get baby information.
     * @return
     * @throws InternalServerException
     * @throws ConnectionFailureException
     * @throws BabyNotExistsException
     */
    public BabyInfo getBabyInfo()
            throws InternalServerException, ConnectionFailureException,
            BabyNotExistsException, AuthFailureException,
            InvalidRequestException, AccountNotExistException {
        String url = BABYINFO_URL;

        try {
            JSONObject result = get(url, aaaHeaders);

            handleGeneralErrors(result, false);
            handleAAAErrors(result, false);
            if (result.has(ERROR_CODE_KEY)) {
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
     * @throws ConnectionFailureException
     * @throws InternalServerException
     * @throws TimeOverlapException
     */
    public void addSleepRecord(Date from, Date to)
            throws ConnectionFailureException, InternalServerException,
            TimeOverlapException, AuthFailureException, InvalidRequestException, AccountNotExistException {
        String url = SLEEP_RECORDS_URL;
        try {
            JSONObject object = new JSONObject();
            object.put("fallAsleepTime", from);
            object.put("wakeupTime", to);
            object.put("timezone", DateUtils.getLocalTimezone(false));

            JSONObject result = post(url, object, aaaHeaders);

            handleGeneralErrors(result, false);
            handleAAAErrors(result, false);
            if (result.has(ERROR_CODE_KEY)) {
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
     * @param from
     * @param to
     * @return
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public List<SleepRecord> getSleepRecords(Date from, Date to)
            throws ConnectionFailureException, InternalServerException, AuthFailureException, InvalidRequestException, AccountNotExistException {
        String url = String.format(SLEEP_RECORDS_URL + "%s/%s/%s",
                DateUtils.dateToStr(from, QUERY_DATE_FORMAT),
                DateUtils.dateToStr(to, QUERY_DATE_FORMAT),
                DateUtils.getLocalTimezone(true));

        try {
            JSONObject result = get(url, aaaHeaders);

            handleGeneralErrors(result, false);
            handleAAAErrors(result, true);

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

    /**
     * Save the sleep training plan.
     * @param plan
     */
    public void saveSleepTrainingPlan(SleepTrainingPlan plan)
            throws ConnectionFailureException, InternalServerException, AuthFailureException, InvalidRequestException, AccountNotExistException {
        String url = SLEEP_TRAINING_PLAN_URL;

        try {
            JSONObject object = plan.toJson();

            JSONObject result = post(url, object, aaaHeaders);

            handleGeneralErrors(result, false);
            handleAAAErrors(result, true);

            plan.setPlanId(result.getString(SleepTrainingPlan.ID));
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Get sleep training plan of the user.
     * @return
     */
    public SleepTrainingPlan getSleepTrainingPlan()
            throws ConnectionFailureException, InternalServerException,
            TrainingPlanNotExistException, AuthFailureException, InvalidRequestException, AccountNotExistException {
        String url = SLEEP_TRAINING_PLAN_URL;
        try {
            JSONObject result = get(url, aaaHeaders);

            handleGeneralErrors(result, false);
            handleAAAErrors(result, false);

            if (result.has(ERROR_CODE_KEY)) {
                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_PLAN_NOT_EXISTS)) {
                    throw new TrainingPlanNotExistException();
                }
                throw new InternalServerException();
            }

            return new SleepTrainingPlan(result);
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Reset sleep training plan.
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public void resetSleepTrainingPlan()
            throws ConnectionFailureException, InternalServerException, AuthFailureException, InvalidRequestException, AccountNotExistException {
        String url = RESET_SLEEP_TRAINING_PLAN_URL;
        try {
            JSONObject result = get(url, aaaHeaders);
            handleGeneralErrors(result, false);
            handleAAAErrors(result, true);
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Add a sleep Training record.
     * @param planId
     * @param elapsedTime
     * @param criedOutTimes
     * @param sootheTimes
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public void addTrainingRecord(String planId, long elapsedTime, int criedOutTimes, int sootheTimes)
            throws ConnectionFailureException, InternalServerException,
            InvalidRequestException, AccountNotExistException,
            AuthFailureException, TrainingPlanNotExistException {
        String url = TRAINING_RECORDS_URL;
        try {
            JSONObject object = new JSONObject();
            object.put("planId", planId);
            object.put("elapsedTime", elapsedTime);
            object.put("criedOutTimes", criedOutTimes);
            object.put("sootheTimes", sootheTimes);

            JSONObject result = post(url, object, aaaHeaders);

            handleGeneralErrors(result, false);
            handleAAAErrors(result, false);

            if (result.has(ERROR_CODE_KEY)) {
                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_PLAN_NOT_EXISTS)) {
                    throw new TrainingPlanNotExistException();
                }
                throw new InternalServerException();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }
}
