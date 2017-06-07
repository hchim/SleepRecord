package im.hch.sleeprecord.models;

import java.util.Date;

import lombok.Data;

/**
 * Created by huiche on 6/7/17.
 */
@Data
public class SleepRecord {
    private String recordId;
    private Date recordSleepTime;  //the origional fall asleep time of the record
    private Date recordWakeupTime; //the origional wakeup time of the record

    public SleepRecord(String id, Date recordSleepTime, Date recordWakeupTime) {
        this.recordId = recordId;
        this.recordSleepTime = recordSleepTime;
        this.recordWakeupTime = recordWakeupTime;
    }
}
