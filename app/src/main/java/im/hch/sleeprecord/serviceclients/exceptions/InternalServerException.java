package im.hch.sleeprecord.serviceclients.exceptions;

public class InternalServerException extends Exception {

    public InternalServerException() {
        super("Internal server failure.");
    }
}
