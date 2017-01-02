package im.hch.sleeprecord.serviceclients.exceptions;

public class TimeOverlapException extends Exception {

    public TimeOverlapException() {
        super("Sleep record overlaps with existing records.");
    }
}
