package im.hch.sleeprecord.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public static int yearsBetween(Calendar from, Calendar to) {
        int year = from.get(Calendar.YEAR);
        int years = 0;

        do {
            from.add(Calendar.YEAR, 1);
            if (from.before(to)) {
                years++;
            } else {
                break;
            }
        } while (true);

        from.set(Calendar.YEAR, year);
        return years;
    }

    public static int monthsBetween(Calendar from, Calendar to) {
        int year = from.get(Calendar.YEAR);
        int month = from.get(Calendar.MONTH);
        int months = 0;

        do {
            from.add(Calendar.MONTH, 1);
            if (from.before(to)) {
                months++;
            } else {
                break;
            }
        } while (true);

        from.set(Calendar.YEAR, year);
        from.set(Calendar.MONTH, month);
        return months;
    }

    public static int daysBetween(Calendar from, Calendar to) {
        long toTime = to.getTimeInMillis();
        long fromTime = from.getTimeInMillis();

        if (toTime < fromTime) {
            return 0;
        }

        return (int) ((toTime - fromTime) / 1000/ 60/ 60/ 24);
    }
}
