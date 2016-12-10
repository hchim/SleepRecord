package im.hch.sleeprecord.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT);

    public static String dateToStr(Date date) {
        return dateFormat.format(date);
    }

    public static Date strToDate(String str) {
        try {
            Date time = dateFormat.parse(str);
            return time;
        } catch (ParseException e) {
            return null;
        }
    }
}
