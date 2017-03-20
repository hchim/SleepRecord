package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.IdentityServiceClient;

public class InvalidIDTokenException extends BaseException {

    public InvalidIDTokenException(String message) {
        super(IdentityServiceClient.ERROR_CODE_INVALID_ID_TOKEN, message);
    }
}
