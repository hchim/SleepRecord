package im.hch.sleeprecord.models;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SleepRecord {
    private Calendar dateTime;
    private List<Pair<Date, Date>> sleepTimePairs;
    private double sleepQuality = 0;

    public SleepRecord(Date date, double sleepQuality) {
        this.dateTime = Calendar.getInstance();
        this.dateTime.setTime(date);
        this.sleepQuality = sleepQuality;
        sleepTimePairs = new ArrayList<>();
    }

    public void addSleepTime(Pair<Date, Date> sleepTime) {
        sleepTimePairs.add(sleepTime);
    }

    public int getMonth() {
        return dateTime.get(Calendar.MONTH);
    }

    public int getDate() {
        return dateTime.get(Calendar.DATE);
    }
}
