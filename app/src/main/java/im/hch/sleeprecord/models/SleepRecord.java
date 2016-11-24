package im.hch.sleeprecord.models;

import android.util.Pair;

import java.util.Date;
import java.util.List;

public class SleepRecord {
    private int month;
    private int date;
    private List<Pair<Date, Date>> sleepTimePairs;
    private double sleepQuality = 0;

    public SleepRecord(int month, int date) {
        this.month = month;
        this.date = date;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public List<Pair<Date, Date>> getSleepTimePairs() {
        return sleepTimePairs;
    }

    public void setSleepTimePairs(List<Pair<Date, Date>> sleepTimePairs) {
        this.sleepTimePairs = sleepTimePairs;
    }

    public double getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(double sleepQuality) {
        this.sleepQuality = sleepQuality;
    }
}
