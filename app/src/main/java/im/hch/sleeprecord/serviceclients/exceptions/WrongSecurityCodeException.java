package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.IdentityServiceClient;

public class WrongSecurityCodeException extends BaseException {

    public WrongSecurityCodeException() {
        super(IdentityServiceClient.ERROR_CODE_WRONG_SECURITY_CODE, "Wrong security code.");
    }
}
