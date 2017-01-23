package im.hch.sleeprecord.serviceclients.exceptions;

public class WrongSecurityCodeException extends Exception {

    public WrongSecurityCodeException() {
        super("Wrong security code.");
    }
}
