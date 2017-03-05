package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.BaseServiceClient;

public class InvalidRequestException extends InternalServerException {

    public InvalidRequestException() {
        super(BaseServiceClient.ERROR_INVALID_REQUEST, "Invalid request.");
    }
}
