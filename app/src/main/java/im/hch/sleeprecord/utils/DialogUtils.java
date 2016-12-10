package im.hch.sleeprecord.utils;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;

public class DialogUtils {

    /**
     * Show date picker dialog.
     * @param v
     * @param listener
     */
    public static void showDatePickerDialog(View v, DatePickerDialog.OnDateSetListener listener) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), listener, year, month, day);
        datePickerDialog.show();
    }
}
