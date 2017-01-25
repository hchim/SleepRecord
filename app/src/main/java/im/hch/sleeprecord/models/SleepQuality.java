package im.hch.sleeprecord.models;

import java.util.Date;

import lombok.Data;

@Data
public class SleepQuality {
    private float sleepQuality;
    private Date date;

    public SleepQuality(Date date, float sleepQuality) {
        this.date = date;
        this.sleepQuality = sleepQuality;
    }
}
