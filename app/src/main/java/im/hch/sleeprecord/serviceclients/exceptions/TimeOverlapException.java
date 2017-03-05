package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.SleepServiceClient;

public class TimeOverlapException extends BaseException {

    public TimeOverlapException() {
        super(SleepServiceClient.ERROR_CODE_TIME_OVERLAP, "Sleep record overlaps with existing records.");
    }
}
