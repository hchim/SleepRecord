package im.hch.sleeprecord.models;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import im.hch.sleeprecord.utils.DateUtils;
import lombok.Data;

@Data
public class SleepRecordsPerDay {
    private Calendar dateTime;
    private List<SleepRecord> sleepRecords;
    private List<Pair<Date, Date>> sleepTimePairs;
    private double sleepQuality = 0;
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public SleepRecordsPerDay(Date date, double sleepQuality) {
        this.dateTime = Calendar.getInstance();
        this.dateTime.setTime(date);
        this.sleepQuality = sleepQuality;
        sleepTimePairs = new ArrayList<>();
        sleepRecords = new ArrayList<>();
    }

    public static SleepRecordsPerDay create(JSONObject object) {
        try {
            SleepRecordsPerDay record = new SleepRecordsPerDay(
                    DateUtils.strToDate(object.getString("date"), DATE_FORMAT),
                    object.getDouble("quality")
            );

            JSONArray timesArray = object.getJSONArray("times");
            for (int j = 0; j < timesArray.length(); j++) {
                JSONObject timeObj = timesArray.getJSONObject(j);
                record.addSleepTime(new Pair<Date, Date>(
                        DateUtils.strToDate(timeObj.getString("fallAsleepTime"), DATE_FORMAT),
                        DateUtils.strToDate(timeObj.getString("wakeupTime"), DATE_FORMAT)
                ));

                record.addSleepRecord(new SleepRecord(
                    timeObj.getString("_id"),
                    DateUtils.strToDate(timeObj.getString("recSleepTime"), DATE_FORMAT),
                    DateUtils.strToDate(timeObj.getString("recWakeupTime"), DATE_FORMAT)
                ));
            }

            return record;
        } catch (JSONException e) {}

        return null;
    }

    public void addSleepTime(Pair<Date, Date> sleepTime) {
        sleepTimePairs.add(sleepTime);
    }

    public void addSleepRecord(SleepRecord sleepRecord) {
        sleepRecords.add(sleepRecord);
    }

    public int getMonth() {
        return dateTime.get(Calendar.MONTH);
    }

    public int getDate() {
        return dateTime.get(Calendar.DATE);
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("date", DateUtils.dateToStr(dateTime.getTime(), DATE_FORMAT));
            object.put("quality", sleepQuality);

            JSONArray arr = new JSONArray();
            object.put("times", arr);
            for (Pair<Date, Date> pair : sleepTimePairs) {
                JSONObject object2 = new JSONObject();
                object2.put("fallAsleepTime", DateUtils.dateToStr(pair.first, DATE_FORMAT));
                object2.put("wakeupTime", DateUtils.dateToStr(pair.second, DATE_FORMAT));
                arr.put(object2);
            }
            return object;
        } catch (JSONException e) {
            return null;
        }
    }

    public boolean isSunday() {
        int dayOfWeek = dateTime.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SUNDAY;
    }
}
