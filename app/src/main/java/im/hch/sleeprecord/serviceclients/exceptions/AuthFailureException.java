package im.hch.sleeprecord.serviceclients.exceptions;

public class AuthFailureException extends Exception {

    public AuthFailureException() {
        super("Failed to authenticate.");
    }
}
