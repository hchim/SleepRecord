package im.hch.sleeprecord.serviceclients.exceptions;

import com.sleepaiden.androidcommonutils.exceptions.BaseException;

import im.hch.sleeprecord.serviceclients.SleepServiceClient;

/**
 * Created by huiche on 12/30/16.
 */

public class BabyNotExistsException extends BaseException {

    public BabyNotExistsException() {
        super(SleepServiceClient.ERROR_CODE_BABY_NOT_EXISTS, "Baby information does not exist.");
    }
}
