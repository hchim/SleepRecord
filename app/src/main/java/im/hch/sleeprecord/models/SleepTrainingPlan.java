package im.hch.sleeprecord.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import im.hch.sleeprecord.serviceclients.BaseServiceClient;
import im.hch.sleeprecord.utils.DateUtils;
import lombok.Data;

/**
 * Created by huiche on 2/14/17.
 */
@Data
public class SleepTrainingPlan {
    public static final String TAG = "SleepTrainingPlan";
    public static final String START_DATE = "startDate";
    public static final String FIRST_WEEK_TIME = "firstWeekTime";
    public static final String SECOND_WEEK_TIME = "secondWeekTime";
    public static final String FOLLOWING_WEEK_TIME = "followingWeekTime";

    private Date startDate;
    private TrainingPlanTime firstWeekTime;
    private TrainingPlanTime secondWeekTime;
    private TrainingPlanTime followingWeekTime;

    public SleepTrainingPlan(Date startDate) {
        this.startDate = startDate;
        firstWeekTime = new TrainingPlanTime();
        secondWeekTime = new TrainingPlanTime();
        followingWeekTime = new TrainingPlanTime();
    }

    public SleepTrainingPlan(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        try {
            this.startDate = DateUtils.strToDate(jsonObject.getString(START_DATE), BaseServiceClient.DATE_FORMAT);
            this.firstWeekTime = new TrainingPlanTime(jsonObject.getJSONObject(FIRST_WEEK_TIME));
            this.secondWeekTime = new TrainingPlanTime(jsonObject.getJSONObject(SECOND_WEEK_TIME));
            this.followingWeekTime = new TrainingPlanTime(jsonObject.getJSONObject(FOLLOWING_WEEK_TIME));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create SleepTrainingPlan", e);
        }
    }

    public void setFirstWeekTime(int sootheTime, int firstCriedOut,
                                 int secondCriedOut, int followingCriedOut) {
        firstWeekTime.sootheTime = sootheTime;
        firstWeekTime.firstCriedOut = firstCriedOut;
        firstWeekTime.secondCriedOut = secondCriedOut;
        firstWeekTime.followingCriedOut = followingCriedOut;
    }

    public void setSecondWeekTime(int sootheTime, int firstCriedOut,
                                  int secondCriedOut, int followingCriedOut) {
        secondWeekTime.sootheTime = sootheTime;
        secondWeekTime.firstCriedOut = firstCriedOut;
        secondWeekTime.secondCriedOut = secondCriedOut;
        secondWeekTime.followingCriedOut = followingCriedOut;
    }

    public void setFollowingWeekTime(int sootheTime, int firstCriedOut,
                                     int secondCriedOut, int followingCriedOut) {
        followingWeekTime.sootheTime = sootheTime;
        followingWeekTime.firstCriedOut = firstCriedOut;
        followingWeekTime.secondCriedOut = secondCriedOut;
        followingWeekTime.followingCriedOut = followingCriedOut;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (startDate != null) {
                jsonObject.put(START_DATE, DateUtils.dateToStr(startDate, BaseServiceClient.DATE_FORMAT));
            }
            if (firstWeekTime != null) {
                jsonObject.put(FIRST_WEEK_TIME, firstWeekTime.toJSON());
            }
            if (secondWeekTime != null) {
                jsonObject.put(SECOND_WEEK_TIME, secondWeekTime.toJSON());
            }
            if (followingWeekTime != null) {
                jsonObject.put(FOLLOWING_WEEK_TIME, followingWeekTime.toJSON());
            }
        } catch (JSONException e) {
        }

        return jsonObject;
    }

    public static class TrainingPlanTime {
        public static final String SOOTHE_TIME = "sootheTime";
        public static final String FIRST_CRIED_OUT = "firstCriedOut";
        public static final String SECOND_CRIED_OUT = "secondCriedOut";
        public static final String FOLLOWING_CRIED_OUT = "followingCriedOut";

        private int sootheTime;
        private int firstCriedOut;
        private int secondCriedOut;
        private int followingCriedOut;

        public TrainingPlanTime() {}

        public TrainingPlanTime(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                sootheTime = jsonObject.getInt(SOOTHE_TIME);
                firstCriedOut = jsonObject.getInt(FIRST_CRIED_OUT);
                secondCriedOut = jsonObject.getInt(SECOND_CRIED_OUT);
                followingCriedOut = jsonObject.getInt(FOLLOWING_CRIED_OUT);
            } catch (JSONException e) {}
        }

        public JSONObject toJSON() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(SOOTHE_TIME, sootheTime);
                jsonObject.put(FIRST_CRIED_OUT, firstCriedOut);
                jsonObject.put(SECOND_CRIED_OUT, secondCriedOut);
                jsonObject.put(FOLLOWING_CRIED_OUT, followingCriedOut);
            } catch (JSONException e) {}

            return jsonObject;
        }
    }
}
