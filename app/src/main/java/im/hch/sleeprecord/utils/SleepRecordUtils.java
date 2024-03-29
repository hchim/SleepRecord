package im.hch.sleeprecord.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import im.hch.sleeprecord.models.SleepRecordsPerDay;

public class SleepRecordUtils {

    /**
     * Fill the empty sleep records.
     * @param records
     * @param from
     * @param to
     * @return
     */
    public static List<SleepRecordsPerDay> fillSleepRecords(List<SleepRecordsPerDay> records, Date from, Date to) {
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTime(from);
        Calendar toCal = Calendar.getInstance();
        toCal.setTime(to);

        LinkedHashMap<String, SleepRecordsPerDay> map = new LinkedHashMap<>();
        while (DateUtils.after(toCal, fromCal)) {
            map.put(DateUtils.dateToStr(toCal.getTime()), new SleepRecordsPerDay(toCal.getTime(), 0));
            toCal.add(Calendar.DATE, -1);
        }

        for (SleepRecordsPerDay record : records) {
            if (DateUtils.after(record.getDateTime(), fromCal)) {
                map.put(DateUtils.dateToStr(record.getDateTime().getTime()), record);
            }
        }

        List<SleepRecordsPerDay> list = new ArrayList<>();
        list.addAll(map.values());

        return list;
    }

    /**
     * Get the corresponding color of the sleep quality.
     * @param qualityLevelColors
     * @param sleepQuality
     * @return
     */
    public static int getQualityColor(int[] qualityLevelColors, double sleepQuality) {
        int roundValue = (int) Math.ceil(sleepQuality);
        if (roundValue < 0) {
            roundValue = 0;
        }
        if (roundValue > 10) {
            roundValue = 10;
        }

        return qualityLevelColors[roundValue];
    }
}
