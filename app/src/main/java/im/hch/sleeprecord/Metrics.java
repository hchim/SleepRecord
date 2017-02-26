package im.hch.sleeprecord;

public class Metrics {

    //performance metrics
    public static final String MAIN_ACTIVITY_LOADING_TIME_METRIC = Constants.APP_NAME + ":MainActivity:Loading:Time";

    //usage metrics
    public static final String MAIN_ACTIVITY_USAGE_TIME_METRIC = Constants.APP_NAME + ":MainActivity:Usage:Time";

    public static final String SLEEP_RECORD_ACTIVITY_USAGE_TIME_METRIC = Constants.APP_NAME + ":SleepRecordActivity:Usage:Time";

    //error metrics
    public static final String SAVE_BABY_INFO_ERROR_METRIC = Constants.APP_NAME + ":SleepService:saveBabyInfo:Error";
    public static final String GET_BABY_INFO_ERROR_METRIC = Constants.APP_NAME + ":SleepService:getBabyInfo:Error";
    public static final String ADD_SLEEP_RECORD_ERROR_METRIC = Constants.APP_NAME + ":SleepService:addSleepRecord:Error";
    public static final String GET_SLEEP_RECORDS_ERROR_METRIC = Constants.APP_NAME + ":SleepService:getSleepRecords:Error";
    public static final String GET_TRAINING_PLAN_ERROR_METRIC = Constants.APP_NAME + ":SleepService:getSleepTrainingPlan:Error";

    public static final String GET_USER_ERROR_METRIC = Constants.APP_NAME + ":IdentityService:getUser:Error";
    public static final String LOGIN_ERROR_METRIC = Constants.APP_NAME + ":IdentityService:login:Error";
    public static final String REGISTER_ERROR_METRIC = Constants.APP_NAME + ":IdentityService:register:Error";
}
