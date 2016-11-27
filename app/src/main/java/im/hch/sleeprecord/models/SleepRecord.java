package im.hch.sleeprecord.models;

import android.util.Pair;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SleepRecord {
    private int month;
    private int date;
    private List<Pair<Date, Date>> sleepTimePairs;
    private double sleepQuality = 0;

    public SleepRecord(int month, int date) {
        this.month = month;
        this.date = date;
    }
}
