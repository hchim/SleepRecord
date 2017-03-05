package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.IdentityServiceClient;

public class WrongVerifyCodeException extends BaseException {

    public WrongVerifyCodeException() {
        super(IdentityServiceClient.ERROR_CODE_WRONG_VERIFY_CODE, "Wrong verification code.");
    }
}
