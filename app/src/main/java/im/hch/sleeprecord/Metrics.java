package im.hch.sleeprecord;

public class Metrics {

    //performance metrics
    public static final String MAIN_ACTIVITY_LOADING_TIME_METRIC = Constants.APP_NAME + ":MainActivity:Loading:Time";

    //usage metrics
    public static final String MAIN_ACTIVITY_USAGE_TIME_METRIC = Constants.APP_NAME + ":MainActivity:Usage:Time";

    public static final String SLEEP_RECORD_ACTIVITY_USAGE_TIME_METRIC = Constants.APP_NAME + ":SleepRecordActivity:Usage:Time";

    //add metrics
    public static final String HOME_FRAGMENT_AD_LOADED = Constants.APP_NAME + ":HomeFragment:Ad:Loaded";
    public static final String HOME_FRAGMENT_AD_LOAD_FAILURE = Constants.APP_NAME + ":HomeFragment:Ad:LoadFailure";

    //error metrics
    public static final String SAVE_BABY_INFO_ERROR_METRIC = Constants.APP_NAME + ":SleepService:saveBabyInfo:Error";
    public static final String GET_BABY_INFO_ERROR_METRIC = Constants.APP_NAME + ":SleepService:getBabyInfo:Error";
    public static final String ADD_SLEEP_RECORD_ERROR_METRIC = Constants.APP_NAME + ":SleepService:addSleepRecord:Error";
    public static final String GET_SLEEP_RECORDS_ERROR_METRIC = Constants.APP_NAME + ":SleepService:getSleepRecords:Error";
    public static final String GET_TRAINING_PLAN_ERROR_METRIC = Constants.APP_NAME + ":SleepService:getSleepTrainingPlan:Error";
    public static final String RESET_TRAINING_PLAN_ERROR_METRIC = Constants.APP_NAME + ":SleepService:resetSleepTrainingPlan:Error";
    public static final String ADD_TRAINING_RECORD_ERROR_METRIC = Constants.APP_NAME + ":SleepService:addTrainingRecord:Error";

    public static final String GET_USER_ERROR_METRIC = Constants.APP_NAME + ":IdentityService:getUser:Error";
    public static final String LOGIN_ERROR_METRIC = Constants.APP_NAME + ":IdentityService:login:Error";
    public static final String REGISTER_ERROR_METRIC = Constants.APP_NAME + ":IdentityService:register:Error";
}
