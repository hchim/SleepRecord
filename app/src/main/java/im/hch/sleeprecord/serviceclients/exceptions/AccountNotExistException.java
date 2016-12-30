package im.hch.sleeprecord.serviceclients.exceptions;

public class AccountNotExistException extends Exception {

    public AccountNotExistException() {
        super("Account does not exist.");
    }
}
