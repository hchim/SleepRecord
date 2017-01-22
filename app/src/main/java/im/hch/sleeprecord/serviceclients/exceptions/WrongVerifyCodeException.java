package im.hch.sleeprecord.serviceclients.exceptions;

public class WrongVerifyCodeException extends Exception {

    public WrongVerifyCodeException() {
        super("Wrong verification code.");
    }
}
