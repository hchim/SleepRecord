package im.hch.sleeprecord.serviceclients.exceptions;

import com.sleepaiden.androidcommonutils.exceptions.BaseException;

import im.hch.sleeprecord.serviceclients.IdentityServiceClient;

public class WrongPasswordException extends BaseException {

    public WrongPasswordException() {
        super(IdentityServiceClient.ERROR_CODE_WRONG_PASSWORD, "Wrong password.");
    }
}
