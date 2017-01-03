package im.hch.sleeprecord.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import im.hch.sleeprecord.models.SleepRecord;

public class SleepRecordUtils {

    /**
     * Fill the empty sleep records.
     * @param records
     * @param from
     * @param to
     * @return
     */
    public static List<SleepRecord> fillSleepRecords(List<SleepRecord> records, Date from, Date to) {
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTime(from);
        Calendar toCal = Calendar.getInstance();
        toCal.setTime(to);

        LinkedHashMap<String, SleepRecord> map = new LinkedHashMap<>();
        while (DateUtils.after(toCal, fromCal)) {
            map.put(DateUtils.dateToStr(toCal.getTime()), new SleepRecord(toCal.getTime(), 0));
            toCal.add(Calendar.DATE, -1);
        }

        for (SleepRecord record : records) {
            if (DateUtils.after(record.getDateTime(), fromCal)) {
                map.put(DateUtils.dateToStr(record.getDateTime().getTime()), record);
            }
        }

        List<SleepRecord> list = new ArrayList<>();
        list.addAll(map.values());

        return list;
    }
}
