package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.BaseServiceClient;

public class AuthFailureException extends BaseException {

    public AuthFailureException() {
        super(BaseServiceClient.ERROR_AUTH_FAILURE, "Failed to authenticate.");
    }
}
