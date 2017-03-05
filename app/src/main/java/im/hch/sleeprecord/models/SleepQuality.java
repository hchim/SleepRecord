package im.hch.sleeprecord.models;

import java.util.Date;

import lombok.Data;

@Data
public class SleepQuality {
    private double sleepQuality;
    private Date date;

    public SleepQuality(Date date, double sleepQuality) {
        this.date = date;
        this.sleepQuality = sleepQuality;
    }
}
