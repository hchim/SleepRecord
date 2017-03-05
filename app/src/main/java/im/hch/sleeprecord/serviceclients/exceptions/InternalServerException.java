package im.hch.sleeprecord.serviceclients.exceptions;

public class InternalServerException extends BaseException {
    public static final String ERROR_UNKNOWN = "ERROR_UNKNOWN";

    public InternalServerException() {
        super(ERROR_UNKNOWN, "Internal server failure.");
    }

    public InternalServerException(String errorCode) {
        super(errorCode, "Internal server failure.");
    }

    public InternalServerException(String errorCode, String message) {
        super(errorCode, message);
    }
}
