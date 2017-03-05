package im.hch.sleeprecord.serviceclients.exceptions;

import im.hch.sleeprecord.serviceclients.BaseServiceClient;

public class AccountNotExistException extends BaseException {

    public AccountNotExistException() {
        super(BaseServiceClient.ERROR_CONNECTION_FAILURE, "Account does not exist.");
    }
}
