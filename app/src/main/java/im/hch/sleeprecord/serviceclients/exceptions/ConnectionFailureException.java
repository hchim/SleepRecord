package im.hch.sleeprecord.serviceclients.exceptions;

public class ConnectionFailureException extends Exception {

    public ConnectionFailureException() {
        super("Failed to connect to the server.");
    }
}
