package im.hch.sleeprecord.serviceclients.exceptions;

import com.sleepaiden.androidcommonutils.exceptions.BaseException;

import im.hch.sleeprecord.serviceclients.SleepServiceClient;

public class TrainingPlanNotExistException extends BaseException {

    public TrainingPlanNotExistException() {
        super(SleepServiceClient.ERROR_PLAN_NOT_EXISTS, "Sleep training plan does not exist.");
    }
}
