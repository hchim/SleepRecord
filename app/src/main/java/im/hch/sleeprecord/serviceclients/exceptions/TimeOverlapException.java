package im.hch.sleeprecord.serviceclients.exceptions;

import com.sleepaiden.androidcommonutils.exceptions.BaseException;

import im.hch.sleeprecord.serviceclients.SleepServiceClient;

public class TimeOverlapException extends BaseException {

    public TimeOverlapException() {
        super(SleepServiceClient.ERROR_CODE_TIME_OVERLAP, "Sleep record overlaps with existing records.");
    }
}
