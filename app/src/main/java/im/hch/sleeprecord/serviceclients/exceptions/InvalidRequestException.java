package im.hch.sleeprecord.serviceclients.exceptions;

import com.sleepaiden.androidcommonutils.exceptions.InternalServerException;
import com.sleepaiden.androidcommonutils.service.BaseServiceClient;

public class InvalidRequestException extends InternalServerException {

    public InvalidRequestException() {
        super(BaseServiceClient.ERROR_INVALID_REQUEST, "Invalid request.");
    }
}
