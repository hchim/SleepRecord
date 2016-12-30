package im.hch.sleeprecord.serviceclients.exceptions;

public class EmailUsedException extends Exception {

    public EmailUsedException() {
        super("Email was already used.");
    }
}
