package im.hch.sleeprecord.serviceclients.exceptions;

public class TrainingPlanNotExistException extends Exception {

    public TrainingPlanNotExistException() {
        super("Sleep training plan does not exist.");
    }
}
