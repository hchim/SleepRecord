package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.IdentityServiceClient;

public class EmailUsedException extends BaseException {

    public EmailUsedException() {
        super(IdentityServiceClient.ERROR_CODE_EMAIL_USED, "Email was already used.");
    }
}
