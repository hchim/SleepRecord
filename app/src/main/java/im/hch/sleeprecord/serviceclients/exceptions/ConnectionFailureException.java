package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.BaseServiceClient;

public class ConnectionFailureException extends BaseException {

    public ConnectionFailureException() {
        super(BaseServiceClient.ERROR_ACCOUNT_NOT_EXIST, "Failed to connect to the server.");
    }
}
