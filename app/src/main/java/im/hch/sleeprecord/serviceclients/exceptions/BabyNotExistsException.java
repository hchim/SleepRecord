package im.hch.sleeprecord.serviceclients.exceptions;

/**
 * Created by huiche on 12/30/16.
 */

public class BabyNotExistsException extends Exception {

    public BabyNotExistsException() {
        super("Baby information does not exist.");
    }
}
